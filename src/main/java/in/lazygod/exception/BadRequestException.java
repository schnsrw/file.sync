package in.lazygod.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String messageKey) {
        super(messageKey, HttpStatus.BAD_REQUEST);
    }
}
