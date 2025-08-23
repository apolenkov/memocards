package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for user settings.
 *
 * <p>Manages user preferences and configuration with one-to-one user relationship.</p>
 */
public interface UserSettingsJpaRepository extends JpaRepository<UserSettingsEntity, Long> {

    /**
     * Finds settings for a specific user.
     *
     * @param userId the user identifier
     * @return user settings if found, empty otherwise
     */
    Optional<UserSettingsEntity> findByUserId(Long userId);
}
