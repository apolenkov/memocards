package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
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
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient Deck currentDeck;

    // Callbacks
    private final transient Consumer<Flashcard> onFlashcardSaved;

    // State
    private transient Flashcard editingFlashcard;

    // UI Components
    private FormLayout formLayout;
    private BeanValidationBinder<Flashcard> binder;
    private TextField frontTextField;
    private TextField backTextField;
    private TextArea exampleArea;
    private TextField imageUrlField;
    private Button saveButton;
    private Button cancelButton;

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
     * Creates the validation binder for form data.
     */
    private void createBinder() {
        binder = new BeanValidationBinder<>(Flashcard.class);

        binder.forField(frontTextField)
                .asRequired(getTranslation(DeckConstants.FILL_REQUIRED_KEY))
                .bind(Flashcard::getFrontText, Flashcard::setFrontText);
        binder.forField(backTextField)
                .asRequired(getTranslation(DeckConstants.FILL_REQUIRED_KEY))
                .bind(Flashcard::getBackText, Flashcard::setBackText);
        binder.forField(exampleArea).bind(Flashcard::getExample, Flashcard::setExample);
        binder.forField(imageUrlField).bind(Flashcard::getImageUrl, Flashcard::setImageUrl);
    }

    /**
     * Creates dialog buttons for save and cancel actions.
     */
    private void createButtons() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        saveButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.DIALOG_SAVE),
                VaadinIcon.CHECK,
                e -> handleSave(),
                ButtonVariant.LUMO_PRIMARY);

        cancelButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_CANCEL), e -> handleCancel(), ButtonVariant.LUMO_TERTIARY);

        buttonsLayout.add(saveButton, cancelButton);
    }

    /**
     * Opens the dialog with the specified flashcard data.
     *
     * @param flashcard the flashcard to edit, or null for creating new
     */
    private void openDialog(final Flashcard flashcard) {
        // Store the editing flashcard
        this.editingFlashcard = flashcard;

        // Ensure form is created before using it
        if (formLayout == null) {
            createForm();
            createBinder();
            createButtons();
        }

        // Set dialog title
        H3 title = new H3(
                flashcard == null
                        ? getTranslation(DeckConstants.DECK_CARD_ADD_TITLE)
                        : getTranslation(DeckConstants.DECK_CARD_EDIT_TITLE));

        // Populate form with flashcard data
        populateForm(flashcard);

        // Create dialog layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(title, formLayout, getButtonsLayout());

        removeAll();
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
        try {
            Flashcard flashcard = prepareFlashcard();
            flashcardUseCase.saveFlashcard(flashcard);

            logFlashcardAction(flashcard);
            notifyParentAndClose(flashcard);

        } catch (ValidationException vex) {
            handleValidationError();
        } catch (Exception ex) {
            handleSaveError(ex);
        }
    }

    /**
     * Prepares flashcard for saving (create or edit).
     *
     * @return prepared flashcard
     * @throws ValidationException if validation fails
     */
    private Flashcard prepareFlashcard() throws ValidationException {
        boolean isEditing = editingFlashcard != null;

        if (isEditing) {
            return prepareEditingFlashcard();
        } else {
            return prepareNewFlashcard();
        }
    }

    /**
     * Prepares existing flashcard for editing.
     *
     * @return prepared flashcard
     * @throws ValidationException if validation fails
     */
    private Flashcard prepareEditingFlashcard() throws ValidationException {
        Flashcard flashcard = editingFlashcard;
        binder.writeBean(flashcard);
        return flashcard;
    }

    /**
     * Prepares new flashcard for creation.
     *
     * @return prepared flashcard
     * @throws ValidationException if validation fails
     */
    private Flashcard prepareNewFlashcard() throws ValidationException {
        Flashcard flashcard = new Flashcard();
        flashcard.setDeckId(currentDeck.getId());
        binder.writeBean(flashcard);
        return flashcard;
    }

    /**
     * Logs flashcard action (create or edit).
     *
     * @param flashcard the flashcard being saved
     */
    private void logFlashcardAction(final Flashcard flashcard) {
        boolean isEditing = editingFlashcard != null;
        String action = isEditing ? "edited" : "created";

        AUDIT_LOGGER.info(
                "User {} flashcard '{}' (ID: {}) in deck '{}' (ID: {})",
                action,
                flashcard.getFrontText(),
                flashcard.getId(),
                currentDeck.getTitle(),
                currentDeck.getId());
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
     * Handles validation errors.
     */
    private void handleValidationError() {
        LOGGER.warn("Flashcard save failed due to validation error");
        NotificationHelper.showError(getTranslation(DeckConstants.FILL_REQUIRED_KEY));
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
     * Handles canceling the dialog.
     */
    private void handleCancel() {
        close();
    }

    /**
     * Gets the buttons layout for the dialog.
     *
     * @return the horizontal layout containing buttons
     */
    private HorizontalLayout getButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonsLayout.add(saveButton, cancelButton);
        return buttonsLayout;
    }
}
