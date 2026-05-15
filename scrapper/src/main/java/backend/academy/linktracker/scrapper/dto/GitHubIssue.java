package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record GitHubIssue(@JsonProperty("total_count") int totalCount, List<Issue> items) {
    public record Issue(
            String url,
            String title,
            User user,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            String body,
            @JsonProperty("pull_request") Object pullRequest) {
        public record User(String login) {}
    }
}
