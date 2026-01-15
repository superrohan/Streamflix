package com.streamflix.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTOs for authentication operations.
 */
public class AuthResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;  // seconds until access token expires
        private Instant expiresAt;
        private UserInfo user;
        private ProfileInfo profile;  // null if no profile selected
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String subscriptionTier;
        private String subscriptionStatus;
        private List<String> roles;
        private boolean emailVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfo {
        private String id;
        private String name;
        private String avatarUrl;
        private boolean isKidsProfile;
        private String maturityRating;
        private String languagePreference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private String deviceId;
        private String deviceType;
        private String deviceName;
        private String ipAddress;
        private Instant lastActiveAt;
        private boolean isCurrent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationResponse {
        private String userId;
        private String email;
        private boolean emailVerificationRequired;
        private String message;
    }
}
