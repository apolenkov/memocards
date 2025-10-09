package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for flashcard deletion confirmation.
 * Provides a simple confirmation dialog before deleting a flashcard.
 *
 * <p>Features:
 * <ul>
 *   <li>Simple confirmation dialog</li>
 *   <li>Callback-based communication with parent components</li>
 *   <li>Proper error handling</li>
 * </ul>
 */
public final class DeckFlashcardDeleteDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckFlashcardDeleteDialog.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient Flashcard flashcard;

    // Callbacks
    private final transient Consumer<Long> onFlashcardDeleted;

    /**
     * Creates a new DeckFlashcardDeleteDialog with required dependencies.
     *
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param flashcardParam the flashcard to delete
     * @param onFlashcardDeletedParam callback executed when flashcard is deleted
     */
    public DeckFlashcardDeleteDialog(
            final FlashcardUseCase flashcardUseCaseParam,
            final Flashcard flashcardParam,
            final Consumer<Long> onFlashcardDeletedParam) {
        super();
        this.flashcardUseCase = flashcardUseCaseParam;
        this.flashcard = flashcardParam;
        this.onFlashcardDeleted = onFlashcardDeletedParam;
    }

    /**
     * Shows the flashcard deletion confirmation dialog.
     */
    public void show() {
        if (flashcard == null) {
            LOGGER.warn("Cannot show delete dialog: flashcard is null");
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
                getTranslation(DeckConstants.DECK_CARD_DELETE_CONFIRM), e -> handleFlashcardDeletion());
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
     * Handles flashcard deletion with error handling.
     */
    private void handleFlashcardDeletion() {
        try {
            flashcardUseCase.deleteFlashcard(flashcard.getId());

            // Audit log for flashcard deletion
            AUDIT_LOGGER.info(
                    "User deleted flashcard '{}' (ID: {}) from deck (ID: {})",
                    flashcard.getFrontText(),
                    flashcard.getId(),
                    flashcard.getDeckId());

            notifyFlashcardDeleted();
            close();
            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_CARD_DELETED));
            LOGGER.info("Flashcard {} deleted successfully", flashcard.getId());
        } catch (Exception ex) {
            handleDeletionError(ex);
        }
    }

    /**
     * Notifies parent component about flashcard deletion.
     */
    private void notifyFlashcardDeleted() {
        if (onFlashcardDeleted != null) {
            onFlashcardDeleted.accept(flashcard.getId());
        }
    }

    /**
     * Handles deletion errors.
     *
     * @param ex exception that occurred
     */
    private void handleDeletionError(final Exception ex) {
        LOGGER.error("Error deleting flashcard: {}", flashcard.getId(), ex);
        NotificationHelper.showErrorLong(ex.getMessage());
    }
}
