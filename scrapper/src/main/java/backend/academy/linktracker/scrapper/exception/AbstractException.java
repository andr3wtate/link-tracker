package backend.academy.linktracker.scrapper.exception;

public abstract class AbstractException extends RuntimeException {
    public AbstractException(String message) {
        super(message);
    }

    public abstract String getDescription();
    public abstract String getCode();
}
