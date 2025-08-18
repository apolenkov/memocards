package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apolenkov.application.domain.port.PasswordResetTokenRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.PasswordResetTokenEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.PasswordResetTokenJpaRepository;
import org.apolenkov.application.model.PasswordResetToken;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for password reset tokens
 */
@Repository
public class PasswordResetTokenJpaAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository repository;

    public PasswordResetTokenJpaAdapter(PasswordResetTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = toEntity(token);
        PasswordResetTokenEntity savedEntity = repository.save(entity);
        return toModel(savedEntity);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token).map(this::toModel);
    }

    @Override
    public Optional<PasswordResetToken> findByUserIdAndNotUsed(Long userId) {
        return repository.findByUserIdAndUsedFalse(userId).map(this::toModel);
    }

    @Override
    public void deleteExpiredTokens() {
        repository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Override
    public void markAsUsed(Long id) {
        repository.markAsUsed(id);
    }

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
