package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowAnswer(List<Answer> items) {
    public record Answer(
            Owner owner, @JsonProperty("creation_date") Long creationDate, String link, String title, String body) {
        public record Owner(@JsonProperty("display_name") String displayName) {}
    }
}
