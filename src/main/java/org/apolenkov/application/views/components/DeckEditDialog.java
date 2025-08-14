package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.DeckFacade;

public class DeckEditDialog extends Dialog {

    private final DeckFacade deckFacade;
    private final Deck deck;
    private final Consumer<Deck> onSaved;
    private BeanValidationBinder<Deck> binder;

    public DeckEditDialog(DeckFacade deckFacade, Deck deck, Consumer<Deck> onSaved) {
        this.deckFacade = deckFacade;
        this.deck = deck;
        this.onSaved = onSaved;
        setWidth("520px");
        build();
    }

    private void build() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = new H3(getTranslation("deck.edit.title"));

        TextField titleField = new TextField(getTranslation("dialog.deckTitle"));
        titleField.setWidth("100%");
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);
        titleField.setValue(deck.getTitle() != null ? deck.getTitle() : "");

        TextArea descriptionArea = new TextArea(getTranslation("dialog.description"));
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("140px");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation("dialog.description.placeholder"));
        descriptionArea.setValue(deck.getDescription() != null ? deck.getDescription() : "");

        binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button(getTranslation("deck.edit.save"));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            try {
                binder.writeBean(deck);
                Deck saved = deckFacade.saveDeck(deck);
                Notification.show(getTranslation("deck.edit.success"), 2000, Notification.Position.BOTTOM_START);
                close();
                if (onSaved != null) onSaved.accept(saved);
            } catch (ValidationException vex) {
                Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button(getTranslation("dialog.cancel"));
        cancel.addClickListener(e -> close());
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}
