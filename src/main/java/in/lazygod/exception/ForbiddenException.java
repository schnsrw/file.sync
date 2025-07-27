package in.lazygod.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String messageKey) {
        super(messageKey, HttpStatus.FORBIDDEN);
    }
}
