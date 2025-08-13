package org.apolenkov.application.infrastructure.repository.memory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.springframework.stereotype.Repository;

@org.springframework.context.annotation.Profile("memory")
@Repository
public class InMemoryUserSettingsRepository implements UserSettingsRepository {

    private final Map<Long, String> userIdToLocale = new ConcurrentHashMap<>();

    @Override
    public Optional<String> findPreferredLocaleCode(long userId) {
        return Optional.ofNullable(userIdToLocale.get(userId));
    }

    @Override
    public void savePreferredLocaleCode(long userId, String localeCode) {
        userIdToLocale.put(userId, localeCode);
    }
}
