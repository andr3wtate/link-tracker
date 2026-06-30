package backend.academy.linktracker.commondto.dto;

import java.net.URI;
import java.util.List;

public record AddLink(URI link, List<String> tags) {}
