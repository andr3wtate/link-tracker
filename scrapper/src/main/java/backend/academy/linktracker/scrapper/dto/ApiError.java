package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record ApiError(String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
}
