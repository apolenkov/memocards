package org.apolenkov.application.views.components;

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
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.NotificationHelper;

/**
 * Dialog component for creating new decks.
 * Provides a form interface for users to create new flashcard decks with validation
 * and automatic navigation to the newly created deck.
 */
public class CreateDeckDialog extends Dialog {

    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private final transient Consumer<Deck> onCreated;

    /**
     * Creates a new CreateDeckDialog.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param userUseCaseParam service for user operations
     * @param createdCallback callback function called when deck is successfully created
     */
    public CreateDeckDialog(
            final DeckUseCase deckUseCaseParam,
            final UserUseCase userUseCaseParam,
            final Consumer<Deck> createdCallback) {
        this.deckUseCase = deckUseCaseParam;
        this.userUseCase = userUseCaseParam;
        this.onCreated = createdCallback;
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
     * Creates and configures all form elements including input fields,
     * validation binding, buttons, and event handlers.
     */
    private void build() {
        // Create main layout with proper spacing and padding
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = new H3(getTranslation("dialog.newDeck"));

        // Configure title field with validation and constraints
        TextField titleField = new TextField(getTranslation("dialog.deckTitle"));
        titleField.setWidthFull();
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);

        // Configure description area with appropriate styling
        TextArea descriptionArea = new TextArea(getTranslation("dialog.description"));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation("dialog.description.placeholder"));

        // Set up bean validation binding for form data integrity
        BeanValidationBinder<Deck> binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("home.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        // Create button layout with save and cancel actions
        HorizontalLayout buttons = LayoutHelper.createButtonLayout();

        Button save = ButtonHelper.createButton(
                getTranslation("dialog.create"),
                e -> {
                    // Create new deck instance and populate with form data
                    Deck bean = new Deck();
                    bean.setUserId(userUseCase.getCurrentUser().getId());
                    try {
                        // Validate and write form data to bean
                        binder.writeBean(bean);
                        Deck saved = deckUseCase.saveDeck(bean);
                        NotificationHelper.showSuccessBottom(getTranslation("home.deckCreated"));
                        close();
                        // Execute callback and navigate to new deck
                        if (onCreated != null) {
                            onCreated.accept(saved);
                        }
                        NavigationHelper.navigateToDeck(saved.getId());
                    } catch (ValidationException vex) {
                        // Show validation error message
                        NotificationHelper.showError(getTranslation("dialog.fillRequired"));
                    } catch (Exception ex) {
                        // Show general error message
                        NotificationHelper.showError(ex.getMessage());
                    }
                },
                ButtonVariant.LUMO_PRIMARY);

        Button cancel =
                ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);
        buttons.add(save, cancel);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setSpacing(true);
        buttons.setWidthFull();
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);

        // Assemble final layout and add to dialog
        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}
