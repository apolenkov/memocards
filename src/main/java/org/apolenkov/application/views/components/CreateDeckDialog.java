package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.usecase.UserUseCase;

/**
 * Dialog component for creating new decks.
 *
 * <p>This dialog provides a form interface for users to create new flashcard decks.
 * It includes validation, error handling, and automatic navigation to the newly
 * created deck upon successful creation.</p>
 *
 * <p>The dialog features:</p>
 * <ul>
 *   <li>Title and description input fields with validation</li>
 *   <li>Bean validation binding for data integrity</li>
 *   <li>Error handling and user feedback</li>
 *   <li>Automatic navigation after successful creation</li>
 * </ul>
 */
public class CreateDeckDialog extends Dialog {

    private final transient DeckFacade deckFacade;
    private final transient UserUseCase userUseCase;
    private final transient Consumer<Deck> onCreated;

    /**
     * Constructs a new CreateDeckDialog.
     *
     * @param deckFacade service for deck operations
     * @param userUseCase service for user operations
     * @param onCreated callback function called when deck is successfully created
     */
    public CreateDeckDialog(DeckFacade deckFacade, UserUseCase userUseCase, Consumer<Deck> onCreated) {
        this.deckFacade = deckFacade;
        this.userUseCase = userUseCase;
        this.onCreated = onCreated;
        addClassName("dialog-md");
        build();
    }

    /**
     * Builds the dialog UI components and layout.
     *
     * <p>Creates and configures all form elements including input fields,
     * validation binding, buttons, and event handlers. The method sets up
     * the complete user interface for deck creation.</p>
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
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button save = new Button(getTranslation("dialog.create"));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            // Create new deck instance and populate with form data
            Deck bean = new Deck();
            bean.setUserId(userUseCase.getCurrentUser().getId());
            try {
                // Validate and write form data to bean
                binder.writeBean(bean);
                Deck saved = deckFacade.saveDeck(bean);
                Notification.show(getTranslation("home.deckCreated"), 2000, Notification.Position.BOTTOM_START);
                close();
                // Execute callback and navigate to new deck
                if (onCreated != null) onCreated.accept(saved);
                getUI().ifPresent(ui -> ui.navigate("deck/" + saved.getId().toString()));
            } catch (ValidationException vex) {
                // Show validation error message
                Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                // Show general error message
                Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
            }
        });

        Button cancel = new Button(getTranslation("common.cancel"));
        cancel.addClickListener(e -> close());
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
