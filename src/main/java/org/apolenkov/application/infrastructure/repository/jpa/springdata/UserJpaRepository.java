package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for user management.
 *
 * <p>Provides CRUD operations and authentication queries for users.</p>
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by email address.
     *
     * @param email the user's email address
     * @return user if found, empty otherwise
     */
    Optional<UserEntity> findByEmail(String email);
}
