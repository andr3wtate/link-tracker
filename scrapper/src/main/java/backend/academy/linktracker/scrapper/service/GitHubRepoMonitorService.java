package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.commondto.ClientException;
import backend.academy.linktracker.commondto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import backend.academy.linktracker.scrapper.repository.ScrapperRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GitHubRepoMonitorService {
    private final ScrapperRepository repository;
    private final GitHubClient gitHubClient;
    private final ScrapperClient scrapperClient;
    private final Map<URI, String> getETag = new ConcurrentHashMap<>();
    private final Map<URI, String[]> getOwnerAndRepo = new ConcurrentHashMap<>();

    GitHubRepoMonitorService(ScrapperRepository repository, GitHubClient gitHubClient, ScrapperClient scrapperClient) {
        this.repository = repository;
        this.gitHubClient = gitHubClient;
        this.scrapperClient = scrapperClient;
    }

    @Scheduled(fixedRate = 5000)
    private void checkChanges() {
        List<URI> links = repository.getAllLinks();
        links.forEach(link -> {
            String eTag = getETag.get(link);
            String[] ownerAndRepo = getOwnerAndRepo(link);
            if (ownerAndRepo == null) {
                // todo логи
                return;
            }
            ResponseEntity<Void> response = gitHubClient.checkChanges(ownerAndRepo[0], ownerAndRepo[1], eTag);
            if (response.getStatusCode().is2xxSuccessful()) {
                getETag.put(link, response.getHeaders().getETag());
                try {
                    scrapperClient.sendUpdates(new LinkUpdate(0, link.toString(), "Изменение в репозитории", repository.getTrackingIds(link)));
                } catch (ClientException e) {
                    // todo логи
                }
            } else if (response.getStatusCode().value() != 304) {
                // todo логи
            }
        });
    }

    private String[] getOwnerAndRepo(URI link) {
        if (getOwnerAndRepo.containsKey(link)) {
            return getOwnerAndRepo.get(link);
        }
        String[] ownerAndRepo = parseLink(link);
        if (ownerAndRepo != null) {
            getOwnerAndRepo.put(link, ownerAndRepo);
        }
        return ownerAndRepo;
    }

    private String[] parseLink(URI link) {
        String path = link.getPath();
        List<String> parts = Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .toList();
        if (parts.size() < 2) {
            return null;
        }
        return new String[]{parts.get(0), parts.get(1)};
    }
}
