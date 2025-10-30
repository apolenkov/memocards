package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for card deletion confirmation.
 * Provides a simple confirmation dialog before deleting a card.
 *
 * <p>Features:
 * <ul>
 *   <li>Simple confirmation dialog</li>
 *   <li>Callback-based communication with parent components</li>
 *   <li>Proper error handling</li>
 * </ul>
 */
public final class DeckCardDeleteDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckCardDeleteDialog.class);

    // Dependencies
    private final transient CardUseCase cardUseCase;
    private final transient Card card;

    // Callbacks
    private final transient Consumer<Long> onCardDeleted;

    /**
     * Creates a new DeckCardDeleteDialog with required dependencies.
     *
     * @param cardUseCaseParam use case for card operations
     * @param cardParam the card to delete
     * @param onCardDeletedParam callback executed when card is deleted
     */
    public DeckCardDeleteDialog(
            final CardUseCase cardUseCaseParam, final Card cardParam, final Consumer<Long> onCardDeletedParam) {
        super();
        this.cardUseCase = cardUseCaseParam;
        this.card = cardParam;
        this.onCardDeleted = onCardDeletedParam;
    }

    /**
     * Shows the card deletion confirmation dialog.
     */
    public void show() {
        if (card == null) {
            LOGGER.warn("Cannot show delete dialog: card is null");
            return;
        }

        addClassName(DeckConstants.DIALOG_SM_CLASS);
        VerticalLayout layout = createDialogLayout();
        add(layout);
        open();
    }

    /**
     * Creates the dialog layout with title, description and buttons.
     *
     * @return configured VerticalLayout
     */
    private VerticalLayout createDialogLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(createDialogTitle());
        layout.add(createDialogDescription());
        layout.add(createButtonLayout());
        return layout;
    }

    /**
     * Creates dialog title.
     *
     * @return configured H3 title
     */
    private H3 createDialogTitle() {
        return new H3(getTranslation(DeckConstants.DECK_CARD_DELETE_TITLE));
    }

    /**
     * Creates dialog description.
     *
     * @return configured Span description
     */
    private Span createDialogDescription() {
        return new Span(getTranslation(DeckConstants.DECK_CARD_DELETE_DESCRIPTION));
    }

    /**
     * Creates button layout with confirm and cancel buttons.
     *
     * @return configured HorizontalLayout with buttons
     */
    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttons = DialogHelper.createButtonLayout();

        Button confirmButton = createConfirmButton();
        Button cancelButton = createCancelButton();

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates confirm button with deletion logic.
     *
     * @return configured Button
     */
    private Button createConfirmButton() {
        return ButtonHelper.createConfirmButton(
                getTranslation(DeckConstants.DECK_CARD_DELETE_CONFIRM), e -> handleCardDeletion());
    }

    /**
     * Creates cancel button.
     *
     * @return configured Button
     */
    private Button createCancelButton() {
        return ButtonHelper.createCancelButton(getTranslation(DeckConstants.COMMON_CANCEL), e -> close());
    }

    /**
     * Handles card deletion with error handling.
     */
    private void handleCardDeletion() {
        try {
            cardUseCase.deleteCard(card.getId());

            notifyCardDeleted();
            close();
            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_CARD_DELETED));
            LOGGER.debug("Card {} deleted successfully", card.getId());
        } catch (Exception ex) {
            handleDeletionError(ex);
        }
    }

    /**
     * Notifies parent component about card deletion.
     */
    private void notifyCardDeleted() {
        if (onCardDeleted != null) {
            onCardDeleted.accept(card.getId());
        }
    }

    /**
     * Handles deletion errors.
     *
     * @param ex exception that occurred
     */
    private void handleDeletionError(final Exception ex) {
        LOGGER.error("Error deleting card: {}", card.getId(), ex);
        NotificationHelper.showErrorLong(ex.getMessage());
    }
}
