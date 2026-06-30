package backend.academy.linktracker.commondto.dto;

import java.net.URI;
import java.util.List;

public record Link(long id, URI url, List<String> tags) {
    public Link(long id, String url, List<String> tags) {
        this(id, URI.create(url), tags);
    }
}
