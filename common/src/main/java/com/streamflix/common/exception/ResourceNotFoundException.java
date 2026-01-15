package com.streamflix.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(DEFAULT_ERROR_CODE,
              String.format("%s not found with id: %s", resourceType, resourceId),
              HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(DEFAULT_ERROR_CODE,
              String.format("%s not found with %s: %s", resourceType, field, value),
              HttpStatus.NOT_FOUND);
    }
}
