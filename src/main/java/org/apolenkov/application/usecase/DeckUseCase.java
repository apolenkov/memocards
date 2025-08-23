package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

/**
 * Use case interface for deck management operations.
 *
 * <p>Defines core business operations for managing flashcard decks.
 * Provides clean abstraction layer between application services and
 * underlying data access layer.</p>
 *
 * <p>Key operations:</p>
 * <ul>
 *   <li>Retrieving decks by various criteria</li>
 *   <li>Creating and updating deck information</li>
 *   <li>Deleting decks and their associated data</li>
 *   <li>User-specific deck management</li>
 * </ul>
 */
public interface DeckUseCase {
    /**
     * Retrieves all decks in system.
     *
     * @return list of all available decks
     */
    List<Deck> getAllDecks();

    /**
     * Retrieves all decks belonging to specific user.
     *
     * @param userId ID of user whose decks to retrieve
     * @return list of decks owned by specified user
     */
    List<Deck> getDecksByUserId(Long userId);

    /**
     * Retrieves specific deck by ID.
     *
     * @param id unique identifier of deck
     * @return Optional containing deck if found, empty otherwise
     */
    Optional<Deck> getDeckById(Long id);

    /**
     * Saves deck to system.
     *
     * <p>If deck has ID, it will be updated. If no ID is present,
     * new deck will be created.</p>
     *
     * @param deck deck to save
     * @return saved deck with generated ID if it was new
     */
    Deck saveDeck(Deck deck);

    /**
     * Deletes deck and all its associated data.
     *
     * @param id ID of deck to delete
     */
    void deleteDeck(Long id);
}
