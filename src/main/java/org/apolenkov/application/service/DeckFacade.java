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
 * Coordinates between deck and flashcard use cases, providing simplified interface
 * for deck management operations including creation, deletion, and flashcard manipulation.
 */
@Component
public class DeckFacade {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;

    /**
     * Creates DeckFacade with specified use cases and services.
     * Initializes the facade with required dependencies for deck operations.
     *
     * @param deckUseCase deck use case for deck operations (non-null)
     * @param flashcardUseCase flashcard use case for flashcard operations (non-null)
     * @param statsService stats service for statistics tracking (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public DeckFacade(DeckUseCase deckUseCase, FlashcardUseCase flashcardUseCase, StatsService statsService) {
        if (deckUseCase == null || flashcardUseCase == null || statsService == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
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
     * @throws IllegalArgumentException if deckId is not positive
     */
    @Transactional(readOnly = true)
    public Deck getDeckOrThrow(long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        return deckUseCase.getDeckById(deckId).orElseThrow();
    }

    /**
     * Loads all flashcards for specific deck.
     * Returns an empty list if the deck has no flashcards.
     *
     * @param deckId ID of deck whose flashcards to load (must be positive)
     * @return list of flashcards in specified deck, never null (may be empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    @Transactional(readOnly = true)
    public List<Flashcard> loadFlashcards(long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        return flashcardUseCase.getFlashcardsByDeckId(deckId);
    }

    /**
     * Gets set of card IDs marked as known in specified deck.
     * Provides information about user's learning progress by returning
     * IDs of cards they have marked as known or learned.
     *
     * @param deckId ID of deck to check for known cards (must be positive)
     * @return set of card IDs marked as known, never null (may be empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnown(long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        return statsService.getKnownCardIds(deckId);
    }

    /**
     * Toggles known status of specific card in deck.
     * Changes learning status of card from known to unknown or vice versa.
     * Used to track user progress and adjust learning algorithms.
     *
     * @param deckId ID of deck containing the card (must be positive)
     * @param cardId ID of card to toggle (must be positive)
     * @throws IllegalArgumentException if deckId or cardId is not positive
     */
    @Transactional
    public void toggleKnown(long deckId, long cardId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        if (cardId <= 0) {
            throw new IllegalArgumentException("Card ID must be positive, got: " + cardId);
        }
        boolean known = statsService.isCardKnown(deckId, cardId);
        statsService.setCardKnown(deckId, cardId, !known);
    }

    /**
     * Resets learning progress for all cards in specific deck (irreversible operation).
     *
     * @param deckId ID of deck whose progress to reset (must be positive)
     * @throws IllegalArgumentException if deckId is not positive
     */
    @Transactional
    public void resetProgress(long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
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
     * Calculates learning progress percentage for specific deck (0-100%).
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
     * Saves flashcard to system (creates new or updates existing).
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
     * Deletes flashcard from system (irreversible operation).
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
     * Saves deck to system (creates new or updates existing).
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
     * Deletes deck and all its associated flashcards (irreversible operation).
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
