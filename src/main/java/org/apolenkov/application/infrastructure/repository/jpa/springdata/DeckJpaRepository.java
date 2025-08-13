package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckJpaRepository extends JpaRepository<DeckEntity, Long> {
    List<DeckEntity> findByUserId(Long userId);
}
