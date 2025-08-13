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
import org.apolenkov.application.application.usecase.DeckUseCase;
import org.apolenkov.application.model.Deck;

import java.util.function.Consumer;

public class DeckEditDialog extends Dialog {

    private final DeckUseCase deckUseCase;
    private final Deck deck;
    private final Consumer<Deck> onSaved;

    public DeckEditDialog(DeckUseCase deckUseCase, Deck deck, Consumer<Deck> onSaved) {
        this.deckUseCase = deckUseCase;
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

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button(getTranslation("deck.edit.save"));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            String titleStr = titleField.getValue() != null ? titleField.getValue().trim() : "";
            String desc = descriptionArea.getValue() != null ? descriptionArea.getValue().trim() : "";
            if (titleStr.isEmpty()) {
                Notification.show(getTranslation("deckCreate.enterTitle"), 3000, Notification.Position.MIDDLE);
                titleField.focus();
                return;
            }
            deck.setTitle(titleStr);
            deck.setDescription(desc);
            Deck saved = deckUseCase.saveDeck(deck);
            Notification.show(getTranslation("deck.edit.success"), 2000, Notification.Position.BOTTOM_START);
            close();
            if (onSaved != null) onSaved.accept(saved);
        });

        Button cancel = new Button(getTranslation("dialog.cancel"));
        cancel.addClickListener(e -> close());
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}


