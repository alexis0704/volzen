package com.app.venus.shared.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ProductApiException {
    public UnprocessableEntityException(String message) {
        super("UNPROCESSABLE_ENTITY", HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
