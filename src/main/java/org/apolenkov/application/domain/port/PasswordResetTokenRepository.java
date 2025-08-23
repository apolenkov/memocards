package org.apolenkov.application.domain.port;

import java.util.Optional;
import org.apolenkov.application.model.PasswordResetToken;

/**
 * Domain port for password reset token management.
 *
 * <p>Defines the contract for managing temporary tokens used
 * in secure password reset functionality.</p>
 */
public interface PasswordResetTokenRepository {

    /**
     * Saves a password reset token.
     *
     * @param token the token to save
     * @return the saved token with generated ID
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Finds a token by its unique string value.
     *
     * @param token the token string to search for
     * @return token if found, empty otherwise
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Finds an unused token for a specific user.
     *
     * @param userId the user identifier
     * @return unused token if found, empty otherwise
     */
    Optional<PasswordResetToken> findByUserIdAndNotUsed(Long userId);

    /**
     * Removes all expired tokens from the system.
     */
    void deleteExpiredTokens();

    /**
     * Marks a specific token as used.
     *
     * @param id the token identifier to mark as used
     */
    void markAsUsed(Long id);
}
