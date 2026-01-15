package com.streamflix.auth.controller;

import com.streamflix.auth.dto.ProfileDto;
import com.streamflix.auth.service.ProfileService;
import com.streamflix.common.dto.ApiResponse;
import com.streamflix.common.security.SecurityConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for profile management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get all profiles for the current user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProfileDto.Response>>> getProfiles(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId) {

        List<ProfileDto.Response> profiles = profileService.getProfiles(UUID.fromString(userId));

        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    /**
     * Get a specific profile.
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<ApiResponse<ProfileDto.Response>> getProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @PathVariable String profileId) {

        ProfileDto.Response profile = profileService.getProfile(
                UUID.fromString(userId), UUID.fromString(profileId));

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Create a new profile.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProfileDto.Response>> createProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @Valid @RequestBody ProfileDto.CreateRequest request) {

        ProfileDto.Response profile = profileService.createProfile(
                UUID.fromString(userId), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(profile));
    }

    /**
     * Update a profile.
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<ApiResponse<ProfileDto.Response>> updateProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @PathVariable String profileId,
            @Valid @RequestBody ProfileDto.UpdateRequest request) {

        ProfileDto.Response profile = profileService.updateProfile(
                UUID.fromString(userId), UUID.fromString(profileId), request);

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Delete a profile.
     */
    @DeleteMapping("/{profileId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @PathVariable String profileId) {

        profileService.deleteProfile(UUID.fromString(userId), UUID.fromString(profileId));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Set or update profile PIN.
     */
    @PostMapping("/{profileId}/pin")
    public ResponseEntity<ApiResponse<Void>> setProfilePin(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @PathVariable String profileId,
            @Valid @RequestBody ProfileDto.SetPinRequest request) {

        profileService.setProfilePin(UUID.fromString(userId), UUID.fromString(profileId), request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Remove profile PIN.
     */
    @DeleteMapping("/{profileId}/pin")
    public ResponseEntity<ApiResponse<Void>> removeProfilePin(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @PathVariable String profileId) {

        profileService.removeProfilePin(UUID.fromString(userId), UUID.fromString(profileId));

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
