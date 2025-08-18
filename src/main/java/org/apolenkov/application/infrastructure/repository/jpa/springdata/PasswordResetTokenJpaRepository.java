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
 * JPA repository for password reset tokens
 */
@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Find token by token string
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Find token by user ID and not used
     */
    Optional<PasswordResetTokenEntity> findByUserIdAndUsedFalse(Long userId);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Mark token as used
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.id = :id")
    void markAsUsed(@Param("id") Long id);
}
