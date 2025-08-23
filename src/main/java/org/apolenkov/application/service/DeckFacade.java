package org.apolenkov.application.service;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade service for deck-related operations.
 *
 * <p>This facade coordinates between deck and flashcard use cases,
 * providing a simplified interface for deck management operations
 * including creation, deletion, and flashcard manipulation.</p>
 *
 */
@Component
public class DeckFacade {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;

    /**
     * Constructs a new DeckFacade with the specified use cases and services.
     *
     * @param deckUseCase the deck use case for deck operations
     * @param flashcardUseCase the flashcard use case for flashcard operations
     * @param statsService the stats service for statistics tracking
     */
    public DeckFacade(DeckUseCase deckUseCase, FlashcardUseCase flashcardUseCase, StatsService statsService) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
    }

    /**
     * Retrieves a deck by its ID or throws an exception if not found.
     *
     * @param deckId the ID of the deck to retrieve
     * @return the deck with the specified ID
     * @throws java.util.NoSuchElementException if no deck is found with the given ID
     */
    @Transactional(readOnly = true)
    public Deck getDeckOrThrow(long deckId) {
        return deckUseCase.getDeckById(deckId).orElseThrow();
    }

    /**
     * Loads all flashcards for a specific deck.
     *
     * <p>This method retrieves all flashcards associated with the specified deck.
     * It uses a read-only transaction for optimal performance when loading data.</p>
     *
     * @param deckId the ID of the deck whose flashcards to load
     * @return a list of flashcards in the specified deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public List<Flashcard> loadFlashcards(long deckId) {
        return flashcardUseCase.getFlashcardsByDeckId(deckId);
    }

    /**
     * Retrieves the set of card IDs that are marked as known in the specified deck.
     *
     * <p>This method provides information about the user's learning progress
     * by returning the IDs of cards they have marked as known or learned.</p>
     *
     * @param deckId the ID of the deck to check for known cards
     * @return a set of card IDs that are marked as known
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnown(long deckId) {
        return statsService.getKnownCardIds(deckId);
    }

    /**
     * Toggles the known status of a specific card in a deck.
     *
     * <p>This method changes the learning status of a card from known to unknown
     * or vice versa. It's commonly used in flashcard applications to track
     * user progress and adjust learning algorithms.</p>
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to toggle
     * @throws IllegalArgumentException if deckId or cardId is invalid
     */
    @Transactional
    public void toggleKnown(long deckId, long cardId) {
        boolean known = statsService.isCardKnown(deckId, cardId);
        statsService.setCardKnown(deckId, cardId, !known);
    }

    /**
     * Resets the learning progress for all cards in a specific deck.
     *
     * <p>This method clears all progress tracking data for the specified deck,
     * effectively resetting the user's learning state back to the beginning.
     * This is useful for users who want to start over or for testing purposes.</p>
     *
     * <p><strong>Warning:</strong> This operation is irreversible and will
     * permanently delete all progress data for the deck.</p>
     *
     * @param deckId the ID of the deck whose progress to reset
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional
    public void resetProgress(long deckId) {
        statsService.resetDeckProgress(deckId);
    }

    /**
     * Gets the total number of flashcards in a specific deck.
     *
     * <p>This method provides a convenient way to determine the size of a deck
     * without loading all the flashcard data into memory.</p>
     *
     * @param deckId the ID of the deck to check
     * @return the number of flashcards in the deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public int deckSize(long deckId) {
        return loadFlashcards(deckId).size();
    }

    /**
     * Calculates the learning progress percentage for a specific deck.
     *
     * <p>This method computes the percentage of cards in the deck that have
     * been marked as known, providing a measure of the user's learning progress.</p>
     *
     * <p>The progress is calculated as: (known cards / total cards) * 100</p>
     *
     * @param deckId the ID of the deck to calculate progress for
     * @return the progress percentage (0-100)
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public int progressPercent(long deckId) {
        return statsService.getDeckProgressPercent(deckId, deckSize(deckId));
    }

    /**
     * Saves a flashcard to the system.
     *
     * <p>This method persists a flashcard, either creating a new one or
     * updating an existing one. It handles both insert and update operations
     * based on whether the flashcard has an existing ID.</p>
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard with updated fields (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if flashcard is null or invalid
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardUseCase.saveFlashcard(flashcard);
    }

    /**
     * Deletes a flashcard from the system.
     *
     * <p>This method permanently removes a flashcard from the system.
     * The operation is irreversible and will also remove any associated
     * progress tracking data.</p>
     *
     * @param id the ID of the flashcard to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if database operation fails or flashcard doesn't exist
     */
    @Transactional
    public void deleteFlashcard(Long id) {
        flashcardUseCase.deleteFlashcard(id);
    }

    /**
     * Saves a deck to the system.
     *
     * <p>This method persists a deck, either creating a new one or
     * updating an existing one. It handles both insert and update operations
     * based on whether the deck has an existing ID.</p>
     *
     * @param deck the deck to save
     * @return the saved deck with updated fields (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if deck is null or invalid
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public Deck saveDeck(Deck deck) {
        return deckUseCase.saveDeck(deck);
    }

    /**
     * Deletes a deck and all its associated flashcards.
     *
     * <p>This method permanently removes a deck and all flashcards that belong
     * to it. The operation is irreversible and will also remove any associated
     * progress tracking data.</p>
     *
     * <p><strong>Warning:</strong> This operation deletes all data associated
     * with the deck, including flashcards and progress statistics.</p>
     *
     * @param deckId the ID of the deck to delete
     * @throws IllegalArgumentException if deckId is null
     * @throws RuntimeException if database operation fails or deck doesn't exist
     */
    @Transactional
    public void deleteDeck(Long deckId) {
        deckUseCase.deleteDeck(deckId);
    }

    /**
     * Safely deletes a deck with confirmation text validation.
     *
     * @param deckId the ID of the deck to delete
     * @param confirmationText the text that must match the deck title
     * @throws IllegalArgumentException if confirmation text doesn't match deck title
     * @throws java.util.NoSuchElementException if deck is not found
     */
    @Transactional
    public void deleteDeckWithConfirmation(Long deckId, String confirmationText) {
        // Get deck and validate it exists
        Deck deck = getDeckOrThrow(deckId);

        // Server-side validation for security - cannot be bypassed by frontend manipulation
        if (confirmationText == null || !deck.getTitle().equals(confirmationText.trim())) {
            throw new IllegalArgumentException("Confirmation text does not match deck title");
        }

        // Proceed with deletion if validation passes
        deleteDeck(deckId);
    }
}
