package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record ListLinks(List<Link> links, int size)  {
}
