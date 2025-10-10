package org.apolenkov.application.domain.usecase;

import java.util.List;
import org.apolenkov.application.model.Flashcard;

/**
 * Core business operations for managing flashcards.
 */
public interface FlashcardUseCase {
    /**
     * Returns flashcards belonging to specific deck.
     *
     * @param deckId ID of deck to retrieve flashcards for
     * @return list of all flashcards in specified deck
     */
    List<Flashcard> getFlashcardsByDeckId(long deckId);

    /**
     * Saves flashcard to system (creates new or updates existing).
     *
     * @param flashcard flashcard to save
     */
    void saveFlashcard(Flashcard flashcard);

    /**
     * Deletes flashcard from system.
     *
     * @param id ID of flashcard to delete
     */
    void deleteFlashcard(long id);

    /**
     * Returns total number of flashcards in deck.
     *
     * @param deckId ID of deck to count flashcards for
     * @return total number of flashcards in specified deck
     */
    long countByDeckId(long deckId);
}
