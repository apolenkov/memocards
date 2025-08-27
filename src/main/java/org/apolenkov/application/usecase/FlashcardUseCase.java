package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
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
     * Returns flashcard by ID.
     *
     * @param id unique identifier of flashcard
     * @return Optional containing flashcard if found, empty otherwise
     */
    Optional<Flashcard> getFlashcardById(long id);

    /**
     * Saves flashcard to system (creates new or updates existing).
     *
     * @param flashcard flashcard to save
     * @return saved flashcard with generated ID if it was new
     */
    Flashcard saveFlashcard(Flashcard flashcard);

    /**
     * Deletes flashcard from system.
     *
     * @param id ID of flashcard to delete
     */
    void deleteFlashcard(Long id);

    /**
     * Returns flashcards for practice session.
     *
     * @param deckId ID of deck to practice
     * @param count number of flashcards to retrieve
     * @param random whether to randomize order of flashcards
     * @return list of flashcards prepared for practice
     */
    List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random);

    /**
     * Returns total number of flashcards in deck.
     *
     * @param deckId ID of deck to count flashcards for
     * @return total number of flashcards in specified deck
     */
    long countByDeckId(Long deckId);
}
