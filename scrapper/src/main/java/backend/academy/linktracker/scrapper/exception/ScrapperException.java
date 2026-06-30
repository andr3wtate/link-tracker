package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public abstract class ScrapperException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ScrapperException(String message) {
        super(message);
    }

    public abstract String getDescription();

    public abstract String getCode();
}
