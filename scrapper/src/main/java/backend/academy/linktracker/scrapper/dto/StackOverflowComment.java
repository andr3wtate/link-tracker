package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowComment(List<Comment> items) {
    public record Comment(
            Owner owner, @JsonProperty("creation_date") Long creationDate, String link, String body) {
        public record Owner(@JsonProperty("display_name") String displayName) {}
    }
}
