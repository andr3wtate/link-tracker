package backend.academy.linktracker.scrapper.exception;

import java.io.Serial;

public class ItemAlreadyExistsException extends ScrapperException {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DESCRIPTION = "Chat already exists or link is already tracked";
    private static final String CODE = "409";

    public ItemAlreadyExistsException(String message) {
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
