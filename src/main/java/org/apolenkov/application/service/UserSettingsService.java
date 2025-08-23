package org.apolenkov.application.service;

import java.util.Locale;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing user-specific settings and preferences.
 * Provides functionality for storing and retrieving user preferences.
 */
@Service
public class UserSettingsService {

    private final UserSettingsRepository repository;

    /**
     * Creates UserSettingsService with required repository dependency.
     *
     * @param repository repository for user settings operations
     * @throws IllegalArgumentException if repository is null
     */
    public UserSettingsService(UserSettingsRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("UserSettingsRepository cannot be null");
        }
        this.repository = repository;
    }

    /**
     * Sets preferred locale for specific user.
     * Allows users to customize their language preferences, which affects
     * internationalization of user interface. Locale is converted to language
     * tag string for storage in database.
     *
     * @param userId ID of user whose locale preference to set
     * @param locale preferred locale for user
     * @throws IllegalArgumentException if userId is invalid or locale is null
     * @throws RuntimeException if database operation fails
     * @see Locale#toLanguageTag()
     * @see UserSettingsRepository#savePreferredLocaleCode(long, String)
     */
    public void setPreferredLocale(long userId, Locale locale) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }

        repository.savePreferredLocaleCode(userId, locale.toLanguageTag());
    }
}
