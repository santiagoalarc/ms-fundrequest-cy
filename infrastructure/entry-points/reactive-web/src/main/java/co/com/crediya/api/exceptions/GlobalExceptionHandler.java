package co.com.crediya.api.exceptions;


import co.com.crediya.exceptions.FundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FundException.class)
    public ResponseEntity<ErrorResponse> handleFundException(FundException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ValidateModelException.class)
    public ResponseEntity<ErrorResponse> handleModelException(ValidateModelException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido un error interno del servidor."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

