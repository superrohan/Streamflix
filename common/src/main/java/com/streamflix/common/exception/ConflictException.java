package com.streamflix.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's a conflict with existing data.
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "CONFLICT";

    public ConflictException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.CONFLICT);
    }

    public ConflictException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.CONFLICT);
    }

    public static ConflictException duplicateResource(String resourceType, String identifier) {
        return new ConflictException(
            "DUPLICATE_RESOURCE",
            String.format("%s already exists with identifier: %s", resourceType, identifier)
        );
    }

    public static ConflictException emailAlreadyExists(String email) {
        return new ConflictException(
            "EMAIL_EXISTS",
            String.format("An account with email '%s' already exists", email)
        );
    }
}
