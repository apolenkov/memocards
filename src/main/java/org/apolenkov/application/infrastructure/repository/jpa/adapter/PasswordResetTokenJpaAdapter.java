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
     * @param repository the Spring Data JPA repository for token operations
     * @throws IllegalArgumentException if repository is null
     */
    public PasswordResetTokenJpaAdapter(PasswordResetTokenJpaRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("PasswordResetTokenJpaRepository cannot be null");
        }
        this.repository = repository;
    }

    /**
     * Saves a password reset token to the database.
     *
     * @param token the password reset token to save
     * @return the saved token with updated fields
     * @throws IllegalArgumentException if token is null
     */
    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        if (token == null) {
            throw new IllegalArgumentException("PasswordResetToken cannot be null");
        }
        PasswordResetTokenEntity entity = toEntity(token);
        PasswordResetTokenEntity savedEntity = repository.save(entity);
        return toModel(savedEntity);
    }

    /**
     * Finds a password reset token by its token value.
     *
     * @param token the token string value to search for
     * @return Optional containing the token if found
     * @throws IllegalArgumentException if token is null or empty
     */
    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token value cannot be null or empty");
        }
        return repository.findByToken(token.trim()).map(this::toModel);
    }

    /**
     * Finds an unused password reset token for a specific user.
     *
     * @param userId the ID of the user to find unused tokens for
     * @return Optional containing the unused token if found
     * @throws IllegalArgumentException if userId is null
     */
    @Override
    public Optional<PasswordResetToken> findByUserIdAndNotUsed(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return repository.findByUserIdAndUsedFalse(userId).map(this::toModel);
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
     * @param id the unique identifier of the token to mark as used
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void markAsUsed(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Token ID cannot be null");
        }
        repository.markAsUsed(id);
    }

    /**
     * Converts domain model to JPA entity.
     *
     * @param token the domain model to convert
     * @return the corresponding JPA entity, or null if token is null
     */
    private PasswordResetTokenEntity toEntity(PasswordResetToken token) {
        if (token == null) return null;
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        return entity;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model, or null if entity is null
     */
    private PasswordResetToken toModel(PasswordResetTokenEntity entity) {
        if (entity == null) return null;
        PasswordResetToken token = new PasswordResetToken();
        token.setId(entity.getId());
        token.setToken(entity.getToken());
        token.setUserId(entity.getUserId());
        token.setExpiresAt(entity.getExpiresAt());
        token.setUsed(entity.isUsed());
        return token;
    }
}
