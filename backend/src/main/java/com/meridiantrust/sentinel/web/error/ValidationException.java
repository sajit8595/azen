package com.meridiantrust.sentinel.web.error;

/** Thrown when a request or ingested record fails validation. Maps to HTTP 400. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
