package backend.academy.linktracker.commondto;

import java.net.URI;
import java.util.List;

public record Link(long id, URI url, List<String> tags) {
}
