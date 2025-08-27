package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.Optional;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserSettingsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for user settings operations.
 *
 * <p>Manages user preferences including locale settings and other
 * personalized configurations. Active in dev/prod profiles only.</p>
 */
@Repository
@Profile({"dev", "prod"})
public class UserSettingsJpaAdapter implements UserSettingsRepository {

    private final UserSettingsJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repoValue the Spring Data JPA repository for user settings operations
     * @throws IllegalArgumentException if repoValue is null
     */
    public UserSettingsJpaAdapter(final UserSettingsJpaRepository repoValue) {
        if (repoValue == null) {
            throw new IllegalArgumentException("UserSettingsJpaRepository cannot be null");
        }
        this.repo = repoValue;
    }

    /**
     * Retrieves the preferred locale code for a specific user.
     *
     * @param userId the ID of the user whose locale preference to retrieve
     * @return Optional containing the preferred locale code, or empty if not set
     * @throws IllegalArgumentException if userId is invalid (≤ 0)
     */
    @Override
    public Optional<String> findPreferredLocaleCode(final long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return repo.findByUserId(userId)
                .map(UserSettingsEntity::getPreferredLocaleCode)
                .filter(code -> !code.trim().isEmpty());
    }

    /**
     * Saves the preferred locale code for a specific user.
     *
     * @param userId the ID of the user whose locale preference to save
     * @param localeCode the locale code to save (e.g., "en-US", "ru-RU")
     * @throws IllegalArgumentException if userId is invalid (≤ 0)
     */
    @Override
    public void savePreferredLocaleCode(final long userId, final String localeCode) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        // If locale code is null or empty, we'll still save it to allow clearing preferences
        final String processedLocaleCode = (localeCode != null) ? localeCode.trim() : "";

        final UserSettingsEntity entity = repo.findByUserId(userId).orElseGet(UserSettingsEntity::new);
        entity.setUserId(userId);
        entity.setPreferredLocaleCode(processedLocaleCode);
        repo.save(entity);
    }
}
