package org.apolenkov.application.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.FlashcardService;
import org.apolenkov.application.views.deskview.DeskviewView;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@PageTitle("Колоды")
@Route("")
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    private final FlashcardService flashcardService;
    private VerticalLayout decksContainer;
    private TextField searchField;

    public HomeView(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
        
        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        
        createHeader();
        createSearchAndActions();
        createDecksList();
        loadDecks();
    }

    private void createHeader() {
        H2 title = new H2("Мои колоды");
        title.getStyle().set("margin", "0");
        getContent().add(title);
    }

    private void createSearchAndActions() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidth("100%");
        searchLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        searchLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        searchField = new TextField();
        searchField.setPlaceholder("Поиск колоды...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> loadDecks());
        
        Button addDeckButton = new Button("Добавить колоду", VaadinIcon.PLUS.create());
        addDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addDeckButton.addClickListener(e -> openCreateDeckDialog());
        
        searchLayout.add(searchField, addDeckButton);
        getContent().add(searchLayout);
    }

    private void createDecksList() {
        decksContainer = new VerticalLayout();
        decksContainer.setWidth("100%");
        decksContainer.setPadding(false);
        decksContainer.setSpacing(true);
        getContent().add(decksContainer);
    }

    private void loadDecks() {
        decksContainer.removeAll();
        
        List<Deck> decks = flashcardService.getDecksByUserId(
            flashcardService.getCurrentUser().getId()
        );
        
        String query = searchField != null && searchField.getValue() != null
                ? searchField.getValue().toLowerCase(Locale.ROOT).trim() : "";
        if (!query.isEmpty()) {
            decks = decks.stream()
                    .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase(Locale.ROOT).contains(query))
                            || (d.getDescription() != null && d.getDescription().toLowerCase(Locale.ROOT).contains(query)))
                    .collect(Collectors.toList());
        }
        
        for (Deck deck : decks) {
            Div deckCard = createDeckCard(deck);
            deckCard.getStyle()
                .set("width", "100%")
                .set("box-sizing", "border-box");
            decksContainer.add(deckCard);
        }
    }

    private Div createDeckCard(Deck deck) {
        Div card = new Div();
        card.getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("cursor", "pointer")
            .set("transition", "box-shadow 0.2s")
            .set("background", "var(--lumo-base-color)");
        
        card.addClickListener(e -> {
            if (deck.getId() != null) {
                getUI().ifPresent(ui -> ui.navigate(DeskviewView.class, deck.getId().toString()));
            } else {
                Notification.show("Ошибка: ID колоды не найден", 3000, Notification.Position.MIDDLE);
            }
        });
        
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(false);
        cardContent.setSpacing(false);
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span icon = new Span("📚");
        icon.getStyle().set("font-size", "1.5em");
        
        H3 title = new H3(deck.getTitle() + " (" + deck.getFlashcardCount() + ")");
        title.getStyle().set("margin", "0");
        
        titleLayout.add(icon, title);
        
        Span description = new Span(deck.getDescription());
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        HorizontalLayout progressLayout = new HorizontalLayout();
        progressLayout.setSpacing(true);
        progressLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        progressLayout.setWidth("100%");
        
        Span progressLabel = new Span("Прогресс:");
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.random() * 0.8 + 0.2);
        progressBar.setWidth("120px");
        int progressPercent = (int) (progressBar.getValue() * 100);
        Span progressText = new Span(progressPercent + "%");
        
        progressLayout.add(progressLabel, progressBar, progressText);
        
        Button practiceButton = new Button("▶ Начать практику");
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> {
            if (deck.getId() != null) {
                getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.practice.PracticeView.class, deck.getId().toString()));
            } else {
                Notification.show("Ошибка: ID колоды не найден", 3000, Notification.Position.MIDDLE);
            }
        });
        
        cardContent.add(titleLayout, description, progressLayout, practiceButton);
        card.add(cardContent);
        return card;
    }

    private void openCreateDeckDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H3 header = new H3("Новая колода");

        TextField titleField = new TextField("Название колоды");
        titleField.setWidth("100%");
        titleField.setRequiredIndicatorVisible(true);
        titleField.setMaxLength(120);
        titleField.setClearButtonVisible(true);

        TextArea descriptionArea = new TextArea("Описание");
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("140px");
        descriptionArea.setMaxLength(500);
        descriptionArea.setPlaceholder("Краткое описание (опционально)");

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button("Создать", VaadinIcon.CHECK.create());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            String title = titleField.getValue() != null ? titleField.getValue().trim() : "";
            String desc = descriptionArea.getValue() != null ? descriptionArea.getValue().trim() : "";
            if (title.isEmpty()) {
                Notification.show("Введите название колоды", 3000, Notification.Position.MIDDLE);
                titleField.focus();
                return;
            }
            Deck deck = new Deck();
            deck.setUserId(flashcardService.getCurrentUser().getId());
            deck.setTitle(title);
            deck.setDescription(desc);
            Deck saved = flashcardService.saveDeck(deck);
            Notification.show("Колода создана", 2000, Notification.Position.BOTTOM_START);
            dialog.close();
            loadDecks();
            // Навигация к созданной колоде по желанию
            getUI().ifPresent(ui -> ui.navigate(DeskviewView.class, saved.getId().toString()));
        });

        Button cancel = new Button("Отмена", VaadinIcon.CLOSE.create());
        cancel.addClickListener(e -> dialog.close());
        buttons.add(save, cancel);

        layout.add(header, titleField, descriptionArea, buttons);
        dialog.add(layout);
        dialog.open();
    }
}
