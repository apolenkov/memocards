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
import java.util.function.Consumer;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for simple deck deletion of empty decks.
 * Provides a straightforward confirmation dialog without additional validation.
 *
 * <p>Features:
 * <ul>
 *   <li>Simple confirmation dialog for empty decks</li>
 *   <li>No additional validation required</li>
 *   <li>Callback-based communication with parent components</li>
 *   <li>Audit logging for deletion actions</li>
 * </ul>
 */
public final class DeckSimpleDeleteDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckSimpleDeleteDialog.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Void> onDeckDeleted;

    /**
     * Creates a new DeckSimpleDeleteDialog with required dependencies.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param currentDeckParam the deck to delete
     * @param onDeckDeletedParam callback executed when deck is deleted
     */
    public DeckSimpleDeleteDialog(
            final DeckUseCase deckUseCaseParam, final Deck currentDeckParam, final Consumer<Void> onDeckDeletedParam) {
        super();
        this.deckUseCase = deckUseCaseParam;
        this.currentDeck = currentDeckParam;
        this.onDeckDeleted = onDeckDeletedParam;
    }

    /**
     * Shows the simple deletion dialog.
     */
    public void show() {
        if (currentDeck == null) {
            LOGGER.warn("Cannot show simple delete dialog: currentDeck is null");
            return;
        }

        configureDialog();
        VerticalLayout layout = createDialogLayout();
        add(layout);
        open();
    }

    /**
     * Configures basic dialog properties.
     */
    private void configureDialog() {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    /**
     * Creates the layout for simple deletion dialog.
     *
     * @return configured VerticalLayout
     */
    private VerticalLayout createDialogLayout() {
        VerticalLayout layout = createBaseLayout();

        layout.add(createIcon());
        layout.add(createTitle());
        layout.add(createDescription());
        layout.add(createButtons());

        return layout;
    }

    /**
     * Creates base layout with common styling.
     *
     * @return configured VerticalLayout
     */
    private VerticalLayout createBaseLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        return layout;
    }

    /**
     * Creates dialog icon.
     *
     * @return configured Div with icon
     */
    private Div createIcon() {
        Div icon = new Div();
        icon.add(VaadinIcon.INFO_CIRCLE.create());
        icon.addClassName(DeckConstants.DECK_DELETE_DIALOG_ICON_CLASS);
        return icon;
    }

    /**
     * Creates dialog title.
     *
     * @return configured H3 title
     */
    private H3 createTitle() {
        H3 title = new H3(getTranslation(DeckConstants.DECK_DELETE_SIMPLE_TITLE));
        title.addClassName(DeckConstants.DECK_DELETE_DIALOG_TITLE_CLASS);
        return title;
    }

    /**
     * Creates dialog description.
     *
     * @return configured Span description
     */
    private Span createDescription() {
        String deckTitle = currentDeck.getTitle();
        Span description = new Span(getTranslation(DeckConstants.DECK_DELETE_SIMPLE_DESCRIPTION, deckTitle));
        description.addClassName(DeckConstants.DECK_DELETE_DIALOG_DESCRIPTION_CLASS);
        return description;
    }

    /**
     * Creates buttons for dialog.
     *
     * @return configured HorizontalLayout with buttons
     */
    private HorizontalLayout createButtons() {
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
        return ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_DELETE_SIMPLE_CONFIRM),
                VaadinIcon.TRASH,
                e -> handleDeletion(),
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
    }

    /**
     * Creates cancel button.
     *
     * @return configured Button
     */
    private Button createCancelButton() {
        return ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_CANCEL), e -> close(), ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Handles deck deletion logic.
     */
    private void handleDeletion() {
        try {
            deckUseCase.deleteDeck(currentDeck.getId());

            // Audit log for simple deck deletion
            AUDIT_LOGGER.info(
                    "User deleted empty deck '{}' (ID: {}) - Simple deletion (no cards)",
                    currentDeck.getTitle(),
                    currentDeck.getId());

            close();
            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_DELETE_SUCCESS));
            NavigationHelper.navigateToDecks();
            notifyDeckDeleted();
        } catch (Exception ex) {
            LOGGER.error("Error deleting deck ID {}: {}", currentDeck.getId(), ex.getMessage(), ex);
            NotificationHelper.showErrorLong(ex.getMessage());
        }
    }

    /**
     * Notifies parent component about deck deletion.
     */
    private void notifyDeckDeleted() {
        if (onDeckDeleted != null) {
            onDeckDeleted.accept(null);
        }
    }
}
