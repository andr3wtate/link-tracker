package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.commondto.ApiError;
import lombok.Getter;

@Getter
public class BotClientException extends RuntimeException {
    private final ApiError apiError;
    public BotClientException(ApiError apiError) {
        super(apiError.exceptionMessage());
        this.apiError = apiError;
    }
}
