package com.streamflix.auth.controller;

import com.streamflix.auth.dto.AuthRequest;
import com.streamflix.auth.dto.AuthResponse;
import com.streamflix.auth.service.AuthService;
import com.streamflix.common.dto.ApiResponse;
import com.streamflix.common.security.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for authentication operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login with email and password.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse.TokenResponse response = authService.login(request, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse.RegistrationResponse>> register(
            @Valid @RequestBody AuthRequest.Register request) {

        AuthResponse.RegistrationResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Refresh access token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> refreshToken(
            @Valid @RequestBody AuthRequest.RefreshToken request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse.TokenResponse response = authService.refreshAccessToken(
                request.getRefreshToken(), ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Logout (revoke refresh token).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody AuthRequest.RefreshToken request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Logout from all devices.
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId) {

        authService.logoutAllDevices(UUID.fromString(userId));

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Select a profile (get new access token with profile context).
     */
    @PostMapping("/select-profile")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> selectProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId,
            @Valid @RequestBody AuthRequest.SelectProfile request) {

        AuthResponse.TokenResponse response = authService.selectProfile(
                UUID.fromString(userId), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get active sessions.
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AuthResponse.SessionInfo>>> getSessions(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) String userId) {

        List<AuthResponse.SessionInfo> sessions = authService.getActiveSessions(
                UUID.fromString(userId));

        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}
