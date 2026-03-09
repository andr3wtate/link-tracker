package backend.academy.linktracker.commondto;

import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    private final ApiError apiError;
    public ClientException(ApiError apiError) {
        super(apiError.exceptionMessage());
        this.apiError = apiError;
    }
}
