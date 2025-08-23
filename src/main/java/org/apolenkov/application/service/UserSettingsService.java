package org.apolenkov.application.service;

import java.util.Locale;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing user-specific settings and preferences.
 *
 * <p>This service provides functionality for storing and retrieving user preferences
 * such as language settings, UI preferences, and other personalized configurations.
 * It acts as a bridge between the application logic and the user settings persistence layer.</p>
 *
 * <p>The service supports:</p>
 * <ul>
 *   <li><strong>Language Preferences:</strong> Storing user's preferred locale for internationalization</li>
 *   <li><strong>UI Settings:</strong> User interface customization preferences</li>
 *   <li><strong>Application Behavior:</strong> Personalized application behavior settings</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Set user's preferred language
 * userSettingsService.setPreferredLocale(userId, Locale.ENGLISH);
 *
 * // Set user's preferred language with country
 * userSettingsService.setPreferredLocale(userId, Locale.US);
 * }</pre>
 *
 * @see UserSettingsRepository
 * @see Locale
 * @see java.util.Locale#toLanguageTag()
 */
@Service
public class UserSettingsService {

    private final UserSettingsRepository repository;

    /**
     * Constructs a new UserSettingsService with the required repository dependency.
     *
     * <p>This constructor initializes the service with a repository for
     * persisting and retrieving user settings data.</p>
     *
     * @param repository the repository for user settings operations
     * @throws IllegalArgumentException if repository is null
     */
    public UserSettingsService(UserSettingsRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("UserSettingsRepository cannot be null");
        }
        this.repository = repository;
    }

    /**
     * Sets the preferred locale for a specific user.
     *
     * <p>This method allows users to customize their language preferences,
     * which affects the internationalization of the user interface. The locale
     * is converted to a language tag string for storage in the database.</p>
     *
     * <p>The locale setting influences:</p>
     * <ul>
     *   <li><strong>UI Language:</strong> Text displayed in the user's preferred language</li>
     *   <li><strong>Date/Time Format:</strong> Regional formatting for dates and times</li>
     *   <li><strong>Number Format:</strong> Regional formatting for numbers and currencies</li>
     *   <li><strong>Content Localization:</strong> Region-specific content and features</li>
     * </ul>
     *
     * <p><strong>Note:</strong> The locale is converted to a language tag string
     * (e.g., "en-US", "ru-RU") for consistent storage and retrieval.</p>
     *
     * @param userId the ID of the user whose locale preference to set
     * @param locale the preferred locale for the user
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
