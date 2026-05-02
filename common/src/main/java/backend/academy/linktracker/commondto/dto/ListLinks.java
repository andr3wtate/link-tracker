package backend.academy.linktracker.commondto.dto;

import java.util.List;

public record ListLinks(List<Link> links, int size) {}
