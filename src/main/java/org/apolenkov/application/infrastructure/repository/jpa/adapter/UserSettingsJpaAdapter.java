package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserSettingsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
     * @param repo the Spring Data JPA repository for user settings operations
     * @throws IllegalArgumentException if repo is null
     */
    public UserSettingsJpaAdapter(UserSettingsJpaRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("UserSettingsJpaRepository cannot be null");
        }
        this.repo = repo;
    }

    /**
     * Retrieves the preferred locale code for a specific user.
     *
     * @param userId the ID of the user whose locale preference to retrieve
     * @return Optional containing the preferred locale code, or empty if not set
     * @throws IllegalArgumentException if userId is invalid (≤ 0)
     */
    @Override
    public Optional<String> findPreferredLocaleCode(long userId) {
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
    public void savePreferredLocaleCode(long userId, String localeCode) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        // If locale code is null or empty, we'll still save it to allow clearing preferences
        String processedLocaleCode = (localeCode != null) ? localeCode.trim() : "";

        UserSettingsEntity e = repo.findByUserId(userId).orElseGet(UserSettingsEntity::new);
        e.setUserId(userId);
        e.setPreferredLocaleCode(processedLocaleCode);
        repo.save(e);
    }
}
