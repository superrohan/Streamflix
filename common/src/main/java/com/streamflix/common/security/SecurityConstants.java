package com.streamflix.common.security;

/**
 * Security-related constants used across all services.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // Header names
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String PROFILE_ID_HEADER = "X-Profile-ID";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String API_VERSION_HEADER = "X-API-Version";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String DEVICE_ID_HEADER = "X-Device-ID";

    // JWT claim keys
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_PROFILE_ID = "profileId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_DEVICE_ID = "deviceId";

    // Token types
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_CONTENT_MANAGER = "ROLE_CONTENT_MANAGER";
    public static final String ROLE_ANALYTICS_VIEWER = "ROLE_ANALYTICS_VIEWER";

    // Public paths that don't require authentication
    public static final String[] PUBLIC_PATHS = {
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/forgot-password",
        "/api/v1/health",
        "/actuator/health",
        "/actuator/info",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };
}
