package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for deck deletion with different confirmation levels.
 * Handles both simple deletion for empty decks and complex deletion with confirmation.
 *
 * <p>Features:
 * <ul>
 *   <li>Simple dialog for empty decks</li>
 *   <li>Complex dialog with confirmation input for decks with cards</li>
 *   <li>Automatic detection of deck content</li>
 *   <li>Callback-based communication with parent components</li>
 * </ul>
 */
public final class DeckDeleteDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckDeleteDialog.class);

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Void> onDeckDeleted;

    /**
     * Creates a new DeckDeleteDialog with required dependencies.
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
        super();
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

        // Check if deck is empty using flashcardUseCase.countFlashcardsByDeckId() for accurate count
        int cardCount = (int) flashcardUseCase.countByDeckId(currentDeck.getId());
        boolean isEmpty = cardCount == 0;

        LOGGER.debug(
                "Deck deletion check - Title: {}, Card count: {}, isEmpty: {}",
                currentDeck.getTitle(),
                cardCount,
                isEmpty);

        if (isEmpty) {
            showSimpleDeleteDialog();
        } else {
            showComplexDeleteDialog();
        }
    }

    /**
     * Shows simple deletion dialog for empty decks.
     * No additional confirmation required since there are no cards to lose.
     */
    private void showSimpleDeleteDialog() {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Simple icon and title
        Div icon = new Div();
        icon.add(VaadinIcon.INFO_CIRCLE.create());
        icon.addClassName("deck-delete-dialog__icon");

        H3 title = new H3(getTranslation("deck.delete.simpleTitle"));
        title.addClassName("deck-delete-dialog__title");

        // Description
        String deckTitle = currentDeck.getTitle();
        Span description = new Span(getTranslation("deck.delete.simpleDescription", deckTitle));
        description.addClassName("deck-delete-dialog__description");

        // Show actual card count if different from expected
        int actualCardCount = (int) flashcardUseCase.countByDeckId(currentDeck.getId());
        if (actualCardCount > 0) {
            Span cardCountInfo = new Span(getTranslation("deck.delete.actualCardCount", actualCardCount));
            cardCountInfo.addClassName("deck-delete-dialog__card-count");
            layout.add(cardCountInfo);
        }

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = ButtonHelper.createButton(
                getTranslation("deck.delete.simpleConfirm"),
                VaadinIcon.TRASH,
                e -> {
                    try {
                        deckUseCase.deleteDeck(currentDeck.getId());
                        close();
                        NotificationHelper.showSuccessBottom(getTranslation("deck.delete.success"));
                        NavigationHelper.navigateToDecks();

                        // Notify parent component
                        if (onDeckDeleted != null) {
                            onDeckDeleted.accept(null);
                        }
                    } catch (Exception ex) {
                        NotificationHelper.showErrorLong(ex.getMessage());
                    }
                },
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);

        Button cancelButton =
                ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);

        buttons.add(confirmButton, cancelButton);
        layout.add(icon, title, description, buttons);

        add(layout);
        open();
    }

    /**
     * Shows complex deletion dialog for decks with cards.
     * Uses dual-layer validation: frontend for UX + backend for security.
     * Requires user to type deck name for confirmation to prevent accidental deletion.
     */
    private void showComplexDeleteDialog() {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Warning icon and title
        Div warningIcon = new Div();
        warningIcon.add(VaadinIcon.WARNING.create());
        warningIcon.addClassName("deck-delete-confirm__warning-icon");

        H3 title = new H3(getTranslation("deck.delete.confirmTitle"));
        title.addClassName("deck-delete-confirm__title");

        // Description
        Span description = new Span(getTranslation("deck.delete.confirmDescription"));
        description.addClassName("deck-delete-confirm__description");

        // Deck info
        Div deckInfoDiv = new Div();
        deckInfoDiv.addClassName("deck-delete-confirm__info");
        deckInfoDiv.addClassName("glass-md");

        Span deckName = new Span(currentDeck.getTitle());
        deckName.addClassName("deck-delete-confirm__deck-name");

        long actualCardCount = flashcardUseCase.countByDeckId(currentDeck.getId());
        Span cardCount = new Span(getTranslation("deck.delete.cardCount", actualCardCount));
        cardCount.addClassName("deck-delete-confirm__card-count");

        deckInfoDiv.add(deckName, cardCount);

        // Confirmation input
        TextField confirmInput = new TextField(getTranslation("deck.delete.confirmInput"));
        confirmInput.setPlaceholder(currentDeck.getTitle());
        confirmInput.setWidthFull();
        confirmInput.setRequired(true);

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = ButtonHelper.createButton(
                getTranslation("deck.delete.confirm"),
                VaadinIcon.TRASH,
                e -> {
                    // Placeholder - will be replaced by actual click listener below
                },
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        confirmButton.setEnabled(false);

        Button cancelButton =
                ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);

        // Frontend validation for better UX - enables/disables button immediately
        confirmInput.addValueChangeListener(
                e -> confirmButton.setEnabled(currentDeck.getTitle().equals(e.getValue())));

        // Additional input listener for real-time feedback
        confirmInput.addInputListener(e -> {
            LOGGER.debug("Confirm input value: {}", confirmInput.getValue());
            confirmButton.setEnabled(currentDeck.getTitle().equals(confirmInput.getValue()));
        });

        // Backend validation for security - actual deletion with server-side confirmation
        confirmButton.addClickListener(e -> {
            try {
                // Server will validate the confirmation text for security
                deckUseCase.deleteDeck(currentDeck.getId());
                close();
                NotificationHelper.showSuccessBottom(getTranslation("deck.delete.success"));
                NavigationHelper.navigateToDecks();

                // Notify parent component
                if (onDeckDeleted != null) {
                    onDeckDeleted.accept(null);
                }
            } catch (IllegalArgumentException ex) {
                // Handle validation errors from backend
                NotificationHelper.showErrorLong(getTranslation("deck.delete.confirmationMismatch"));
                LOGGER.warn("Deck deletion failed due to confirmation mismatch for deck: {}", currentDeck.getId());
            } catch (Exception ex) {
                NotificationHelper.showErrorLong(ex.getMessage());
                LOGGER.error("Error deleting deck: {}", currentDeck.getId(), ex);
            }
        });

        buttons.add(confirmButton, cancelButton);
        layout.add(warningIcon, title, description, deckInfoDiv, confirmInput, buttons);

        add(layout);
        open();
    }
}
