package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
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

        addClassName("dialog-sm");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation("deck.card.deleteTitle")));
        layout.add(new Span(getTranslation("deck.card.deleteDescription")));

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = ButtonHelper.createConfirmButton(getTranslation("deck.card.deleteConfirm"), e -> {
            try {
                flashcardUseCase.deleteFlashcard(flashcard.getId());

                // Notify parent component
                if (onFlashcardDeleted != null) {
                    onFlashcardDeleted.accept(flashcard.getId());
                }

                close();
                NotificationHelper.showSuccessBottom(getTranslation("deck.card.deleted"));

                LOGGER.info("Flashcard {} deleted successfully", flashcard.getId());
            } catch (Exception ex) {
                LOGGER.error("Error deleting flashcard: {}", flashcard.getId(), ex);
                NotificationHelper.showErrorLong(ex.getMessage());
            }
        });

        Button cancelButton = ButtonHelper.createCancelButton(getTranslation("common.cancel"), e -> close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);
        add(layout);

        open();
    }
}
