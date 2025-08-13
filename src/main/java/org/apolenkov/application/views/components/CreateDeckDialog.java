package org.apolenkov.application.views.components;

import java.util.function.Consumer;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.DeckView;

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

/** Dialog for creating a deck. Emits callback upon successful save. */
public class CreateDeckDialog extends Dialog {

  private final DeckUseCase deckUseCase;
  private final UserUseCase userUseCase;
  private final Consumer<Deck> onCreated;
  private BeanValidationBinder<Deck> binder;

  public CreateDeckDialog(
      DeckUseCase deckUseCase, UserUseCase userUseCase, Consumer<Deck> onCreated) {
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

    binder = new BeanValidationBinder<>(Deck.class);
    binder
        .forField(titleField)
        .asRequired(getTranslation("home.enterTitle"))
        .bind(Deck::getTitle, Deck::setTitle);
    binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

    HorizontalLayout buttons = new HorizontalLayout();
    Button save = new Button(getTranslation("dialog.create"));
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.addClickListener(
        e -> {
          Deck bean = new Deck();
          bean.setUserId(userUseCase.getCurrentUser().getId());
          try {
            binder.writeBean(bean);
            Deck saved = deckUseCase.saveDeck(bean);
            Notification.show(
                getTranslation("home.deckCreated"), 2000, Notification.Position.BOTTOM_START);
            close();
            if (onCreated != null) onCreated.accept(saved);
            getUI().ifPresent(ui -> ui.navigate(DeckView.class, saved.getId().toString()));
          } catch (ValidationException vex) {
            Notification.show(
                getTranslation("dialog.fillRequired"), 3000, Notification.Position.MIDDLE);
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
