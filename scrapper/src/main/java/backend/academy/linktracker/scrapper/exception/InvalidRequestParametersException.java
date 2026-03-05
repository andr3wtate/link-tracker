package backend.academy.linktracker.scrapper.exception;

public class InvalidRequestParametersException extends AbstractException {
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
