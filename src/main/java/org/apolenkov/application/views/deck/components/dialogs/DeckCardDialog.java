package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for creating and editing cards.
 * Handles form validation, data binding, and user interactions.
 *
 * <p>Features:
 * <ul>
 *   <li>Form validation with BeanValidationBinder</li>
 *   <li>Support for both create and edit modes</li>
 *   <li>Callback-based communication with parent components</li>
 *   <li>Proper cleanup of event listeners</li>
 * </ul>
 */
public final class DeckCardDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckCardDialog.class);

    // ==================== Fields ====================

    // Dependencies
    private final transient CardUseCase cardUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Card> onCardSaved;

    // State
    private transient Card editingCard;

    // UI Components
    private FormLayout formLayout;
    private TextField frontTextField;
    private TextField backTextField;
    private TextArea exampleArea;
    private TextField imageUrlField;

    // ==================== Constructor ====================

    /**
     * Creates a new DeckCardDialog with required dependencies.
     *
     * @param cardUseCaseParam use case for card operations
     * @param currentDeckParam the deck this card belongs to
     * @param onCardSavedParam callback executed when card is saved
     */
    public DeckCardDialog(
            final CardUseCase cardUseCaseParam, final Deck currentDeckParam, final Consumer<Card> onCardSavedParam) {
        super();
        this.cardUseCase = cardUseCaseParam;
        this.currentDeck = currentDeckParam;
        this.onCardSaved = onCardSavedParam;
        addClassName(DeckConstants.DIALOG_MD_CLASS);
    }

    // ==================== Public API ====================

    /**
     * Opens the dialog for creating a new card.
     */
    public void openForCreate() {
        openDialog(null);
    }

    /**
     * Opens the dialog for editing an existing card.
     *
     * @param card the card to edit
     */
    public void openForEdit(final Card card) {
        openDialog(card);
    }

    /**
     * Creates the form layout with input fields.
     */
    private void createForm() {
        formLayout = new FormLayout();

        frontTextField = new TextField(getTranslation(DeckConstants.DECK_COL_FRONT));
        frontTextField.setWidthFull();
        frontTextField.setRequired(true);

        backTextField = new TextField(getTranslation(DeckConstants.DECK_COL_BACK));
        backTextField.setWidthFull();
        backTextField.setRequired(true);

        exampleArea = new TextArea(getTranslation(DeckConstants.DECK_EXAMPLE_OPTIONAL));
        exampleArea.setWidthFull();
        exampleArea.addClassName(DeckConstants.TEXT_AREA_SM_CLASS);

        imageUrlField = new TextField(getTranslation(DeckConstants.DECK_IMAGE_URL_OPTIONAL));
        imageUrlField.setWidthFull();

        formLayout.add(frontTextField, backTextField, exampleArea, imageUrlField);
    }

    /**
     * Opens the dialog with the specified card data.
     * Creates fresh form components each time to avoid component reuse issues.
     *
     * @param card the card to edit, or null for creating new
     */
    private void openDialog(final Card card) {
        // Store the editing card
        this.editingCard = card;

        // Create fresh form components each time
        createForm();

        // Set dialog title
        H3 title = new H3(
                card == null
                        ? getTranslation(DeckConstants.DECK_CARD_ADD_TITLE)
                        : getTranslation(DeckConstants.DECK_CARD_EDIT_TITLE));

        // Populate form with card data
        populateForm(card);

        // Create dialog layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(title, formLayout, createButtonsLayout());

        // No removeAll() needed - dialog is fresh from constructor
        add(dialogLayout);
        open();
    }

    /**
     * Populates the form with card data.
     *
     * @param card the card to populate form with, or null for new card
     */
    private void populateForm(final Card card) {
        // Ensure form components are initialized
        if (frontTextField == null || backTextField == null || exampleArea == null || imageUrlField == null) {
            LOGGER.warn("Form components not initialized yet, skipping populateForm");
            return;
        }

        if (card != null) {
            frontTextField.setValue(card.getFrontText() != null ? card.getFrontText() : "");
            backTextField.setValue(card.getBackText() != null ? card.getBackText() : "");
            exampleArea.setValue(card.getExample() != null ? card.getExample() : "");
            imageUrlField.setValue(card.getImageUrl() != null ? card.getImageUrl() : "");
        } else {
            // Clear form for new card
            frontTextField.setValue("");
            backTextField.setValue("");
            exampleArea.setValue("");
            imageUrlField.setValue("");
        }
    }

    /**
     * Handles saving the card after form validation.
     */
    private void handleSave() {
        String frontText = ValidationHelper.safeTrimToEmpty(frontTextField.getValue());
        String backText = ValidationHelper.safeTrimToEmpty(backTextField.getValue());

        if (ValidationHelper.validateRequiredSimple(
                frontTextField, frontText, getTranslation(DeckConstants.FILL_REQUIRED_KEY))) {
            return;
        }
        if (ValidationHelper.validateRequiredSimple(
                backTextField, backText, getTranslation(DeckConstants.FILL_REQUIRED_KEY))) {
            return;
        }

        try {
            Card card = prepareCard(frontText, backText);
            cardUseCase.saveCard(card);

            notifyParentAndClose(card);

        } catch (Exception ex) {
            handleSaveError(ex);
        }
    }

    /**
     * Prepares card entity with form values.
     * Values are already trimmed by caller.
     *
     * @param frontText front text value (already trimmed)
     * @param backText back text value (already trimmed)
     * @return prepared card
     */
    private Card prepareCard(final String frontText, final String backText) {
        Card card = editingCard != null ? editingCard : new Card();

        if (editingCard == null) {
            card.setDeckId(currentDeck.getId());
        }

        card.setFrontText(frontText);
        card.setBackText(backText);
        card.setExample(ValidationHelper.safeTrim(exampleArea.getValue())); // Nullable field
        card.setImageUrl(ValidationHelper.safeTrim(imageUrlField.getValue())); // Nullable field

        return card;
    }

    /**
     * Notifies parent component and closes dialog.
     *
     * @param card the saved card
     */
    private void notifyParentAndClose(final Card card) {
        if (onCardSaved != null) {
            onCardSaved.accept(card);
        }

        close();
        showSuccessMessage();
    }

    /**
     * Shows success message based on action type.
     */
    private void showSuccessMessage() {
        boolean isEditing = editingCard != null;
        String message = isEditing
                ? getTranslation(DeckConstants.DECK_CARD_UPDATED)
                : getTranslation(DeckConstants.DECK_CARD_ADDED);
        NotificationHelper.showSuccessBottom(message);
    }

    /**
     * Handles general save errors.
     *
     * @param ex exception that occurred
     */
    private void handleSaveError(final Exception ex) {
        LOGGER.error("Error saving card: {}", ex.getMessage(), ex);
        NotificationHelper.showErrorLong(ex.getMessage());
    }

    /**
     * Creates the buttons layout for the dialog.
     *
     * @return the horizontal layout containing buttons
     */
    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = DialogHelper.createButtonLayout();

        Button saveButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.DIALOG_SAVE),
                VaadinIcon.CHECK,
                e -> handleSave(),
                ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_CANCEL), e -> close(), ButtonVariant.LUMO_TERTIARY);

        buttonsLayout.add(saveButton, cancelButton);
        return buttonsLayout;
    }
}
