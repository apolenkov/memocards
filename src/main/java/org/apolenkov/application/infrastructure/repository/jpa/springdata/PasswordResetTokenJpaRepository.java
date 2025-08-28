package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for password reset tokens.
 *
 * <p>Manages temporary tokens for secure password reset functionality.
 * Provides operations for token creation, validation, and cleanup.</p>
 */
@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Finds a token by its unique string value.
     *
     * @param token the token string to search for
     * @return token if found, empty otherwise
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Finds an unused token for a specific user.
     *
     * @param userId the user identifier
     * @return unused token if found, empty otherwise
     */
    Optional<PasswordResetTokenEntity> findByUserIdAndUsedFalse(long userId);

    /**
     * Removes all expired tokens from the database.
     *
     * @param now the current timestamp for expiration comparison
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Marks a specific token as used.
     *
     * @param id the token identifier to mark as used
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.id = :id")
    void markAsUsed(@Param("id") long id);
}
