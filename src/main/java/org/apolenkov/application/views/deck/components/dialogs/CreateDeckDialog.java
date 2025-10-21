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
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.DialogHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for creating new decks.
 * Provides a form interface for creating card decks with validation.
 */
public class CreateDeckDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDeckDialog.class);

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;

    // Callbacks
    private final transient Consumer<Deck> onCreated;

    /**
     * Creates a new CreateDeckDialog.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param userUseCaseParam service for user operations
     * @param onCreatedParam callback function called when deck is successfully created
     */
    public CreateDeckDialog(
            final DeckUseCase deckUseCaseParam,
            final UserUseCase userUseCaseParam,
            final Consumer<Deck> onCreatedParam) {
        this.deckUseCase = deckUseCaseParam;
        this.userUseCase = userUseCaseParam;
        this.onCreated = onCreatedParam;
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
     * Builds the dialog UI components and layout.
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
        return new H3(getTranslation(DeckConstants.DIALOG_NEW_DECK));
    }

    /**
     * Creates the title input field with validation.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField titleField = new TextField(getTranslation(DeckConstants.DECK_DECK_TITLE));
        titleField.setWidthFull();
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);
        return titleField;
    }

    /**
     * Creates the description text area.
     *
     * @return configured description area
     */
    private TextArea createDescriptionArea() {
        TextArea descriptionArea = new TextArea(getTranslation(DeckConstants.DIALOG_DESCRIPTION));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName(DeckConstants.TEXT_AREA_MD_CLASS);
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation(DeckConstants.DIALOG_DESCRIPTION_PLACEHOLDER));
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
                getTranslation(DeckConstants.DIALOG_CREATE),
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
                titleField, title, getTranslation(DeckConstants.HOME_ENTER_TITLE))) {
            return;
        }

        try {
            Deck deck = new Deck();
            deck.setUserId(userUseCase.getCurrentUser().getId());
            deck.setTitle(title);
            deck.setDescription(description);

            Deck saved = deckUseCase.saveDeck(deck);

            LOGGER.debug("Deck created successfully: id={}, title='{}'", saved.getId(), saved.getTitle());

            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.HOME_DECK_CREATED));
            close();

            if (onCreated != null) {
                onCreated.accept(saved);
            }
            NavigationHelper.navigateToDeck(saved.getId());
        } catch (Exception ex) {
            LOGGER.error("Error creating deck: {}", ex.getMessage(), ex);
            NotificationHelper.showError(ex.getMessage());
        }
    }
}
