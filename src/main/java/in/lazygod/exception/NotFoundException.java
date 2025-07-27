package in.lazygod.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String messageKey) {
        super(messageKey, HttpStatus.NOT_FOUND);
    }
}
