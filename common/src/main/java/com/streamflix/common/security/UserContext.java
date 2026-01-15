package com.streamflix.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Holds authenticated user context propagated from API Gateway.
 *
 * This object is populated from headers set by the gateway after
 * JWT validation, allowing downstream services to access user info
 * without re-validating the token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private String userId;
    private String profileId;
    private String email;
    private List<String> roles;
    private String deviceId;
    private String correlationId;

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Check if user is an admin.
     */
    public boolean isAdmin() {
        return hasRole(SecurityConstants.ROLE_ADMIN);
    }

    /**
     * Check if user is a content manager.
     */
    public boolean isContentManager() {
        return hasRole(SecurityConstants.ROLE_CONTENT_MANAGER);
    }

    /**
     * Check if a profile is selected.
     */
    public boolean hasProfileSelected() {
        return profileId != null && !profileId.isEmpty();
    }
}
