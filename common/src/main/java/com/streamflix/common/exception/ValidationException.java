package com.streamflix.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when request validation fails.
 * Maps to HTTP 400 Bad Request.
 */
@Getter
public class ValidationException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";

    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = null;
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String field, String message) {
        super(DEFAULT_ERROR_CODE,
              String.format("Validation failed for field '%s': %s", field, message),
              HttpStatus.BAD_REQUEST);
        this.fieldErrors = Map.of(field, List.of(message));
    }
}
