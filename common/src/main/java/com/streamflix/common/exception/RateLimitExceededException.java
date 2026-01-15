package com.streamflix.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded.
 * Maps to HTTP 429 Too Many Requests.
 */
@Getter
public class RateLimitExceededException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "RATE_LIMIT_EXCEEDED";

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(long retryAfterSeconds) {
        super(DEFAULT_ERROR_CODE,
              String.format("Rate limit exceeded. Please retry after %d seconds", retryAfterSeconds),
              HttpStatus.TOO_MANY_REQUESTS);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
