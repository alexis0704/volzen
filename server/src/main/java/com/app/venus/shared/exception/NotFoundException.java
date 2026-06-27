package com.app.venus.shared.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ProductApiException {
    public NotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, message);
    }
}
