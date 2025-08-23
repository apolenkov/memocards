package org.apolenkov.application.domain.port;

import java.util.Optional;

/**
 * Domain port for managing user settings and preferences.
 *
 * <p>Defines contract for storing and retrieving user-specific
 * configuration such as locale preferences and other personal settings.</p>
 */
public interface UserSettingsRepository {

    /**
     * Retrieves preferred locale code for user.
     *
     * @param userId user identifier
     * @return preferred locale code if set, empty otherwise
     */
    Optional<String> findPreferredLocaleCode(long userId);

    /**
     * Saves preferred locale code for user.
     *
     * @param userId user identifier
     * @param localeCode locale code to save (e.g., "en", "ru")
     */
    void savePreferredLocaleCode(long userId, String localeCode);
}
