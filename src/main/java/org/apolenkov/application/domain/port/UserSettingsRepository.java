package org.apolenkov.application.domain.port;

import java.util.Optional;

/**
 * Repository for per-user preferences (e.g., locale).
 */
public interface UserSettingsRepository {
    Optional<String> findPreferredLocaleCode(long userId);

    void savePreferredLocaleCode(long userId, String localeCode);
}


