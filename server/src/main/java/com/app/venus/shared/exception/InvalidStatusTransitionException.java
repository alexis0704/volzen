package com.app.venus.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends ProductApiException {
    public InvalidStatusTransitionException(String message) {
        super("INVALID_STATUS_TRANSITION", HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
