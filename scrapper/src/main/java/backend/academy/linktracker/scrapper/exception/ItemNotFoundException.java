package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class ItemNotFoundException extends ScrapperException {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DESCRIPTION = "Chat doesn't exists or link not found";
    private static final String CODE = "404";

    public ItemNotFoundException(String message) {
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
