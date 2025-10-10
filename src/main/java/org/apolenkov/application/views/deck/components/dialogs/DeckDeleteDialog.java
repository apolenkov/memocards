package org.apolenkov.application.views.deck.components.dialogs;

import java.util.function.Consumer;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory component for deck deletion dialogs.
 * Automatically determines the appropriate deletion dialog based on deck content.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic detection of deck content (empty vs with cards)</li>
 *   <li>Delegates to appropriate specialized dialog</li>
 *   <li>Simple factory pattern implementation</li>
 *   <li>Maintains backward compatibility</li>
 * </ul>
 */
public final class DeckDeleteDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckDeleteDialog.class);

    // Dependencies
    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final Deck currentDeck;

    // Callbacks
    private final Consumer<Void> onDeckDeleted;

    /**
     * Creates a new DeckDeleteDialog factory with required dependencies.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param currentDeckParam the deck to delete
     * @param onDeckDeletedParam callback executed when deck is deleted
     */
    public DeckDeleteDialog(
            final DeckUseCase deckUseCaseParam,
            final FlashcardUseCase flashcardUseCaseParam,
            final Deck currentDeckParam,
            final Consumer<Void> onDeckDeletedParam) {
        this.deckUseCase = deckUseCaseParam;
        this.flashcardUseCase = flashcardUseCaseParam;
        this.currentDeck = currentDeckParam;
        this.onDeckDeleted = onDeckDeletedParam;
    }

    /**
     * Shows the appropriate deletion dialog based on deck content.
     * Automatically detects if deck is empty and shows simple or complex dialog.
     */
    public void show() {
        if (currentDeck == null) {
            LOGGER.warn("Cannot show delete dialog: currentDeck is null");
            return;
        }

        // Check if deck is empty using flashcardUseCase.countByDeckId() for accurate count
        int cardCount = (int) flashcardUseCase.countByDeckId(currentDeck.getId());
        boolean isEmpty = cardCount == 0;

        LOGGER.debug(
                "Deck deletion check - Title: {}, Card count: {}, isEmpty: {}",
                currentDeck.getTitle(),
                cardCount,
                isEmpty);

        if (isEmpty) {
            showSimpleDialog();
        } else {
            showComplexDialog();
        }
    }

    /**
     * Shows simple deletion dialog for empty decks.
     */
    private void showSimpleDialog() {
        DeckSimpleDeleteDialog dialog = new DeckSimpleDeleteDialog(deckUseCase, currentDeck, onDeckDeleted);
        dialog.show();
    }

    /**
     * Shows complex deletion dialog for decks with cards.
     */
    private void showComplexDialog() {
        DeckComplexDeleteDialog dialog =
                new DeckComplexDeleteDialog(deckUseCase, flashcardUseCase, currentDeck, onDeckDeleted);
        dialog.show();
    }
}
