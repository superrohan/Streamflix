package com.streamflix.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all Streamflix application exceptions.
 *
 * Exception Hierarchy:
 * - StreamflixException (base)
 *   ├── ResourceNotFoundException (404)
 *   ├── ValidationException (400)
 *   ├── AuthenticationException (401)
 *   ├── AuthorizationException (403)
 *   ├── ConflictException (409)
 *   ├── RateLimitExceededException (429)
 *   └── ServiceUnavailableException (503)
 *
 * Usage:
 * - Throw specific subclasses, not this base class
 * - Include meaningful error codes for client handling
 * - Add context in the message for debugging
 */
@Getter
public class StreamflixException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String correlationId;

    public StreamflixException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.correlationId = null;
    }

    public StreamflixException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.correlationId = null;
    }

    public StreamflixException(String errorCode, String message, HttpStatus httpStatus, String correlationId) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.correlationId = correlationId;
    }
}
