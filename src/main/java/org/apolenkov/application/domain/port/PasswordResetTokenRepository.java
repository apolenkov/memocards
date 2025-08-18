package org.apolenkov.application.domain.port;

import java.util.Optional;
import org.apolenkov.application.model.PasswordResetToken;

/**
 * Port for accessing password reset tokens.
 */
public interface PasswordResetTokenRepository {

    /**
     * Save password reset token
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Find token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find token by user ID and not used
     */
    Optional<PasswordResetToken> findByUserIdAndNotUsed(Long userId);

    /**
     * Delete expired tokens
     */
    void deleteExpiredTokens();

    /**
     * Mark token as used
     */
    void markAsUsed(Long id);
}
