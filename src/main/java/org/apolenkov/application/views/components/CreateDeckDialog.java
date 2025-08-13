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
import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.DeckView;

import java.util.function.Consumer;

/**
 * Dialog for creating a deck. Emits callback upon successful save.
 */
public class CreateDeckDialog extends Dialog {

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final Consumer<Deck> onCreated;

    public CreateDeckDialog(DeckUseCase deckUseCase, UserUseCase userUseCase, Consumer<Deck> onCreated) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;
        this.onCreated = onCreated;
        setWidth("520px");
        build();
    }

    private void build() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = new H3(getTranslation("dialog.newDeck"));

        TextField titleField = new TextField(getTranslation("dialog.deckTitle"));
        titleField.setWidth("100%");
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);

        TextArea descriptionArea = new TextArea(getTranslation("dialog.description"));
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("140px");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder(getTranslation("dialog.description.placeholder"));

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button(getTranslation("dialog.create"));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            String titleStr = titleField.getValue() != null ? titleField.getValue().trim() : "";
            String desc = descriptionArea.getValue() != null ? descriptionArea.getValue().trim() : "";
            if (titleStr.isEmpty()) {
                Notification.show(getTranslation("home.enterTitle"), 3000, Notification.Position.MIDDLE);
                titleField.focus();
                return;
            }
            Deck newDeck = new Deck();
            newDeck.setUserId(userUseCase.getCurrentUser().getId());
            newDeck.setTitle(titleStr);
            newDeck.setDescription(desc);
            Deck saved = deckUseCase.saveDeck(newDeck);
            Notification.show(getTranslation("home.deckCreated"), 2000, Notification.Position.BOTTOM_START);
            close();
            if (onCreated != null) onCreated.accept(saved);
            getUI().ifPresent(ui -> ui.navigate(DeckView.class, saved.getId().toString()));
        });

        Button cancel = new Button(getTranslation("dialog.cancel"));
        cancel.addClickListener(e -> close());
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        add(layout);
    }
}


