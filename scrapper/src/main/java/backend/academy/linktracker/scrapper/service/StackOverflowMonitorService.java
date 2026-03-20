package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.exceptions.ClientException;
import backend.academy.linktracker.commondto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StackOverflowMonitorService {
    private final ScrapperRepository scrapperRepository;
    private final StackOverflowClient stackOverflowClient;
    private final ScrapperClient scrapperClient;
    private final CacheRepository<String, Long> lastActivityRepository;

    @Value("${app.stackoverflow.key}")
    private String STACK_OVERFLOW_KEY;

    @Scheduled(fixedRate = 5000)
    public void checkChanges() {
        List<URI> links = scrapperRepository.getAllLinks();
        links.forEach(link -> {
            if (!link.getHost().equals("stackoverflow.com")) {
                return;
            }
            String id = getId(link);
            if (id == null) {
                return;
            }
            try {
                ResponseEntity<@NotNull StackOverflowResponse> response =
                        stackOverflowClient.checkChanges(id, "stackoverflow", STACK_OVERFLOW_KEY);
                if (response.getStatusCode().is2xxSuccessful()) {
                    long lastActivity = response.getBody().items().getFirst().lastActivityDate();
                    Optional<Long> lastActivityStored = lastActivityRepository.get(id);
                    if (lastActivityStored.isEmpty()) {
                        lastActivityRepository.set(id, lastActivity);
                        return;
                    }
                    if (lastActivityStored.get() != lastActivity) {
                        lastActivityRepository.set(id, lastActivity);
                        try {
                            scrapperClient.sendUpdates(new LinkUpdate(
                                    0,
                                    link.toString(),
                                    "Изменение в вопросе",
                                    scrapperRepository.getTrackingIds(link)));
                        } catch (ClientException e) {
                            log.atWarn()
                                    .addKeyValue("link", link)
                                    .log("Error in scrapper client while sending updates");
                        }
                    }
                } else {
                    log.atWarn().addKeyValue("link", link).log("StackOverflow response is not 200");
                }
            } catch (HttpClientErrorException _) {
                log.atWarn().addKeyValue("link", link).log("Error in stackOverflow client while checking changes");
            }
        });
    }

    private String getId(URI link) {
        String path = link.getPath();
        List<String> parts =
                Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
        if (parts.size() < 2) {
            return null;
        }
        return parts.get(1);
    }
}
