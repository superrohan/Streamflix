package com.streamflix.auth.dto;

import com.streamflix.auth.entity.Profile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs for profile operations.
 */
public class ProfileDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Profile name is required")
        @Size(min = 1, max = 50, message = "Profile name must be between 1 and 50 characters")
        private String name;

        private String avatarUrl;
        private Boolean isKidsProfile;
        private Profile.MaturityRating maturityRating;
        private String languagePreference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(min = 1, max = 50, message = "Profile name must be between 1 and 50 characters")
        private String name;

        private String avatarUrl;
        private Profile.MaturityRating maturityRating;
        private String languagePreference;
        private Boolean autoplayNextEpisode;
        private Boolean autoplayPreviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetPinRequest {
        @NotBlank(message = "PIN is required")
        @Size(min = 4, max = 4, message = "PIN must be exactly 4 digits")
        private String pin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String avatarUrl;
        private boolean isKidsProfile;
        private String maturityRating;
        private String languagePreference;
        private boolean autoplayNextEpisode;
        private boolean autoplayPreviews;
        private boolean isPinProtected;
    }
}
