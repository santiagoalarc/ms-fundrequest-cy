package co.com.crediya.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ValidateModelException extends RuntimeException {

    private final HttpStatus status;

    @Getter
    private String message;
    public ValidateModelException(String message, HttpStatus status){
        this.message = message;
        this.status = status;
    }

}
