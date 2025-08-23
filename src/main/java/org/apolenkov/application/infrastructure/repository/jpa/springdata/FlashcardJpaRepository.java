package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.FlashcardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for managing flashcards.
 *
 * <p>Provides CRUD operations and queries for flashcards within decks.</p>
 */
public interface FlashcardJpaRepository extends JpaRepository<FlashcardEntity, Long> {

    /**
     * Finds all flashcards in a specific deck.
     *
     * @param deckId the deck identifier
     * @return list of flashcards in the deck
     */
    List<FlashcardEntity> findByDeckId(Long deckId);

    /**
     * Counts flashcards in a specific deck.
     *
     * @param deckId the deck identifier
     * @return number of flashcards in the deck
     */
    long countByDeckId(Long deckId);

    /**
     * Deletes all flashcards in a specific deck.
     *
     * @param deckId the deck identifier
     */
    void deleteByDeckId(Long deckId);
}
