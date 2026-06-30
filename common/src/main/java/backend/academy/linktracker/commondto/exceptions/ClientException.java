package backend.academy.linktracker.commondto.exceptions;

import backend.academy.linktracker.commondto.dto.ApiError;
import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    private final ApiError apiError;

    public ClientException(ApiError apiError) {
        super(apiError.exceptionMessage());
        this.apiError = apiError;
    }
}
