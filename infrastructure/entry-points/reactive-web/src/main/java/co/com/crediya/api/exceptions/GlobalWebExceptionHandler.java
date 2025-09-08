package co.com.crediya.api.exceptions;

import co.com.crediya.exceptions.FundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        return switch (ex) {
            case ValidateAuthException validateAuthException ->
                    handleValidateAuthException(response, validateAuthException);
            case AuthorizationDeniedException authorizationDeniedException ->
                    handleAuthorizationDeniedException(response, authorizationDeniedException);
            case FundException fundException -> handleFundException(response, fundException);
            case ValidateModelException validateModelException ->
                    handleValidateModelException(response, validateModelException);
            default ->
                    Mono.error(ex);
        };

    }

    private Mono<Void> handleValidateAuthException(ServerHttpResponse response, ValidateAuthException ex) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );

        return writeResponse(response, errorResponse);
    }

    private Mono<Void> handleAuthorizationDeniedException(ServerHttpResponse response, AuthorizationDeniedException ex) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );

        return writeResponse(response, errorResponse);
    }

    private Mono<Void> handleFundException(ServerHttpResponse response, FundException ex) {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return writeResponse(response, errorResponse);
    }

    private Mono<Void> handleValidateModelException(ServerHttpResponse response, ValidateModelException ex) {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return writeResponse(response, errorResponse);
    }

    private Mono<Void> writeResponse(ServerHttpResponse response, ErrorResponse errorResponse) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes());
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            String simpleResponse = "{\"code\": 500, \"message\": \"Error interno del servidor\"}";
            DataBuffer buffer = response.bufferFactory().wrap(simpleResponse.getBytes());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.writeWith(Mono.just(buffer));
        }
    }
}