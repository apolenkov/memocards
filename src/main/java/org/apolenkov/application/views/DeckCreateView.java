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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.FlashcardService;

@PageTitle("Добавить колоду")
@Route("decks/new")
@AnonymousAllowed
public class DeckCreateView extends Composite<VerticalLayout> {

    private final FlashcardService flashcardService;
    private TextField titleField;
    private TextArea descriptionArea;

    public DeckCreateView(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
        
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
        
        Button backButton = new Button("Назад", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        
        H2 title = new H2("Добавить новую колоду");
        title.getStyle().set("margin-left", "var(--lumo-space-m)");
        
        leftSection.add(backButton, title);
        headerLayout.add(leftSection);
        
        getContent().add(headerLayout);
    }

    private void createForm() {
        Div formContainer = new Div();
        formContainer.setWidth("100%");
        formContainer.setMaxWidth("600px");
        formContainer.getStyle()
            .set("border", "2px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("margin", "var(--lumo-space-l) auto")
            .set("background", "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-s)");
        
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.setWidth("100%");
        
        H3 formTitle = new H3("Информация о колоде");
        formTitle.getStyle().set("margin-top", "0");
        
        titleField = new TextField("Название колоды");
        titleField.setWidth("100%");
        titleField.setRequired(true);
        titleField.setPlaceholder("Например: Английские слова");
        
        descriptionArea = new TextArea("Описание");
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("150px");
        descriptionArea.setPlaceholder("Краткое описание колоды (опционально)");
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        Button saveButton = new Button("Создать колоду", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        saveButton.addClickListener(e -> saveDeck());
        
        Button cancelButton = new Button("Отмена", VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        
        buttonsLayout.add(saveButton, cancelButton);
        
        formLayout.add(formTitle, titleField, descriptionArea, buttonsLayout);
        formContainer.add(formLayout);
        
        getContent().add(formContainer);
    }

    private void saveDeck() {
        if (titleField.isEmpty()) {
            Notification.show("Введите название колоды", 3000, Notification.Position.MIDDLE);
            titleField.focus();
            return;
        }
        
        try {
            Deck newDeck = new Deck();
            newDeck.setUserId(flashcardService.getCurrentUser().getId());
            newDeck.setTitle(titleField.getValue().trim());
            newDeck.setDescription(descriptionArea.getValue().trim());
            
            Deck savedDeck = flashcardService.saveDeck(newDeck);
            
            Notification.show("Колода '" + savedDeck.getTitle() + "' успешно создана!", 
                3000, Notification.Position.BOTTOM_START);
            
            // Переходим к просмотру созданной колоды
            getUI().ifPresent(ui -> ui.navigate(DeckView.class, savedDeck.getId().toString()));
            
        } catch (Exception e) {
            Notification.show("Ошибка при создании колоды: " + e.getMessage(), 
                5000, Notification.Position.MIDDLE);
        }
    }
}
