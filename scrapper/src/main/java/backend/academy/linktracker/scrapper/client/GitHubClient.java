package backend.academy.linktracker.scrapper.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

public interface GitHubClient {

    @GetExchange("/repos/{owner}/{repo}")
    ResponseEntity<Void> checkChanges(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestHeader(value = "If-None-Match", required = false) String eTag
    );
}
