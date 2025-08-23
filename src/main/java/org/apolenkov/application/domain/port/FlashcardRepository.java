package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Flashcard;

/**
 * Domain port for flashcard management operations.
 *
 * <p>Defines the contract for CRUD operations on flashcards,
 * including deck-specific queries and bulk operations.</p>
 */
public interface FlashcardRepository {

    /**
     * Finds all flashcards in a specific deck.
     *
     * @param deckId the deck identifier
     * @return list of flashcards in the deck
     */
    List<Flashcard> findByDeckId(Long deckId);

    /**
     * Finds a flashcard by its identifier.
     *
     * @param id the flashcard identifier
     * @return flashcard if found, empty otherwise
     */
    Optional<Flashcard> findById(Long id);

    /**
     * Saves a flashcard (creates new or updates existing).
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard with generated ID
     */
    Flashcard save(Flashcard flashcard);

    /**
     * Deletes a flashcard by its identifier.
     *
     * @param id the flashcard identifier to delete
     */
    void deleteById(Long id);

    /**
     * Counts the number of flashcards in a deck.
     *
     * @param deckId the deck identifier
     * @return total number of flashcards in the deck
     */
    long countByDeckId(Long deckId);

    /**
     * Deletes all flashcards in a specific deck.
     *
     * @param deckId the deck identifier
     */
    void deleteByDeckId(Long deckId);
}
