package org.apolenkov.application.views.deck.components.dialogs;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        addClassName(DeckConstants.DIALOG_MD_CLASS);
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
        HorizontalLayout buttons = DialogHelper.createButtonLayout();

        Button save = ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_EDIT_SAVE),
                e -> handleSaveAction(titleField, descriptionArea),
                ButtonVariant.LUMO_PRIMARY);

        Button cancel = ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_CANCEL), e -> close(), ButtonVariant.LUMO_TERTIARY);

        buttons.add(save, cancel);
        return buttons;
    }

    /**
     * Handles the save action with validation and business logic.
     *
     * @param titleField the title field
     * @param descriptionArea the description area
     */
    private void handleSaveAction(final TextField titleField, final TextArea descriptionArea) {
        String title = ValidationHelper.safeTrimToEmpty(titleField.getValue());
        String description = ValidationHelper.safeTrim(descriptionArea.getValue()); // Nullable field

        if (ValidationHelper.validateRequiredSimple(
                titleField, title, getTranslation(DeckConstants.DECK_CREATE_ENTER_TITLE))) {
            return;
        }

        try {
            String originalTitle = deck.getTitle();
            int originalDescLength =
                    deck.getDescription() != null ? deck.getDescription().length() : 0;

            deck.setTitle(title);
            deck.setDescription(description);

            Deck saved = deckUseCase.saveDeck(deck);

            AUDIT_LOGGER.info(
                    "User edited deck '{}' (ID: {}) - Title changed: '{}' -> '{}', Description length: {} -> {}",
                    saved.getTitle(),
                    saved.getId(),
                    originalTitle,
                    saved.getTitle(),
                    originalDescLength,
                    saved.getDescription() != null ? saved.getDescription().length() : 0);

            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_EDIT_SUCCESS));
            close();

            if (onSaved != null) {
                onSaved.accept(saved);
            }
        } catch (Exception ex) {
            LOGGER.error("Error editing deck ID {}: {}", deck.getId(), ex.getMessage(), ex);
            NotificationHelper.showError(ex.getMessage());
        }
    }
}
