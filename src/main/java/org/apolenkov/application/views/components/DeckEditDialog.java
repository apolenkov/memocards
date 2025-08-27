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

/**
 * Dialog component for editing existing flashcard decks.
 * Provides users with the ability to modify deck information
 * including title and description with form validation and error handling.
 */
public class DeckEditDialog extends Dialog {

    private final transient DeckFacade deckFacade;
    private final transient Deck deck;
    private final transient Consumer<Deck> onSaved;

    /**
     * Creates a new DeckEditDialog for the specified deck.
     * Initializes the dialog with the deck's current information
     * and sets up the form layout with appropriate validation rules.
     *
     * @param facade service for deck operations and persistence
     * @param deckValue the deck object to edit
     * @param savedCallback callback to execute when the deck is successfully saved
     */
    public DeckEditDialog(final DeckFacade facade, final Deck deckValue, final Consumer<Deck> savedCallback) {
        this.deckFacade = facade;
        this.deck = deckValue;
        this.onSaved = savedCallback;
        addClassName("dialog-md");
        build();
    }

    /**
     * Builds the complete dialog interface.
     * Creates and configures all form elements including input fields,
     * validation binding, buttons, and event handlers.
     */
    private void build() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = new H3(getTranslation("deck.edit.title"));

        TextField titleField = new TextField(getTranslation("dialog.deckTitle"));
        titleField.setWidthFull();
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);
        titleField.setValue(deck.getTitle() != null ? deck.getTitle() : "");

        TextArea descriptionArea = new TextArea(getTranslation("dialog.description"));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation("dialog.description.placeholder"));
        descriptionArea.setValue(deck.getDescription() != null ? deck.getDescription() : "");

        BeanValidationBinder<Deck> binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button save = new Button(getTranslation("deck.edit.save"));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                binder.writeBean(deck);
                Deck saved = deckFacade.saveDeck(deck);
                Notification.show(getTranslation("deck.edit.success"), 2000, Notification.Position.BOTTOM_START);
                close();
                if (onSaved != null) {
                    onSaved.accept(saved);
                }
            } catch (ValidationException vex) {
                Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.BOTTOM_START);
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_START);
            }
        });

        Button cancel = new Button(getTranslation("common.cancel"));
        cancel.addClickListener(e -> close());
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}
