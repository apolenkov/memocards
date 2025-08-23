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
 * <p>Coordinates between deck and flashcard use cases, providing simplified interface
 * for deck management operations including creation, deletion, and flashcard manipulation.</p>
 */
@Component
public class DeckFacade {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;

    /**
     * Creates DeckFacade with specified use cases and services.
     *
     * @param deckUseCase deck use case for deck operations
     * @param flashcardUseCase flashcard use case for flashcard operations
     * @param statsService stats service for statistics tracking
     */
    public DeckFacade(DeckUseCase deckUseCase, FlashcardUseCase flashcardUseCase, StatsService statsService) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
    }

    /**
     * Gets deck by ID or throws exception if not found.
     *
     * @param deckId ID of deck to retrieve
     * @return deck with specified ID
     * @throws java.util.NoSuchElementException if no deck found with given ID
     */
    @Transactional(readOnly = true)
    public Deck getDeckOrThrow(long deckId) {
        return deckUseCase.getDeckById(deckId).orElseThrow();
    }

    /**
     * Loads all flashcards for specific deck.
     *
     * @param deckId ID of deck whose flashcards to load
     * @return list of flashcards in specified deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public List<Flashcard> loadFlashcards(long deckId) {
        return flashcardUseCase.getFlashcardsByDeckId(deckId);
    }

    /**
     * Gets set of card IDs marked as known in specified deck.
     *
     * <p>Provides information about user's learning progress by returning
     * IDs of cards they have marked as known or learned.</p>
     *
     * @param deckId ID of deck to check for known cards
     * @return set of card IDs marked as known
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnown(long deckId) {
        return statsService.getKnownCardIds(deckId);
    }

    /**
     * Toggles known status of specific card in deck.
     *
     * <p>Changes learning status of card from known to unknown or vice versa.
     * Used to track user progress and adjust learning algorithms.</p>
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to toggle
     * @throws IllegalArgumentException if deckId or cardId is invalid
     */
    @Transactional
    public void toggleKnown(long deckId, long cardId) {
        boolean known = statsService.isCardKnown(deckId, cardId);
        statsService.setCardKnown(deckId, cardId, !known);
    }

    /**
     * Resets learning progress for all cards in specific deck.
     *
     * <p>Clears all progress tracking data for specified deck, resetting
     * user's learning state back to beginning.</p>
     *
     * <p><strong>Warning:</strong> This operation is irreversible and will
     * permanently delete all progress data for the deck.</p>
     *
     * @param deckId ID of deck whose progress to reset
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional
    public void resetProgress(long deckId) {
        statsService.resetDeckProgress(deckId);
    }

    /**
     * Gets total number of flashcards in specific deck.
     *
     * @param deckId ID of deck to check
     * @return number of flashcards in deck
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public int deckSize(long deckId) {
        return loadFlashcards(deckId).size();
    }

    /**
     * Calculates learning progress percentage for specific deck.
     *
     * <p>Computes percentage of cards in deck marked as known,
     * providing measure of user's learning progress.</p>
     *
     * @param deckId ID of deck to calculate progress for
     * @return progress percentage (0-100)
     * @throws IllegalArgumentException if deckId is invalid
     */
    @Transactional(readOnly = true)
    public int progressPercent(long deckId) {
        return statsService.getDeckProgressPercent(deckId, deckSize(deckId));
    }

    /**
     * Saves flashcard to system.
     *
     * <p>Persists flashcard, either creating new one or updating existing one.
     * Handles both insert and update operations based on whether flashcard has existing ID.</p>
     *
     * @param flashcard flashcard to save
     * @return saved flashcard with updated fields (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if flashcard is null or invalid
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardUseCase.saveFlashcard(flashcard);
    }

    /**
     * Deletes flashcard from system.
     *
     * <p>Permanently removes flashcard from system. Operation is irreversible
     * and will also remove any associated progress tracking data.</p>
     *
     * @param id ID of flashcard to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if database operation fails or flashcard doesn't exist
     */
    @Transactional
    public void deleteFlashcard(Long id) {
        flashcardUseCase.deleteFlashcard(id);
    }

    /**
     * Saves deck to system.
     *
     * <p>Persists deck, either creating new one or updating existing one.
     * Handles both insert and update operations based on whether deck has existing ID.</p>
     *
     * @param deck deck to save
     * @return saved deck with updated fields (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if deck is null or invalid
     * @throws RuntimeException if database operation fails
     */
    @Transactional
    public Deck saveDeck(Deck deck) {
        return deckUseCase.saveDeck(deck);
    }

    /**
     * Deletes deck and all its associated flashcards.
     *
     * <p>Permanently removes deck and all flashcards that belong to it.
     * Operation is irreversible and will also remove any associated progress tracking data.</p>
     *
     * @param deckId ID of deck to delete
     * @throws IllegalArgumentException if deckId is null
     * @throws RuntimeException if database operation fails or deck doesn't exist
     */
    @Transactional
    public void deleteDeck(Long deckId) {
        deckUseCase.deleteDeck(deckId);
    }

    /**
     * Safely deletes deck with confirmation text validation.
     *
     * @param deckId ID of deck to delete
     * @param confirmationText text that must match deck title
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
