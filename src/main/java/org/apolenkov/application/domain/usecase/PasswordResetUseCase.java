package org.apolenkov.application.domain.usecase;

import java.util.Optional;

/**
 * Core business operations for password reset functionality.
 */
public interface PasswordResetUseCase {
    /**
     * Creates password reset token for user with specified email address.
     *
     * @param email email address of user requesting password reset
     * @return Optional containing generated token if user exists, empty otherwise
     * @throws IllegalArgumentException if email is null or empty
     */
    Optional<String> createPasswordResetToken(String email);

    /**
     * Resets user's password using valid reset token.
     *
     * @param token password reset token to use
     * @param newPassword new password to set for user
     * @return true if password was successfully reset, false if token is invalid
     * @throws IllegalArgumentException if token or newPassword is null or empty
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * Validates whether password reset token is still valid for use.
     *
     * @param token password reset token to validate
     * @return true if token is valid and can be used
     * @throws IllegalArgumentException if token is null or empty
     */
    boolean isTokenValid(String token);
}
