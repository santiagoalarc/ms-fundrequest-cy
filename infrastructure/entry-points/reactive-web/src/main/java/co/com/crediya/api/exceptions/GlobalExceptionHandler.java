package co.com.crediya.api.exceptions;

import co.com.crediya.exceptions.FundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDeniedException(AuthorizationDeniedException exception) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String message = exception.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(FundException.class)
    public ResponseEntity<ErrorResponse> handleFundException(FundException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ValidateModelException.class)
    public ResponseEntity<ErrorResponse> handleModelException(ValidateModelException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ValidateAuthException.class)
    public ResponseEntity<ErrorResponse> handleValidateAuthException(ValidateAuthException ex) {
        // Usar el HttpStatus que viene en la excepción, no hardcodearlo como BAD_REQUEST
        //HttpStatus status = ex.ge() != null ? ex.getStatus() : HttpStatus.UNAUTHORIZED;
        String message = ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), message);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido un error interno del servidor.",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}