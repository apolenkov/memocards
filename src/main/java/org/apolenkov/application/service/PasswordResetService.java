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
 *
 * <p>This service provides a secure mechanism for users to reset their passwords
 * when they forget them. It implements industry-standard security practices:</p>
 * <ul>
 *   <li><strong>Secure Token Generation:</strong> Uses cryptographically secure UUIDs</li>
 *   <li><strong>Time-Limited Tokens:</strong> Tokens expire after a configurable period</li>
 *   <li><strong>Single-Use Tokens:</strong> Each token can only be used once</li>
 *   <li><strong>Secure Password Hashing:</strong> Uses Spring Security's PasswordEncoder</li>
 *   <li><strong>Transaction Safety:</strong> Ensures data consistency during operations</li>
 * </ul>
 *
 * <p>The password reset process follows this workflow:</p>
 * <ol>
 *   <li>User requests password reset by providing their email address</li>
 *   <li>Service generates a secure token and sends it to the user</li>
 *   <li>User receives token and provides new password</li>
 *   <li>Service validates token and updates password if valid</li>
 *   <li>Token is marked as used to prevent reuse</li>
 * </ol>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Create a password reset token
 * Optional<String> token = passwordResetService.createPasswordResetToken("user@example.com");
 * if (token.isPresent()) {
 *     // Send token to user via email
 *     emailService.sendPasswordResetEmail("user@example.com", token.get());
 * }
 *
 * // Reset password using token
 * boolean success = passwordResetService.resetPassword(token, "newPassword123");
 * if (success) {
 *     // Password successfully reset
 * }
 * }</pre>
 *
 * @see PasswordResetToken
 * @see User
 * @see PasswordResetTokenRepository
 * @see UserRepository
 * @see PasswordEncoder
 * @see Transactional
 */
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * The number of hours after which a password reset token expires.
     *
     * <p>This value balances security (shorter expiration) with usability
     * (longer expiration for user convenience). 24 hours is a common
     * industry standard that provides adequate security while allowing
     * users time to check their email and complete the reset process.</p>
     */
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    /**
     * Constructs a new PasswordResetService with the required dependencies.
     *
     * <p>This constructor initializes the service with:</p>
     * <ul>
     *   <li><strong>PasswordResetTokenRepository:</strong> For managing reset tokens</li>
     *   <li><strong>UserRepository:</strong> For user data operations</li>
     *   <li><strong>PasswordEncoder:</strong> For secure password hashing</li>
     * </ul>
     *
     * @param tokenRepository the repository for password reset token operations
     * @param userRepository the repository for user operations
     * @param passwordEncoder the encoder for secure password hashing
     * @throws IllegalArgumentException if any parameter is null
     */
    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        if (tokenRepository == null) {
            throw new IllegalArgumentException("PasswordResetTokenRepository cannot be null");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a password reset token for a user with the specified email address.
     *
     * <p>This method implements the first step of the password reset process:</p>
     * <ol>
     *   <li>Validates that a user exists with the provided email address</li>
     *   <li>Invalidates any existing unused tokens for the user (security measure)</li>
     *   <li>Generates a new cryptographically secure token</li>
     *   <li>Sets the token to expire after {@link #TOKEN_EXPIRATION_HOURS} hours</li>
     *   <li>Persists the token to the database</li>
     * </ol>
     *
     * <p><strong>Security Features:</strong></p>
     * <ul>
     *   <li>Only one active token per user at a time</li>
     *   <li>Tokens are cryptographically secure UUIDs</li>
     *   <li>Automatic expiration prevents indefinite access</li>
     *   <li>Email validation prevents user enumeration attacks</li>
     * </ul>
     *
     * @param email the email address of the user requesting password reset
     * @return an Optional containing the generated token if user exists, empty otherwise
     * @throws IllegalArgumentException if email is null or empty
     * @throws RuntimeException if database operation fails
     * @see UUID#randomUUID()
     * @see LocalDateTime#plusHours(long)
     */
    @Transactional
    public Optional<String> createPasswordResetToken(String email) {
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
     * Resets a user's password using a valid reset token.
     *
     * <p>This method implements the second step of the password reset process:</p>
     * <ol>
     *   <li>Validates the provided token exists and is valid</li>
     *   <li>Checks that the token hasn't been used or expired</li>
     *   <li>Retrieves the associated user account</li>
     *   <li>Securely hashes the new password</li>
     *   <li>Updates the user's password hash</li>
     *   <li>Marks the token as used to prevent reuse</li>
     * </ol>
     *
     * <p><strong>Security Validations:</strong></p>
     * <ul>
     *   <li>Token must exist in the system</li>
     *   <li>Token must not have been previously used</li>
     *   <li>Token must not have expired</li>
     *   <li>Associated user must still exist</li>
     * </ul>
     *
     * @param token the password reset token to use
     * @param newPassword the new password to set for the user
     * @return true if password was successfully reset, false if token is invalid
     * @throws IllegalArgumentException if token or newPassword is null or empty
     * @throws RuntimeException if database operation fails
     * @see PasswordEncoder#encode(CharSequence)
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
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
     * Validates whether a password reset token is still valid for use.
     *
     * <p>This method checks if a token can be used for password reset by verifying:</p>
     * <ul>
     *   <li>The token exists in the system</li>
     *   <li>The token has not been previously used</li>
     *   <li>The token has not expired</li>
     * </ul>
     *
     * <p>This method is useful for:</p>
     * <ul>
     *   <li>Frontend validation before allowing password reset form submission</li>
     *   <li>Checking token validity without performing the actual reset</li>
     *   <li>Providing user feedback about token status</li>
     * </ul>
     *
     * <p><strong>Note:</strong> This method performs a read-only operation and
     * does not modify any data. It's safe to call multiple times.</p>
     *
     * @param token the password reset token to validate
     * @return true if the token is valid and can be used, false otherwise
     * @throws IllegalArgumentException if token is null or empty
     * @see PasswordResetToken#isUsed()
     * @see PasswordResetToken#isExpired()
     */
    public boolean isTokenValid(String token) {
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
