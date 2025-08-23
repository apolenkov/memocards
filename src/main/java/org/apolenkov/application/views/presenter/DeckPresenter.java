package org.apolenkov.application.views.presenter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.service.query.CardQueryService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Component;

/**
 * Presenter for managing deck-related operations and presentation logic.
 *
 * This component coordinates between the UI layer and business services for
 * deck management, flashcard operations, and progress tracking. It provides
 * a clean interface for deck-related functionality and handles the coordination
 * of multiple services.
 */
@Component
public class DeckPresenter {

    private final DeckUseCase deckUseCase;
    private final StatsService statsService;
    private final DeckFacade deckFacade;
    private final CardQueryService cardQueryService;

    /**
     * Creates a new DeckPresenter with required dependencies.
     *
     * @param deckUseCase service for deck operations
     * @param statsService service for statistics and progress tracking
     * @param deckFacade service for deck business logic
     * @param cardQueryService service for card filtering and search
     */
    public DeckPresenter(
            DeckUseCase deckUseCase,
            StatsService statsService,
            DeckFacade deckFacade,
            CardQueryService cardQueryService) {
        this.deckUseCase = deckUseCase;
        this.statsService = statsService;
        this.deckFacade = deckFacade;
        this.cardQueryService = cardQueryService;
    }

    /**
     * Loads a deck by its ID.
     *
     * @param deckId the ID of the deck to load
     * @return an Optional containing the deck if found, empty otherwise
     */
    public Optional<Deck> loadDeck(long deckId) {
        return deckUseCase.getDeckById(deckId);
    }

    /**
     * Loads all flashcards for a specific deck.
     *
     * @param deckId the ID of the deck to load flashcards for
     * @return a list of all flashcards in the deck
     */
    public List<Flashcard> loadFlashcards(long deckId) {
        return deckFacade.loadFlashcards(deckId);
    }

    /**
     * Gets filtered flashcards based on search criteria and knowledge status.
     * Applies text-based filtering and optionally hides cards that are
     * already marked as known by the user.
     *
     * @param deckId the ID of the deck to search in
     * @param rawQuery the search query text
     * @param hideKnown whether to exclude cards marked as known
     * @return a filtered list of flashcards matching the criteria
     */
    public List<Flashcard> listFilteredFlashcards(long deckId, String rawQuery, boolean hideKnown) {
        return cardQueryService.listFilteredFlashcards(deckId, rawQuery, hideKnown);
    }

    /**
     * Checks if a specific card is marked as known in a deck.
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to check
     * @return true if the card is marked as known, false otherwise
     */
    public boolean isKnown(long deckId, long cardId) {
        return statsService.isCardKnown(deckId, cardId);
    }

    /**
     * Toggles the knowledge status of a card in a deck.
     * If the card is currently marked as known, it will be marked as unknown.
     * If it's currently unknown, it will be marked as known.
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to toggle
     */
    public void toggleKnown(long deckId, long cardId) {
        deckFacade.toggleKnown(deckId, cardId);
    }

    /**
     * Resets all progress for a specific deck.
     *
     * <p>Removes all known card entries for the specified deck, effectively
     * resetting the user's progress to zero. This is useful when a user wants
     * to start over or when deck content has been significantly changed.</p>
     *
     * @param deckId the ID of the deck to reset progress for
     */
    public void resetProgress(long deckId) {
        deckFacade.resetProgress(deckId);
    }

    /**
     * Gets the total number of cards in a deck.
     *
     * @param deckId the ID of the deck to get the size for
     * @return the total number of flashcards in the deck
     */
    public int deckSize(long deckId) {
        return deckFacade.deckSize(deckId);
    }
}
