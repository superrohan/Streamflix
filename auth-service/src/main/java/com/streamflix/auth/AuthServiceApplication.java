package com.streamflix.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Streamflix Authentication & Profile Service.
 *
 * Responsibilities:
 * - User registration and authentication
 * - JWT token generation and validation
 * - Profile management (multiple profiles per account, like Netflix)
 * - Role-based access control
 * - Session management and token blacklisting
 *
 * Security Model:
 * - Passwords hashed with BCrypt (work factor 12)
 * - JWT tokens with RS256/HS256 signing
 * - Refresh token rotation for security
 * - Device tracking for suspicious activity detection
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
