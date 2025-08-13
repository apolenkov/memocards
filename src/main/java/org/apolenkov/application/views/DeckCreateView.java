package org.apolenkov.application.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;

@Route("decks/new")
@RolesAllowed("USER")
public class DeckCreateView extends Composite<VerticalLayout> implements HasDynamicTitle {

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private TextField titleField;
    private TextArea descriptionArea;
    private BeanValidationBinder<Deck> binder;

    public DeckCreateView(DeckUseCase deckUseCase, UserUseCase userUseCase) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;

        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        createHeader();
        createForm();
    }

    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = new Button(getTranslation("deckCreate.back"), VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        H2 title = new H2(getTranslation("deckCreate.title"));
        title.addClassName("deckedit-view__header-title");

        leftSection.add(backButton, title);
        headerLayout.add(leftSection);

        getContent().add(headerLayout);
    }

    private void createForm() {
        Div formContainer = new Div();
        formContainer.setWidth("100%");
        formContainer.setMaxWidth("600px");
        formContainer
                .getStyle()
                .set("border", "2px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-xl)")
                .set("margin", "var(--lumo-space-l) auto")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.setWidth("100%");

        H3 formTitle = new H3(getTranslation("deckCreate.section"));
        formTitle.addClassName("deckedit-view__section-title");

        titleField = new TextField(getTranslation("deckCreate.name"));
        titleField.setWidth("100%");
        titleField.setRequired(true);
        titleField.setPlaceholder(getTranslation("deckCreate.name.placeholder"));

        descriptionArea = new TextArea(getTranslation("deckCreate.description"));
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("150px");
        descriptionArea.setPlaceholder(getTranslation("deckCreate.description.placeholder"));

        binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button saveButton = new Button(getTranslation("deckCreate.create"), VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        saveButton.addClickListener(e -> saveDeck());

        Button cancelButton = new Button(getTranslation("deckCreate.cancel"), VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        buttonsLayout.add(saveButton, cancelButton);

        formLayout.add(formTitle, titleField, descriptionArea, buttonsLayout);
        formContainer.add(formLayout);

        getContent().add(formContainer);
    }

    private void saveDeck() {
        try {
            Deck newDeck = new Deck();
            newDeck.setUserId(userUseCase.getCurrentUser().getId());
            binder.writeBean(newDeck);
            Deck savedDeck = deckUseCase.saveDeck(newDeck);
            Notification.show(
                    getTranslation("deckCreate.created", savedDeck.getTitle()),
                    3000,
                    Notification.Position.BOTTOM_START);
            getUI().ifPresent(
                            ui -> ui.navigate(DeckView.class, savedDeck.getId().toString()));
        } catch (ValidationException vex) {
            Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show(getTranslation("deckCreate.error", e.getMessage()), 5000, Notification.Position.MIDDLE);
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("deckCreate.title");
    }
}
