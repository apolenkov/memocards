package org.apolenkov.application.views.deck.components.dialogs;

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
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog component for creating new decks.
 * Provides a form interface for users to create new flashcard decks with validation
 * and automatic navigation to the newly created deck.
 */
public class CreateDeckDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDeckDialog.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

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

        addClassName("dialog-md");
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
        return new H3(getTranslation("dialog.newDeck"));
    }

    /**
     * Creates the title input field with validation.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField titleField = new TextField(getTranslation("dialog.deckTitle"));
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
        TextArea descriptionArea = new TextArea(getTranslation("dialog.description"));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation("dialog.description.placeholder"));
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
                .asRequired(getTranslation("home.enterTitle"))
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
                getTranslation("dialog.create"), e -> handleSaveAction(binder), ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the cancel button.
     *
     * @return configured cancel button
     */
    private Button createCancelButton() {
        return ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Handles the save action with validation and business logic.
     *
     * @param binder the validation binder
     */
    private void handleSaveAction(final BeanValidationBinder<Deck> binder) {
        Deck bean = new Deck();
        bean.setUserId(userUseCase.getCurrentUser().getId());

        try {
            binder.writeBean(bean);
            Deck saved = deckUseCase.saveDeck(bean);

            // Audit log for deck creation
            AUDIT_LOGGER.info(
                    "User created new deck '{}' with {} characters description - Deck ID: {}",
                    saved.getTitle(),
                    saved.getDescription() != null ? saved.getDescription().length() : 0,
                    saved.getId());

            NotificationHelper.showSuccessBottom(getTranslation("home.deckCreated"));
            close();

            if (onCreated != null) {
                onCreated.accept(saved);
            }
            NavigationHelper.navigateToDeck(saved.getId());
        } catch (ValidationException vex) {
            LOGGER.warn("Deck creation failed due to validation error");
            NotificationHelper.showError(getTranslation("dialog.fillRequired"));
        } catch (Exception ex) {
            LOGGER.error("Error creating deck: {}", ex.getMessage(), ex);
            NotificationHelper.showError(ex.getMessage());
        }
    }
}
