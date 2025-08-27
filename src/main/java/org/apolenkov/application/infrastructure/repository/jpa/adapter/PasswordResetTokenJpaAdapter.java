package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apolenkov.application.domain.port.PasswordResetTokenRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.PasswordResetTokenEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.PasswordResetTokenJpaRepository;
import org.apolenkov.application.model.PasswordResetToken;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for password reset token operations.
 *
 * <p>Manages secure password reset tokens with validation, expiration,
 * and usage tracking.</p>
 */
@Repository
public class PasswordResetTokenJpaAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository repository;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repositoryValue the Spring Data JPA repository for token operations
     * @throws IllegalArgumentException if repository is null
     */
    public PasswordResetTokenJpaAdapter(final PasswordResetTokenJpaRepository repositoryValue) {
        if (repositoryValue == null) {
            throw new IllegalArgumentException("PasswordResetTokenJpaRepository cannot be null");
        }
        this.repository = repositoryValue;
    }

    /**
     * Saves a password reset token to the database.
     *
     * @param tokenValue the password reset token to save
     * @throws IllegalArgumentException if token is null
     */
    @Override
    public void save(final PasswordResetToken tokenValue) {
        if (tokenValue == null) {
            throw new IllegalArgumentException("PasswordResetToken cannot be null");
        }
        final PasswordResetTokenEntity entity = toEntity(tokenValue);
        final PasswordResetTokenEntity savedEntity = repository.save(entity);
        toModel(savedEntity);
    }

    /**
     * Finds a password reset token by its token value.
     *
     * @param tokenValue the token string value to search for
     * @return Optional containing the token if found
     * @throws IllegalArgumentException if token is null or empty
     */
    @Override
    public Optional<PasswordResetToken> findByToken(final String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Token value cannot be null or empty");
        }
        return repository.findByToken(tokenValue.trim()).map(this::toModel);
    }

    /**
     * Finds an unused password reset token for a specific user.
     *
     * @param userIdValue the ID of the user to find unused tokens for
     * @return Optional containing the unused token if found
     * @throws IllegalArgumentException if userId is null
     */
    @Override
    public Optional<PasswordResetToken> findByUserIdAndNotUsed(final Long userIdValue) {
        if (userIdValue == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return repository.findByUserIdAndUsedFalse(userIdValue).map(this::toModel);
    }

    /**
     * Deletes all expired password reset tokens.
     */
    @Override
    public void deleteExpiredTokens() {
        repository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Marks a password reset token as used.
     *
     * @param idValue the unique identifier of the token to mark as used
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void markAsUsed(final Long idValue) {
        if (idValue == null) {
            throw new IllegalArgumentException("Token ID cannot be null");
        }
        repository.markAsUsed(idValue);
    }

    /**
     * Converts domain model to JPA entity.
     *
     * @param tokenValue the domain model to convert
     * @return the corresponding JPA entity, or null if token is null
     */
    private PasswordResetTokenEntity toEntity(final PasswordResetToken tokenValue) {
        if (tokenValue == null) {
            return null;
        }
        final PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(tokenValue.getId());
        entity.setToken(tokenValue.getToken());
        entity.setUserId(tokenValue.getUserId());
        entity.setExpiresAt(tokenValue.getExpiresAt());
        entity.setUsed(tokenValue.isUsed());
        return entity;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entityValue the JPA entity to convert
     * @return the corresponding domain model, or null if entity is null
     */
    private PasswordResetToken toModel(final PasswordResetTokenEntity entityValue) {
        if (entityValue == null) {
            return null;
        }
        final PasswordResetToken token = new PasswordResetToken();
        token.setId(entityValue.getId());
        token.setToken(entityValue.getToken());
        token.setUserId(entityValue.getUserId());
        token.setExpiresAt(entityValue.getExpiresAt());
        token.setUsed(entityValue.isUsed());
        return token;
    }
}
