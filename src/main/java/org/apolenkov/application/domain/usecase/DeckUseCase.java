package org.apolenkov.application.domain.usecase;

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
    List<Deck> getDecksByUserId(long userId);

    /**
     * Searches decks belonging to specific user by search query.
     * Performs case-insensitive search in title and description fields.
     *
     * @param userId ID of user whose decks to search
     * @param searchQuery search query (case-insensitive)
     * @return list of decks matching search criteria
     */
    List<Deck> searchDecksByUserId(long userId, String searchQuery);

    /**
     * Returns deck by ID.
     *
     * @param id unique identifier of deck
     * @return Optional containing deck if found, empty otherwise
     */
    Optional<Deck> getDeckById(long id);

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
    void deleteDeck(long id);
}
