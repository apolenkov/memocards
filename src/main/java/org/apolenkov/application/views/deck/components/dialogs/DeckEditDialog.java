package org.apolenkov.application.views.deck.components.dialogs;

import java.util.function.Consumer;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

/**
 * Dialog component for editing existing flashcard decks.
 * Provides users with the ability to modify deck information
 * including title and description with form validation and error handling.
 */
public class DeckEditDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckEditDialog.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient DeckUseCase deckUseCase;

    // Data
    private final transient Deck deck;

    // Callbacks
    private final transient Consumer<Deck> onSaved;

    /**
     * Creates a new DeckEditDialog for the specified deck.
     *
     * @param deckUseCaseParam use case for deck operations and persistence
     * @param deckParam the deck object to edit
     * @param onSavedParam callback to execute when the deck is successfully saved
     */
    public DeckEditDialog(final DeckUseCase deckUseCaseParam, final Deck deckParam, final Consumer<Deck> onSavedParam) {
        this.deckUseCase = deckUseCaseParam;
        this.deck = deckParam;
        this.onSaved = onSavedParam;
    }

    /**
     * Initializes the dialog components when the component is attached to the UI.
     * This method is called by Vaadin when the component is added to the component tree.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        addClassName("dialog-md");
        build();
    }

    /**
     * Builds the complete dialog interface.
     * Coordinates the creation of all dialog elements.
     */
    private void build() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = createHeader();
        TextField titleField = createTitleField();
        TextArea descriptionArea = createDescriptionArea();
        HorizontalLayout buttons = createButtonLayout(titleField, descriptionArea);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }

    /**
     * Creates the dialog header.
     *
     * @return configured header component
     */
    private H3 createHeader() {
        return new H3(getTranslation(DeckConstants.DECK_EDIT_TITLE));
    }

    /**
     * Creates the title input field with validation and pre-filled value.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField titleField = new TextField(getTranslation(DeckConstants.DECK_DECK_TITLE));
        titleField.setWidthFull();
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);
        titleField.setValue(deck.getTitle() != null ? deck.getTitle() : "");
        return titleField;
    }

    /**
     * Creates the description text area with pre-filled value.
     *
     * @return configured description area
     */
    private TextArea createDescriptionArea() {
        TextArea descriptionArea = new TextArea(getTranslation(DeckConstants.DECK_DESCRIPTION));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName(DeckConstants.TEXT_AREA_MD_CLASS);
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation(DeckConstants.DECK_DESCRIPTION_PLACEHOLDER));
        descriptionArea.setValue(deck.getDescription() != null ? deck.getDescription() : "");
        return descriptionArea;
    }

    /**
     * Creates the button layout with save and cancel buttons.
     *
     * @param titleField the title field for validation
     * @param descriptionArea the description area for validation
     * @return configured button layout
     */
    private HorizontalLayout createButtonLayout(final TextField titleField, final TextArea descriptionArea) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        BeanValidationBinder<Deck> binder = createBinder(titleField, descriptionArea);
        Button save = createSaveButton(binder);
        Button cancel = createCancelButton();

        buttons.add(save, cancel);
        return buttons;
    }

    /**
     * Creates the validation binder for form fields.
     *
     * @param titleField the title field to bind
     * @param descriptionArea the description area to bind
     * @return configured validation binder
     */
    private BeanValidationBinder<Deck> createBinder(final TextField titleField, final TextArea descriptionArea) {
        BeanValidationBinder<Deck> binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation(DeckConstants.DECK_CREATE_ENTER_TITLE))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);
        return binder;
    }

    /**
     * Creates the save button with action handler.
     *
     * @param binder the validation binder
     * @return configured save button
     */
    private Button createSaveButton(final BeanValidationBinder<Deck> binder) {
        return ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_EDIT_SAVE),
                e -> handleSaveAction(binder),
                ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the cancel button.
     *
     * @return configured cancel button
     */
    private Button createCancelButton() {
        return ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_CANCEL), e -> close(), ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Handles the save action with validation and business logic.
     *
     * @param binder the validation binder
     */
    private void handleSaveAction(final BeanValidationBinder<Deck> binder) {
        try {
            DeckEditData editData = prepareEditData();
            Deck saved = performSave(binder);
            logEditAction(editData, saved);
            handleSuccessfulSave(saved);
        } catch (ValidationException vex) {
            handleValidationError();
        } catch (Exception ex) {
            handleSaveError(ex);
        }
    }

    /**
     * Prepares data for editing operation.
     *
     * @return edit data with original values
     */
    private DeckEditData prepareEditData() {
        return new DeckEditData(deck.getTitle(), deck.getDescription());
    }

    /**
     * Performs the save operation.
     *
     * @param binder validation binder
     * @return saved deck
     * @throws ValidationException if validation fails
     */
    private Deck performSave(final BeanValidationBinder<Deck> binder) throws ValidationException {
        binder.writeBean(deck);
        return deckUseCase.saveDeck(deck);
    }

    /**
     * Logs the edit action with audit information.
     *
     * @param editData original data
     * @param saved saved deck
     */
    private void logEditAction(final DeckEditData editData, final Deck saved) {
        AUDIT_LOGGER.info(
                "User edited deck '{}' (ID: {}) - Title changed: '{}' -> '{}', Description length: {} -> {}",
                saved.getTitle(),
                saved.getId(),
                editData.originalTitle(),
                saved.getTitle(),
                editData.originalDescriptionLength(),
                saved.getDescription() != null ? saved.getDescription().length() : 0);
    }

    /**
     * Handles successful save operation.
     *
     * @param saved saved deck
     */
    private void handleSuccessfulSave(final Deck saved) {
        NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_EDIT_SUCCESS));
        close();

        if (onSaved != null) {
            onSaved.accept(saved);
        }
    }

    /**
     * Handles validation errors.
     */
    private void handleValidationError() {
        LOGGER.warn("Deck editing failed due to validation error for deck ID: {}", deck.getId());
        NotificationHelper.showError(getTranslation("dialog.fillRequired"));
    }

    /**
     * Handles general save errors.
     *
     * @param ex exception that occurred
     */
    private void handleSaveError(final Exception ex) {
        LOGGER.error("Error editing deck ID {}: {}", deck.getId(), ex.getMessage(), ex);
        NotificationHelper.showError(ex.getMessage());
    }

    /**
     * Record for storing original edit data.
     *
     * @param originalTitle original title
     * @param originalDescription original description
     */
    private record DeckEditData(String originalTitle, String originalDescription) {
        /**
         * Gets original description length.
         *
         * @return description length or 0 if null
         */
        public int originalDescriptionLength() {
            return originalDescription != null ? originalDescription.length() : 0;
        }
    }
}
