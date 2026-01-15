package com.streamflix.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user lacks required permissions.
 * Maps to HTTP 403 Forbidden.
 */
public class AuthorizationException extends StreamflixException {

    private static final String DEFAULT_ERROR_CODE = "ACCESS_DENIED";

    public AuthorizationException(String message) {
        super(DEFAULT_ERROR_CODE, message, HttpStatus.FORBIDDEN);
    }

    public AuthorizationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.FORBIDDEN);
    }

    public static AuthorizationException insufficientPermissions(String resource) {
        return new AuthorizationException(
            "INSUFFICIENT_PERMISSIONS",
            String.format("You do not have permission to access: %s", resource)
        );
    }

    public static AuthorizationException profileAccessDenied(String profileId) {
        return new AuthorizationException(
            "PROFILE_ACCESS_DENIED",
            String.format("You do not have access to profile: %s", profileId)
        );
    }
}
