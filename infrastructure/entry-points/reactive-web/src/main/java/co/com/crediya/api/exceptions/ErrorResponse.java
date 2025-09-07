package co.com.crediya.api.exceptions;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String detailError;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.detailError = "";
    }

    public ErrorResponse(int status, String message, String detailError) {
        this.status = status;
        this.message = message;
        this.detailError = detailError;
    }
}
