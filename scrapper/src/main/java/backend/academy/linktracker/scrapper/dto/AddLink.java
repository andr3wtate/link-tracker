package backend.academy.linktracker.scrapper.dto;

import java.net.URI;
import java.util.List;

public record AddLink(URI link, List<String> tags, List<String> filters) {
}
