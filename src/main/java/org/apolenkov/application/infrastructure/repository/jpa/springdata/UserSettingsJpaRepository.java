package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsJpaRepository extends JpaRepository<UserSettingsEntity, Long> {
    Optional<UserSettingsEntity> findByUserId(Long userId);
}
