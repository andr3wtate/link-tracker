package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface GitHubClient {
    @GetExchange("/search/issues")
    ResponseEntity<GitHubIssue> getChanges(@RequestParam("q") String query);
}
