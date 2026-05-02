package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.dto.LinkUpdate;
import backend.academy.linktracker.commondto.exceptions.ClientException;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.repository.CacheRepository;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubRepoMonitorService {
    private final ScrapperRepository scrapperRepository;
    private final GitHubClient gitHubClient;
    private final ScrapperClient scrapperClient;
    private final CacheRepository<URI, String> eTagRepository;

    @Scheduled(fixedRate = 5000)
    public void checkChanges() {
        List<URI> links = scrapperRepository.getAllLinks();
        links.forEach(link -> {
            if (!link.getHost().equals("github.com")) {
                return;
            }
            Optional<String> eTag = eTagRepository.get(link);
            String[] ownerAndRepo = getOwnerAndRepo(link);
            if (ownerAndRepo == null) {
                return;
            }
            try {
                ResponseEntity<Void> response =
                        gitHubClient.checkChanges(ownerAndRepo[0], ownerAndRepo[1], eTag.orElse(null));
                if (response.getStatusCode().is2xxSuccessful()) {
                    if (eTagRepository.get(link).isEmpty()) {
                        eTagRepository.set(link, response.getHeaders().getETag());
                        return;
                    }
                    eTagRepository.set(link, response.getHeaders().getETag());
                    try {
                        scrapperClient.sendUpdates(new LinkUpdate(
                                0,
                                link.toString(),
                                "Изменение в репозитории",
                                scrapperRepository.getTrackingTgChatIds(link)));
                    } catch (ClientException e) {
                        log.atWarn().addKeyValue("link", link).log("Error in scrapper client while sending updates");
                    }
                } else if (response.getStatusCode().value() != 304) {
                    log.atWarn().addKeyValue("link", link).log("Github response is not 304 or 200");
                }
            } catch (HttpClientErrorException _) {
                log.atWarn().addKeyValue("link", link).log("Error in gitHub client while checking changes");
            }
        });
    }

    private String[] getOwnerAndRepo(URI link) {
        String path = link.getPath();
        List<String> parts =
                Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
        if (parts.size() < 2) {
            return null;
        }
        return new String[] {parts.get(0), parts.get(1)};
    }
}
