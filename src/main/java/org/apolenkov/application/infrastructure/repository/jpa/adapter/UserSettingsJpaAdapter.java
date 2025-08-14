package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.Optional;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserSettingsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"dev", "jpa", "prod"})
public class UserSettingsJpaAdapter implements UserSettingsRepository {

    private final UserSettingsJpaRepository repo;

    public UserSettingsJpaAdapter(UserSettingsJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<String> findPreferredLocaleCode(long userId) {
        return repo.findByUserId(userId).map(UserSettingsEntity::getPreferredLocaleCode);
    }

    @Override
    public void savePreferredLocaleCode(long userId, String localeCode) {
        UserSettingsEntity e = repo.findByUserId(userId).orElseGet(UserSettingsEntity::new);
        e.setUserId(userId);
        e.setPreferredLocaleCode(localeCode);
        repo.save(e);
    }
}
