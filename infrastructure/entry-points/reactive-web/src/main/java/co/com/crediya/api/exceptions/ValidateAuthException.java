package co.com.crediya.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidateAuthException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String message;

    public ValidateAuthException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}