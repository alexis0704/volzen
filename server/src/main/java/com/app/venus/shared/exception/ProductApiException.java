package com.app.venus.shared.exception;

import java.util.Objects;

import org.springframework.http.HttpStatus;

public abstract class ProductApiException extends RuntimeException {
    private final String error;
    private final HttpStatus status;

    protected ProductApiException(String error, HttpStatus status, String message) {
        super(message);
        this.error = Objects.requireNonNull(error, "error must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public String getError() {
        return error;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
