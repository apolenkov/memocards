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
import jakarta.annotation.PostConstruct;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
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
    private static final String FILL_REQUIRED_KEY = "dialog.fillRequired";

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
        addClassName("dialog-md");
    }

    /**
     * Initializes the dialog components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    public void init() {
        createDialog();
        // Form creation moved to openDialog() to ensure proper initialization order
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
     * Creates the main dialog component.
     */
    private void createDialog() {
        // Dialog is already created in constructor
    }

    /**
     * Creates the form layout with input fields.
     */
    private void createForm() {
        formLayout = new FormLayout();

        frontTextField = new TextField(getTranslation("deck.col.front"));
        frontTextField.setWidthFull();
        frontTextField.setRequired(true);

        backTextField = new TextField(getTranslation("deck.col.back"));
        backTextField.setWidthFull();
        backTextField.setRequired(true);

        exampleArea = new TextArea(getTranslation("deck.example.optional"));
        exampleArea.setWidthFull();
        exampleArea.addClassName("text-area--sm");

        imageUrlField = new TextField(getTranslation("deck.imageUrl.optional"));
        imageUrlField.setWidthFull();

        formLayout.add(frontTextField, backTextField, exampleArea, imageUrlField);
    }

    /**
     * Creates the validation binder for form data.
     */
    private void createBinder() {
        binder = new BeanValidationBinder<>(Flashcard.class);

        binder.forField(frontTextField)
                .asRequired(getTranslation(FILL_REQUIRED_KEY))
                .bind(Flashcard::getFrontText, Flashcard::setFrontText);
        binder.forField(backTextField)
                .asRequired(getTranslation(FILL_REQUIRED_KEY))
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
                getTranslation("dialog.save"), VaadinIcon.CHECK, e -> handleSave(), ButtonVariant.LUMO_PRIMARY);

        cancelButton = ButtonHelper.createButton(
                getTranslation("common.cancel"), e -> handleCancel(), ButtonVariant.LUMO_TERTIARY);

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
                flashcard == null ? getTranslation("deck.card.addTitle") : getTranslation("deck.card.editTitle"));

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
            Flashcard flashcard;
            if (editingFlashcard != null) {
                // Editing existing flashcard - preserve ID and deckId
                flashcard = editingFlashcard;
                binder.writeBean(flashcard);
            } else {
                // Creating new flashcard
                flashcard = new Flashcard();
                flashcard.setDeckId(currentDeck.getId());
                binder.writeBean(flashcard);
            }

            flashcardUseCase.saveFlashcard(flashcard);

            // Notify parent component
            if (onFlashcardSaved != null) {
                onFlashcardSaved.accept(flashcard);
            }

            close();
            String message =
                    editingFlashcard != null ? getTranslation("deck.card.updated") : getTranslation("deck.card.added");
            NotificationHelper.showSuccessBottom(message);

        } catch (ValidationException vex) {
            NotificationHelper.showError(getTranslation(FILL_REQUIRED_KEY));
        } catch (Exception ex) {
            NotificationHelper.showErrorLong(ex.getMessage());
            LOGGER.error("Error saving flashcard", ex);
        }
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
