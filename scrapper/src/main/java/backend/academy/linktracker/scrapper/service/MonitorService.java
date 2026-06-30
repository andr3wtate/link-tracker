package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.LinkForMonitorService;
import backend.academy.linktracker.scrapper.messaging.MessageSender;
import backend.academy.linktracker.scrapper.properties.MonitorServiceProperties;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import backend.academy.linktracker.scrapper.service.linkhandler.GitHubLinkHandler;
import backend.academy.linktracker.scrapper.service.linkhandler.StackOverflowLinkHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {
    private final MonitorServiceProperties properties;
    private final ScrapperRepository scrapperRepository;
    private final MessageSender messageSender;
    private final GitHubLinkHandler gitHubLinkHandler;
    private final StackOverflowLinkHandler stackOverflowLinkHandler;
    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(properties.getThreads());
    }

    @Scheduled(fixedDelayString = "${app.monitor-service.launch-interval}")
    public void checkChanges() {
        long lastId = 0;
        final int batchSize = properties.getBatchSize();

        while (true) {
            List<LinkForMonitorService> linksBatch = scrapperRepository.getLinksBatch(lastId, batchSize);
            if (linksBatch.isEmpty()) {
                break;
            }
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (LinkForMonitorService link : linksBatch) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> {
                            try {
                                Instant time = Instant.now().minus(5, ChronoUnit.SECONDS);
                                processLink(link);
                                scrapperRepository.updateLastCheckTime(link.id(), time);
                            } catch (Exception e) {
                                log.atWarn().addKeyValue("exception", e).log("Exception while processing link");
                                messageSender.sendUpdate(new LinkUpdate(
                                        link.url().toString(),
                                        "Ошибка во время обработки ссылки",
                                        scrapperRepository.getTrackingTgChatIds(link.url())));
                            }
                        },
                        executor);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            lastId = linksBatch.getLast().id();
        }
    }

    private void processLink(LinkForMonitorService link) {
        String host = link.url().getHost();
        if ("github.com".equals(host)) {
            gitHubLinkHandler.processLink(link);
        } else if ("stackoverflow.com".equals(host)) {
            stackOverflowLinkHandler.processLink(link);
        } else {
            log.atError().addKeyValue("link", link).log("Links's host is not github.com or stackoverflow.com");
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    log.warn("Executor didn't stop");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
