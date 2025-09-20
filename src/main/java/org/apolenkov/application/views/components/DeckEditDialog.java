package org.apolenkov.application.views.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;

/**
 * Dialog component for editing existing flashcard decks.
 * Provides users with the ability to modify deck information
 * including title and description with form validation and error handling.
 */
public class DeckEditDialog extends Dialog {

    private final transient DeckUseCase deckUseCase;
    private final transient Deck deck;
    private final transient Consumer<Deck> onSaved;

    /**
     * Creates a new DeckEditDialog for the specified deck.
     *
     * @param deckUseCaseParam use case for deck operations and persistence
     * @param deckValue the deck object to edit
     * @param savedCallback callback to execute when the deck is successfully saved
     */
    public DeckEditDialog(
            final DeckUseCase deckUseCaseParam, final Deck deckValue, final Consumer<Deck> savedCallback) {
        this.deckUseCase = deckUseCaseParam;
        this.deck = deckValue;
        this.onSaved = savedCallback;
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

        HorizontalLayout buttons = LayoutHelper.createButtonLayout();

        Button save = ButtonHelper.createButton(
                getTranslation("deck.edit.save"),
                e -> {
                    try {
                        binder.writeBean(deck);
                        Deck saved = deckUseCase.saveDeck(deck);
                        NotificationHelper.showSuccessBottom(getTranslation("deck.edit.success"));
                        close();
                        if (onSaved != null) {
                            onSaved.accept(saved);
                        }
                    } catch (ValidationException vex) {
                        NotificationHelper.showError(getTranslation("dialog.fillRequired"));
                    } catch (Exception ex) {
                        NotificationHelper.showError(ex.getMessage());
                    }
                },
                ButtonVariant.LUMO_PRIMARY);

        Button cancel =
                ButtonHelper.createButton(getTranslation("common.cancel"), e -> close(), ButtonVariant.LUMO_TERTIARY);
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}
