package com.streamflix.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "AUTHENTICATION_FAILED";

    public AuthenticationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNAUTHORIZED);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("INVALID_CREDENTIALS", "Invalid email or password");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("TOKEN_EXPIRED", "Authentication token has expired");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("INVALID_TOKEN", "Authentication token is invalid");
    }
}
