package org.apolenkov.application.domain.port;

import java.util.Optional;
import org.apolenkov.application.model.PasswordResetToken;

/**
 * Domain port for password reset token management.
 *
 * <p>Defines contract for managing temporary tokens used
 * in secure password reset functionality.</p>
 */
public interface PasswordResetTokenRepository {

    /**
     * Saves password reset token.
     *
     * @param token token to save
     * @return saved token with generated ID
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Finds token by unique string value.
     *
     * @param token token string to search for
     * @return token if found, empty otherwise
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Finds unused token for specific user.
     *
     * @param userId user identifier
     * @return unused token if found, empty otherwise
     */
    Optional<PasswordResetToken> findByUserIdAndNotUsed(Long userId);

    /**
     * Removes all expired tokens from system.
     */
    void deleteExpiredTokens();

    /**
     * Marks specific token as used.
     *
     * @param id token identifier to mark as used
     */
    void markAsUsed(Long id);
}
