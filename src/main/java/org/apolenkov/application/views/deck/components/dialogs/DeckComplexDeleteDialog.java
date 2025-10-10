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
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for complex deck deletion with confirmation.
 * Handles deletion of decks with cards using dual-layer validation.
 *
 * <p>Features:
 * <ul>
 *   <li>Complex dialog with confirmation input for decks with cards</li>
 *   <li>Dual-layer validation: frontend for UX + backend for security</li>
 *   <li>Requires user to type deck name for confirmation</li>
 *   <li>Callback-based communication with parent components</li>
 *   <li>Audit logging for deletion actions</li>
 * </ul>
 */
public final class DeckComplexDeleteDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckComplexDeleteDialog.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Void> onDeckDeleted;

    /**
     * Creates a new DeckComplexDeleteDialog with required dependencies.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param currentDeckParam the deck to delete
     * @param onDeckDeletedParam callback executed when deck is deleted
     */
    public DeckComplexDeleteDialog(
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
     * Shows the complex deletion dialog.
     */
    public void show() {
        if (currentDeck == null) {
            LOGGER.warn("Cannot show complex delete dialog: currentDeck is null");
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
     * Creates the layout for complex deletion dialog.
     *
     * @return configured VerticalLayout
     */
    private VerticalLayout createDialogLayout() {
        VerticalLayout layout = createBaseLayout();

        layout.add(createWarningIcon());
        layout.add(createTitle());
        layout.add(createDescription());
        layout.add(createDeckInfo());

        TextField confirmInput = createConfirmationInput();
        layout.add(confirmInput);

        HorizontalLayout buttons = createButtons(confirmInput);
        layout.add(buttons);

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
     * Creates warning icon for dialog.
     *
     * @return configured Div with warning icon
     */
    private Div createWarningIcon() {
        Div warningIcon = new Div();
        warningIcon.add(VaadinIcon.WARNING.create());
        warningIcon.addClassName(DeckConstants.DECK_DELETE_CONFIRM_WARNING_ICON_CLASS);
        return warningIcon;
    }

    /**
     * Creates dialog title.
     *
     * @return configured H3 title
     */
    private H3 createTitle() {
        H3 title = new H3(getTranslation(DeckConstants.DECK_DELETE_CONFIRM_TITLE));
        title.addClassName(DeckConstants.DECK_DELETE_CONFIRM_TITLE_CLASS);
        return title;
    }

    /**
     * Creates dialog description.
     *
     * @return configured Span description
     */
    private Span createDescription() {
        Span description = new Span(getTranslation(DeckConstants.DECK_DELETE_CONFIRM_DESCRIPTION));
        description.addClassName(DeckConstants.DECK_DELETE_CONFIRM_DESCRIPTION_CLASS);
        return description;
    }

    /**
     * Creates deck information section.
     *
     * @return configured Div with deck info
     */
    private Div createDeckInfo() {
        Div deckInfoDiv = new Div();
        deckInfoDiv.addClassName(DeckConstants.DECK_DELETE_CONFIRM_INFO_CLASS);
        deckInfoDiv.addClassName(DeckConstants.GLASS_MD_CLASS);

        Span deckName = new Span(currentDeck.getTitle());
        deckName.addClassName(DeckConstants.DECK_DELETE_CONFIRM_DECK_NAME_CLASS);

        long actualCardCount = flashcardUseCase.countByDeckId(currentDeck.getId());
        Span cardCount = new Span(getTranslation(DeckConstants.DECK_DELETE_CARD_COUNT, actualCardCount));
        cardCount.addClassName(DeckConstants.DECK_DELETE_CONFIRM_CARD_COUNT_CLASS);

        deckInfoDiv.add(deckName, cardCount);
        return deckInfoDiv;
    }

    /**
     * Creates confirmation input field.
     *
     * @return configured TextField
     */
    private TextField createConfirmationInput() {
        TextField confirmInput = new TextField(getTranslation(DeckConstants.DECK_DELETE_CONFIRM_INPUT));
        confirmInput.setPlaceholder(currentDeck.getTitle());
        confirmInput.setWidthFull();
        confirmInput.setRequired(true);
        return confirmInput;
    }

    /**
     * Creates buttons for dialog with validation.
     *
     * @param confirmInput input field for validation
     * @return configured HorizontalLayout with buttons
     */
    private HorizontalLayout createButtons(final TextField confirmInput) {
        HorizontalLayout buttons = DialogHelper.createButtonLayout();

        Button confirmButton = createConfirmButton();
        Button cancelButton = createCancelButton();

        setupInputValidation(confirmInput, confirmButton);
        setupDeletionHandler(confirmButton);

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates confirm button.
     *
     * @return configured Button (initially disabled)
     */
    private Button createConfirmButton() {
        Button confirmButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_DELETE_CONFIRM),
                VaadinIcon.TRASH,
                e -> {
                    // Placeholder - will be replaced by actual click listener
                },
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        confirmButton.setEnabled(false);
        return confirmButton;
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
     * Sets up input validation for confirmation field.
     *
     * @param confirmInput input field to validate
     * @param confirmButton button to enable/disable
     */
    private void setupInputValidation(final TextField confirmInput, final Button confirmButton) {
        // Frontend validation for better UX - enables/disables button immediately
        confirmInput.addValueChangeListener(
                e -> confirmButton.setEnabled(currentDeck.getTitle().equals(e.getValue())));

        // Additional input listener for real-time feedback
        confirmInput.addInputListener(e -> {
            LOGGER.debug("Confirm input value: {}", confirmInput.getValue());
            confirmButton.setEnabled(currentDeck.getTitle().equals(confirmInput.getValue()));
        });
    }

    /**
     * Sets up deletion handler with error handling.
     *
     * @param confirmButton button to attach handler to
     */
    private void setupDeletionHandler(final Button confirmButton) {
        confirmButton.addClickListener(e -> handleDeletion());
    }

    /**
     * Handles deck deletion with validation.
     */
    private void handleDeletion() {
        try {
            performDeletion();
            logDeletionSuccess();
            handleSuccessfulDeletion();
        } catch (IllegalArgumentException ex) {
            handleValidationError();
        } catch (Exception ex) {
            handleDeletionError(ex);
        }
    }

    /**
     * Performs the actual deck deletion.
     */
    private void performDeletion() {
        deckUseCase.deleteDeck(currentDeck.getId());
    }

    /**
     * Logs successful deletion with audit information.
     */
    private void logDeletionSuccess() {
        long cardCount = flashcardUseCase.countByDeckId(currentDeck.getId());
        AUDIT_LOGGER.info(
                "User deleted deck '{}' (ID: {}) with {} cards - Complex deletion (confirmed)",
                currentDeck.getTitle(),
                currentDeck.getId(),
                cardCount);
    }

    /**
     * Handles successful deletion flow.
     */
    private void handleSuccessfulDeletion() {
        close();
        NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_DELETE_SUCCESS));
        NavigationHelper.navigateToDecks();
        notifyDeckDeleted();
    }

    /**
     * Handles validation errors from backend.
     */
    private void handleValidationError() {
        NotificationHelper.showErrorLong(getTranslation(DeckConstants.DECK_DELETE_CONFIRMATION_MISMATCH));
        LOGGER.warn("Deck deletion failed due to confirmation mismatch for deck: {}", currentDeck.getId());
    }

    /**
     * Handles general deletion errors.
     *
     * @param ex exception that occurred
     */
    private void handleDeletionError(final Exception ex) {
        NotificationHelper.showErrorLong(ex.getMessage());
        LOGGER.error("Error deleting deck: {}", currentDeck.getId(), ex);
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
