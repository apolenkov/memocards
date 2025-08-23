package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for managing decks.
 * Provides CRUD operations and user-specific queries for flashcard decks.
 */
public interface DeckJpaRepository extends JpaRepository<DeckEntity, Long> {

    /**
     * Finds all decks owned by a specific user.
     *
     * @param userId the user identifier
     * @return list of decks belonging to the user
     */
    List<DeckEntity> findByUserId(Long userId);
}
