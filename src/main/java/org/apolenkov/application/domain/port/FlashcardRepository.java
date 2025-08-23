package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Flashcard;

/**
 * Domain port for flashcard management operations.
 *
 * <p>Defines contract for CRUD operations on flashcards,
 * including deck-specific queries and bulk operations.</p>
 */
public interface FlashcardRepository {

    /**
     * Finds all flashcards in specific deck.
     *
     * @param deckId deck identifier
     * @return list of flashcards in deck
     */
    List<Flashcard> findByDeckId(Long deckId);

    /**
     * Finds flashcard by identifier.
     *
     * @param id flashcard identifier
     * @return flashcard if found, empty otherwise
     */
    Optional<Flashcard> findById(Long id);

    /**
     * Saves flashcard (creates new or updates existing).
     *
     * @param flashcard flashcard to save
     * @return saved flashcard with generated ID
     */
    Flashcard save(Flashcard flashcard);

    /**
     * Deletes flashcard by identifier.
     *
     * @param id flashcard identifier to delete
     */
    void deleteById(Long id);

    /**
     * Counts number of flashcards in deck.
     *
     * @param deckId deck identifier
     * @return total number of flashcards in deck
     */
    long countByDeckId(Long deckId);

    /**
     * Deletes all flashcards in specific deck.
     *
     * @param deckId deck identifier
     */
    void deleteByDeckId(Long deckId);
}
