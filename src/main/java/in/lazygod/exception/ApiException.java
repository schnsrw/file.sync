package in.lazygod.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String messageKey;
    private final HttpStatus status;

    public ApiException(String messageKey, HttpStatus status) {
        super(messageKey);
        this.messageKey = messageKey;
        this.status = status;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
