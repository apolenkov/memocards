package org.apolenkov.application.domain.port;

import java.util.Optional;

/**
 * Domain port for managing user settings and preferences.
 *
 * <p>Defines the contract for storing and retrieving user-specific
 * configuration such as locale preferences and other personal settings.</p>
 */
public interface UserSettingsRepository {

    /**
     * Retrieves the preferred locale code for a user.
     *
     * @param userId the user identifier
     * @return preferred locale code if set, empty otherwise
     */
    Optional<String> findPreferredLocaleCode(long userId);

    /**
     * Saves the preferred locale code for a user.
     *
     * @param userId the user identifier
     * @param localeCode the locale code to save (e.g., "en", "ru")
     */
    void savePreferredLocaleCode(long userId, String localeCode);
}
