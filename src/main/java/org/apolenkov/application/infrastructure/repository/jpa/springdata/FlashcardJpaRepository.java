package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;

import org.apolenkov.application.infrastructure.repository.jpa.entity.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardJpaRepository extends JpaRepository<FlashcardEntity, Long> {
  List<FlashcardEntity> findByDeckId(Long deckId);
}
