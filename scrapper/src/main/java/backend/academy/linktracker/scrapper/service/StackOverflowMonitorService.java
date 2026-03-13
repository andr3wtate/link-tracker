package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.ClientException;
import backend.academy.linktracker.commondto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StackOverflowMonitorService {
    private final ScrapperRepository scrapperRepository;
    private final StackOverflowClient stackOverflowClient;
    private final ScrapperClient scrapperClient;
    private final CacheRepository<String, Long> lastActivityRepository;

    @Value("${app.stackoverflow.key}")
    private String STACK_OVERFLOW_KEY;


    @Scheduled(fixedRate = 5000)
    private void checkChanges() {
        List<URI> links = scrapperRepository.getAllLinks();
        links.forEach(link -> {
            if (!link.getHost().equals("stackoverflow.com")) {
                return;
            }
            String id = getId(link);
            if (id == null) {
                // todo логи
                return;
            }
            try {
                ResponseEntity<@NotNull StackOverflowResponse> response = stackOverflowClient.checkChanges(id, "stackoverflow", STACK_OVERFLOW_KEY);
                if (response.getStatusCode().is2xxSuccessful()) {
                    long lastActivity = response.getBody().items().getFirst().lastActivityDate();
                    Long lastActivityStored = lastActivityRepository.get(id);
                    if (lastActivityStored == null) {
                        lastActivityRepository.set(id, lastActivity);
                        return;
                    }
                    if (lastActivityStored != lastActivity) {
                        lastActivityRepository.set(id, lastActivity);
                        try {
                            scrapperClient.sendUpdates(new LinkUpdate(0, link.toString(), "Изменение в вопросе", scrapperRepository.getTrackingIds(link)));
                        } catch (ClientException e) {
                            // todo логи
                        }
                    }
                } else {
                    // todo логи
                }
            } catch (HttpClientErrorException _) {

            }
        });
    }

    private String getId(URI link) {
        String path = link.getPath();
        List<String> parts = Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .toList();
        if (parts.size() < 2) {
            return null;
        }
        return parts.get(1);
    }
}
