package com.streamflix.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a downstream service is unavailable.
 * Maps to HTTP 503 Service Unavailable.
 */
public class ServiceUnavailableException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "SERVICE_UNAVAILABLE";

    public ServiceUnavailableException(String serviceName) {
        super(DEFAULT_ERROR_CODE,
              String.format("Service '%s' is currently unavailable. Please try again later.", serviceName),
              HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(DEFAULT_ERROR_CODE,
              String.format("Service '%s' is currently unavailable. Please try again later.", serviceName),
              HttpStatus.SERVICE_UNAVAILABLE,
              cause);
    }
}
