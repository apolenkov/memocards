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
 * Service for password reset functionality
 */
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_HOURS = 24;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create password reset token for user
     */
    @Transactional
    public Optional<String> createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        tokenRepository
                .findByUserIdAndNotUsed(user.getId())
                .ifPresent(token -> tokenRepository.markAsUsed(token.getId()));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), expiresAt);
        tokenRepository.save(resetToken);

        return Optional.of(token);
    }

    /**
     * Reset password using token
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.markAsUsed(resetToken.getId());

        return true;
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isUsed() && !resetToken.isExpired();
    }

    /**
     * Get user email by token
     */
    public Optional<String> getUserEmailByToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
        return userOpt.map(User::getEmail);
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }
}
