package org.apolenkov.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apolenkov.application.domain.port.PasswordResetTokenRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.PasswordResetToken;
import org.apolenkov.application.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing password reset functionality in the application.
 * Provides secure password reset with time-limited, single-use tokens.
 */
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Number of hours after which password reset token expires.
     * Balances security with usability. 24 hours is common industry standard.
     */
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    /**
     * Creates PasswordResetService with required dependencies.
     *
     * @param tokenRepositoryValue repository for password reset token operations
     * @param userRepositoryValue repository for user operations
     * @param passwordEncoderValue encoder for secure password hashing
     * @throws IllegalArgumentException if any parameter is null
     */
    public PasswordResetService(
            final PasswordResetTokenRepository tokenRepositoryValue,
            final UserRepository userRepositoryValue,
            final PasswordEncoder passwordEncoderValue) {
        if (tokenRepositoryValue == null) {
            throw new IllegalArgumentException("PasswordResetTokenRepository cannot be null");
        }
        if (userRepositoryValue == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoderValue == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }

        this.tokenRepository = tokenRepositoryValue;
        this.userRepository = userRepositoryValue;
        this.passwordEncoder = passwordEncoderValue;
    }

    /**
     * Creates password reset token for user with specified email address.
     * Implements first step of password reset process.
     *
     * @param email email address of user requesting password reset
     * @return Optional containing generated token if user exists, empty otherwise
     * @throws IllegalArgumentException if email is null or empty
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public Optional<String> createPasswordResetToken(final String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            // Return empty to prevent user enumeration attacks
            return Optional.empty();
        }

        User user = userOpt.get();

        // Invalidate any existing unused tokens for this user
        tokenRepository
                .findByUserIdAndNotUsed(user.getId())
                .ifPresent(token -> tokenRepository.markAsUsed(token.getId()));

        // Generate new secure token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), expiresAt);
        tokenRepository.save(resetToken);

        return Optional.of(token);
    }

    /**
     * Resets user's password using valid reset token.
     * Implements second step of password reset process.
     *
     * @param token password reset token to use
     * @param newPassword new password to set for user
     * @return true if password was successfully reset, false if token is invalid
     * @throws IllegalArgumentException if token or newPassword is null or empty
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public boolean resetPassword(final String token, final String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token.trim());
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Validate token is still valid
        if (resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        // Retrieve associated user
        Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Securely hash and update password
        user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        // Mark token as used to prevent reuse
        tokenRepository.markAsUsed(resetToken.getId());

        return true;
    }

    /**
     * Validates whether password reset token is still valid for use.
     *
     * @param token password reset token to validate
     * @return true if token is valid and can be used
     * @throws IllegalArgumentException if token is null or empty
     */
    public boolean isTokenValid(final String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token.trim());
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isUsed() && !resetToken.isExpired();
    }
}
