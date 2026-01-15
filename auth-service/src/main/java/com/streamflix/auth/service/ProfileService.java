package com.streamflix.auth.service;

import com.streamflix.auth.dto.ProfileDto;
import com.streamflix.auth.entity.Profile;
import com.streamflix.auth.entity.User;
import com.streamflix.auth.repository.ProfileRepository;
import com.streamflix.auth.repository.UserRepository;
import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing user profiles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;

    @Value("${profile.max-profiles-per-account:5}")
    private int maxProfilesPerAccount;

    @Value("${profile.default-avatar-url:https://cdn.streamflix.com/avatars/default.png}")
    private String defaultAvatarUrl;

    /**
     * Get all profiles for a user.
     */
    @Transactional(readOnly = true)
    public List<ProfileDto.Response> getProfiles(UUID userId) {
        return profileRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get a specific profile.
     */
    @Transactional(readOnly = true)
    public ProfileDto.Response getProfile(UUID userId, UUID profileId) {
        Profile profile = getProfileAndValidateOwnership(userId, profileId);
        return toResponse(profile);
    }

    /**
     * Create a new profile.
     */
    @Transactional
    public ProfileDto.Response createProfile(UUID userId, ProfileDto.CreateRequest request) {
        // Check profile limit
        long currentCount = profileRepository.countByUserId(userId);
        if (currentCount >= maxProfilesPerAccount) {
            throw new ValidationException(
                    String.format("Maximum number of profiles (%d) reached", maxProfilesPerAccount));
        }

        // Check for duplicate name
        if (profileRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new ValidationException("name", "A profile with this name already exists");
        }

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Profile profile = Profile.builder()
                .user(user)
                .name(request.getName())
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : defaultAvatarUrl)
                .isKidsProfile(Boolean.TRUE.equals(request.getIsKidsProfile()))
                .maturityRating(request.getMaturityRating() != null ?
                        request.getMaturityRating() : Profile.MaturityRating.ALL)
                .languagePreference(request.getLanguagePreference() != null ?
                        request.getLanguagePreference() : user.getLanguagePreference())
                .build();

        // If kids profile, enforce kids maturity rating
        if (profile.isKids()) {
            profile.setMaturityRating(Profile.MaturityRating.KIDS);
        }

        profile = profileRepository.save(profile);

        eventPublisher.publishProfileCreatedEvent(user, profile);
        log.info("Profile created: {} for user {}", profile.getId(), userId);

        return toResponse(profile);
    }

    /**
     * Update a profile.
     */
    @Transactional
    public ProfileDto.Response updateProfile(UUID userId, UUID profileId, ProfileDto.UpdateRequest request) {
        Profile profile = getProfileAndValidateOwnership(userId, profileId);

        // Check for duplicate name if changing
        if (request.getName() != null && !request.getName().equals(profile.getName())) {
            if (profileRepository.existsByUserIdAndName(userId, request.getName())) {
                throw new ValidationException("name", "A profile with this name already exists");
            }
            profile.setName(request.getName());
        }

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getMaturityRating() != null && !profile.isKids()) {
            profile.setMaturityRating(request.getMaturityRating());
        }

        if (request.getLanguagePreference() != null) {
            profile.setLanguagePreference(request.getLanguagePreference());
        }

        if (request.getAutoplayNextEpisode() != null) {
            profile.setAutoplayNextEpisode(request.getAutoplayNextEpisode());
        }

        if (request.getAutoplayPreviews() != null) {
            profile.setAutoplayPreviews(request.getAutoplayPreviews());
        }

        profile = profileRepository.save(profile);
        log.info("Profile updated: {}", profileId);

        return toResponse(profile);
    }

    /**
     * Delete a profile.
     */
    @Transactional
    public void deleteProfile(UUID userId, UUID profileId) {
        Profile profile = getProfileAndValidateOwnership(userId, profileId);

        // Check if it's the last profile
        long profileCount = profileRepository.countByUserId(userId);
        if (profileCount <= 1) {
            throw new ValidationException("Cannot delete the last profile");
        }

        // Soft delete
        profile.setDeletedAt(Instant.now());
        profileRepository.save(profile);

        log.info("Profile deleted: {}", profileId);
    }

    /**
     * Set or update profile PIN.
     */
    @Transactional
    public void setProfilePin(UUID userId, UUID profileId, ProfileDto.SetPinRequest request) {
        Profile profile = getProfileAndValidateOwnership(userId, profileId);

        // Validate PIN format (4 digits)
        if (!request.getPin().matches("\\d{4}")) {
            throw new ValidationException("PIN must be exactly 4 digits");
        }

        profile.setPinHash(passwordEncoder.encode(request.getPin()));
        profileRepository.save(profile);

        log.info("PIN set for profile: {}", profileId);
    }

    /**
     * Remove profile PIN.
     */
    @Transactional
    public void removeProfilePin(UUID userId, UUID profileId) {
        Profile profile = getProfileAndValidateOwnership(userId, profileId);

        profile.setPinHash(null);
        profileRepository.save(profile);

        log.info("PIN removed for profile: {}", profileId);
    }

    private Profile getProfileAndValidateOwnership(UUID userId, UUID profileId) {
        Profile profile = profileRepository.findActiveById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));

        if (!profileRepository.belongsToUser(profileId, userId)) {
            throw new ResourceNotFoundException("Profile", profileId.toString());
        }

        return profile;
    }

    private ProfileDto.Response toResponse(Profile profile) {
        return ProfileDto.Response.builder()
                .id(profile.getId().toString())
                .name(profile.getName())
                .avatarUrl(profile.getAvatarUrl())
                .isKidsProfile(profile.isKids())
                .maturityRating(profile.getMaturityRating().name())
                .languagePreference(profile.getLanguagePreference())
                .autoplayNextEpisode(profile.getAutoplayNextEpisode())
                .autoplayPreviews(profile.getAutoplayPreviews())
                .isPinProtected(profile.isPinProtected())
                .build();
    }
}
