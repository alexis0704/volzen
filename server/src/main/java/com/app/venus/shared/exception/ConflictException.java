package com.app.venus.shared.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ProductApiException {
    public ConflictException(String message) {
        super("RESOURCE_CONFLICT", HttpStatus.CONFLICT, message);
    }

    public ConflictException(String error, String message) {
        super(error, HttpStatus.CONFLICT, message);
    }
}
