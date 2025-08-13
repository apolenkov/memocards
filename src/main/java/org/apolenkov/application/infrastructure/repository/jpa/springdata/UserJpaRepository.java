package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
