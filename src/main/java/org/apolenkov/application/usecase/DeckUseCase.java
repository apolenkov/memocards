package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

/**
 * Core business operations for managing flashcard decks.
 */
public interface DeckUseCase {
    /**
     * Returns all decks in system.
     *
     * @return list of all available decks
     */
    List<Deck> getAllDecks();

    /**
     * Returns decks belonging to specific user.
     *
     * @param userId ID of user whose decks to retrieve
     * @return list of decks owned by specified user
     */
    List<Deck> getDecksByUserId(Long userId);

    /**
     * Returns deck by ID.
     *
     * @param id unique identifier of deck
     * @return Optional containing deck if found, empty otherwise
     */
    Optional<Deck> getDeckById(Long id);

    /**
     * Saves deck to system (creates new or updates existing).
     *
     * @param deck deck to save
     * @return saved deck with generated ID if it was new
     */
    Deck saveDeck(Deck deck);

    /**
     * Deletes deck and all associated data.
     *
     * @param id ID of deck to delete
     */
    void deleteDeck(Long id);
}
