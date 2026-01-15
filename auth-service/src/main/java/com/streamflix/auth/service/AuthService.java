package com.streamflix.auth.service;

import com.streamflix.auth.dto.AuthRequest;
import com.streamflix.auth.dto.AuthResponse;
import com.streamflix.auth.entity.Profile;
import com.streamflix.auth.entity.RefreshToken;
import com.streamflix.auth.entity.Role;
import com.streamflix.auth.entity.User;
import com.streamflix.auth.repository.ProfileRepository;
import com.streamflix.auth.repository.RefreshTokenRepository;
import com.streamflix.auth.repository.RoleRepository;
import com.streamflix.auth.repository.UserRepository;
import com.streamflix.auth.security.JwtTokenProvider;
import com.streamflix.common.exception.AuthenticationException;
import com.streamflix.common.exception.ConflictException;
import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Authentication service handling user login, registration, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;

    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Authenticate user and generate tokens.
     */
    @Transactional
    public AuthResponse.TokenResponse login(AuthRequest.Login request, String ipAddress, String userAgent) {
        log.debug("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> {
                    log.debug("User not found: {}", request.getEmail());
                    return AuthenticationException.invalidCredentials();
                });

        // Check if account is locked
        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", user.getEmail());
            throw new AuthenticationException("ACCOUNT_LOCKED",
                    "Account is locked. Please try again later or contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw AuthenticationException.invalidCredentials();
        }

        // Successful login
        user.recordSuccessfulLogin(ipAddress);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user, null);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store refresh token
        saveRefreshToken(user, refreshToken, request.getDeviceId(), request.getDeviceType(),
                request.getDeviceName(), ipAddress, userAgent);

        // Publish login event
        eventPublisher.publishLoginEvent(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return buildTokenResponse(user, null, accessToken, refreshToken);
    }

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse.RegistrationResponse register(AuthRequest.Register request) {
        log.debug("Registration attempt for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ConflictException.emailAlreadyExists(request.getEmail());
        }

        // Get default user role
        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default user role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .countryCode(request.getCountryCode())
                .emailVerified(false)
                .build();

        user.addRole(userRole);
        user = userRepository.save(user);

        // Create default profile
        Profile defaultProfile = Profile.builder()
                .name(request.getFirstName() != null ? request.getFirstName() : "Profile 1")
                .user(user)
                .build();
        profileRepository.save(defaultProfile);

        // Publish registration event
        eventPublisher.publishRegistrationEvent(user);

        log.info("User registered successfully: {}", user.getEmail());

        return AuthResponse.RegistrationResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .emailVerificationRequired(true)
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse.TokenResponse refreshAccessToken(String refreshToken, String ipAddress, String userAgent) {
        String tokenHash = jwtTokenProvider.hashToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findValidByTokenHash(tokenHash, Instant.now())
                .orElseThrow(() -> AuthenticationException.invalidToken());

        User user = storedToken.getUser();

        // Check if user is still active
        if (user.isDeleted() || user.isLocked()) {
            storedToken.revoke("User account inactive");
            refreshTokenRepository.save(storedToken);
            throw new AuthenticationException("ACCOUNT_INACTIVE", "Account is no longer active");
        }

        // Token rotation: revoke old token and issue new one
        storedToken.revoke("Token rotated");
        refreshTokenRepository.save(storedToken);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, null);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store new refresh token
        saveRefreshToken(user, newRefreshToken, storedToken.getDeviceId(), storedToken.getDeviceType(),
                storedToken.getDeviceName(), ipAddress, userAgent);

        return buildTokenResponse(user, null, newAccessToken, newRefreshToken);
    }

    /**
     * Logout user (revoke refresh token).
     */
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = jwtTokenProvider.hashToken(refreshToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.revoke("User logout");
                    refreshTokenRepository.save(token);
                    log.info("User logged out: {}", token.getUser().getEmail());
                });
    }

    /**
     * Logout from all devices.
     */
    @Transactional
    public void logoutAllDevices(UUID userId) {
        int revokedCount = refreshTokenRepository.revokeAllByUserId(userId, Instant.now(), "Logout all devices");
        log.info("Revoked {} refresh tokens for user {}", revokedCount, userId);
    }

    /**
     * Select a profile and generate a new access token with profile context.
     */
    @Transactional
    public AuthResponse.TokenResponse selectProfile(UUID userId, AuthRequest.SelectProfile request) {
        UUID profileId = UUID.fromString(request.getProfileId());

        Profile profile = profileRepository.findByIdWithUser(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", profileId.toString()));

        // Verify profile belongs to user
        if (!profile.getUser().getId().equals(userId)) {
            throw new ValidationException("Profile does not belong to this account");
        }

        // Check PIN if protected
        if (profile.isPinProtected()) {
            if (request.getPin() == null || request.getPin().isEmpty()) {
                throw new ValidationException("PIN is required for this profile");
            }
            if (!passwordEncoder.matches(request.getPin(), profile.getPinHash())) {
                throw new ValidationException("Invalid PIN");
            }
        }

        User user = profile.getUser();

        // Generate new access token with profile
        String accessToken = jwtTokenProvider.generateAccessToken(user, profile);

        return AuthResponse.TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .expiresAt(Instant.now().plusSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds()))
                .profile(AuthResponse.ProfileInfo.builder()
                        .id(profile.getId().toString())
                        .name(profile.getName())
                        .avatarUrl(profile.getAvatarUrl())
                        .isKidsProfile(profile.isKids())
                        .maturityRating(profile.getMaturityRating().name())
                        .languagePreference(profile.getLanguagePreference())
                        .build())
                .build();
    }

    /**
     * Get active sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<AuthResponse.SessionInfo> getActiveSessions(UUID userId) {
        return refreshTokenRepository.findActiveByUserId(userId, Instant.now()).stream()
                .map(token -> AuthResponse.SessionInfo.builder()
                        .deviceId(token.getDeviceId())
                        .deviceType(token.getDeviceType())
                        .deviceName(token.getDeviceName())
                        .ipAddress(token.getIpAddress())
                        .lastActiveAt(token.getIssuedAt())
                        .build())
                .toList();
    }

    private void handleFailedLogin(User user) {
        user.recordFailedLogin();

        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            Instant lockUntil = Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
            user.lockAccount("Too many failed login attempts", lockUntil);
            log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
        }

        userRepository.save(user);
    }

    private void saveRefreshToken(User user, String refreshToken, String deviceId, String deviceType,
                                   String deviceName, String ipAddress, String userAgent) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(jwtTokenProvider.hashToken(refreshToken))
                .deviceId(deviceId)
                .deviceType(deviceType)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(jwtTokenProvider.getRefreshTokenExpiration())
                .build();

        refreshTokenRepository.save(token);
    }

    private AuthResponse.TokenResponse buildTokenResponse(User user, Profile profile,
                                                           String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .subscriptionTier(user.getSubscriptionTier().name())
                .subscriptionStatus(user.getSubscriptionStatus().name())
                .roles(roles)
                .emailVerified(user.getEmailVerified())
                .build();

        AuthResponse.ProfileInfo profileInfo = null;
        if (profile != null) {
            profileInfo = AuthResponse.ProfileInfo.builder()
                    .id(profile.getId().toString())
                    .name(profile.getName())
                    .avatarUrl(profile.getAvatarUrl())
                    .isKidsProfile(profile.isKids())
                    .maturityRating(profile.getMaturityRating().name())
                    .languagePreference(profile.getLanguagePreference())
                    .build();
        }

        return AuthResponse.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .expiresAt(Instant.now().plusSeconds(jwtTokenProvider.getAccessTokenExpirationSeconds()))
                .user(userInfo)
                .profile(profileInfo)
                .build();
    }
}
