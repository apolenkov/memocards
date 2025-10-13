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
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for creating and editing flashcards.
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
public final class DeckFlashcardDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckFlashcardDialog.class);

    // ==================== Fields ====================

    // Dependencies
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Flashcard> onFlashcardSaved;

    // State
    private transient Flashcard editingFlashcard;

    // UI Components
    private FormLayout formLayout;
    private TextField frontTextField;
    private TextField backTextField;
    private TextArea exampleArea;
    private TextField imageUrlField;

    // ==================== Constructor ====================

    /**
     * Creates a new DeckFlashcardDialog with required dependencies.
     *
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param currentDeckParam the deck this flashcard belongs to
     * @param onFlashcardSavedParam callback executed when flashcard is saved
     */
    public DeckFlashcardDialog(
            final FlashcardUseCase flashcardUseCaseParam,
            final Deck currentDeckParam,
            final Consumer<Flashcard> onFlashcardSavedParam) {
        super();
        this.flashcardUseCase = flashcardUseCaseParam;
        this.currentDeck = currentDeckParam;
        this.onFlashcardSaved = onFlashcardSavedParam;
        addClassName(DeckConstants.DIALOG_MD_CLASS);
    }

    // ==================== Public API ====================

    /**
     * Opens the dialog for creating a new flashcard.
     */
    public void openForCreate() {
        openDialog(null);
    }

    /**
     * Opens the dialog for editing an existing flashcard.
     *
     * @param flashcard the flashcard to edit
     */
    public void openForEdit(final Flashcard flashcard) {
        openDialog(flashcard);
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
     * Opens the dialog with the specified flashcard data.
     * Creates fresh form components each time to avoid component reuse issues.
     *
     * @param flashcard the flashcard to edit, or null for creating new
     */
    private void openDialog(final Flashcard flashcard) {
        // Store the editing flashcard
        this.editingFlashcard = flashcard;

        // Create fresh form components each time
        createForm();

        // Set dialog title
        H3 title = new H3(
                flashcard == null
                        ? getTranslation(DeckConstants.DECK_CARD_ADD_TITLE)
                        : getTranslation(DeckConstants.DECK_CARD_EDIT_TITLE));

        // Populate form with flashcard data
        populateForm(flashcard);

        // Create dialog layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(title, formLayout, createButtonsLayout());

        // No removeAll() needed - dialog is fresh from constructor
        add(dialogLayout);
        open();
    }

    /**
     * Populates the form with flashcard data.
     *
     * @param flashcard the flashcard to populate form with, or null for new card
     */
    private void populateForm(final Flashcard flashcard) {
        // Ensure form components are initialized
        if (frontTextField == null || backTextField == null || exampleArea == null || imageUrlField == null) {
            LOGGER.warn("Form components not initialized yet, skipping populateForm");
            return;
        }

        if (flashcard != null) {
            frontTextField.setValue(flashcard.getFrontText() != null ? flashcard.getFrontText() : "");
            backTextField.setValue(flashcard.getBackText() != null ? flashcard.getBackText() : "");
            exampleArea.setValue(flashcard.getExample() != null ? flashcard.getExample() : "");
            imageUrlField.setValue(flashcard.getImageUrl() != null ? flashcard.getImageUrl() : "");
        } else {
            // Clear form for new flashcard
            frontTextField.setValue("");
            backTextField.setValue("");
            exampleArea.setValue("");
            imageUrlField.setValue("");
        }
    }

    /**
     * Handles saving the flashcard after form validation.
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
            Flashcard flashcard = prepareFlashcard(frontText, backText);
            flashcardUseCase.saveFlashcard(flashcard);

            notifyParentAndClose(flashcard);

        } catch (Exception ex) {
            handleSaveError(ex);
        }
    }

    /**
     * Prepares flashcard entity with form values.
     * Values are already trimmed by caller.
     *
     * @param frontText front text value (already trimmed)
     * @param backText back text value (already trimmed)
     * @return prepared flashcard
     */
    private Flashcard prepareFlashcard(final String frontText, final String backText) {
        Flashcard flashcard = editingFlashcard != null ? editingFlashcard : new Flashcard();

        if (editingFlashcard == null) {
            flashcard.setDeckId(currentDeck.getId());
        }

        flashcard.setFrontText(frontText);
        flashcard.setBackText(backText);
        flashcard.setExample(ValidationHelper.safeTrim(exampleArea.getValue())); // Nullable field
        flashcard.setImageUrl(ValidationHelper.safeTrim(imageUrlField.getValue())); // Nullable field

        return flashcard;
    }

    /**
     * Notifies parent component and closes dialog.
     *
     * @param flashcard the saved flashcard
     */
    private void notifyParentAndClose(final Flashcard flashcard) {
        if (onFlashcardSaved != null) {
            onFlashcardSaved.accept(flashcard);
        }

        close();
        showSuccessMessage();
    }

    /**
     * Shows success message based on action type.
     */
    private void showSuccessMessage() {
        boolean isEditing = editingFlashcard != null;
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
        LOGGER.error("Error saving flashcard: {}", ex.getMessage(), ex);
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
