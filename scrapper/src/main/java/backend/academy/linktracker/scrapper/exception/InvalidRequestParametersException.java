package backend.academy.linktracker.scrapper.exception;

import org.springframework.http.HttpStatusCode;
import java.io.Serial;

public class InvalidRequestParametersException extends ScrapperException {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DESCRIPTION = "Invalid request parameters";
    private static final String CODE = "400";

    public InvalidRequestParametersException(String message) {
        super(message);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
