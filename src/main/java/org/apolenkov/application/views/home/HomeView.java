package org.apolenkov.application.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.FlashcardService;
import org.apolenkov.application.views.deskview.DeskviewView;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@PageTitle("Колоды")
@Route("")
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    private final FlashcardService flashcardService;
    private VerticalLayout decksContainer;

    public HomeView(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
        
        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        
        createHeader();
        createSearchAndActions();
        createDecksGrid();
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
        
        TextField searchField = new TextField();
        searchField.setPlaceholder("Поиск колоды...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        
        Button addDeckButton = new Button("Добавить колоду", VaadinIcon.PLUS.create());
        addDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addDeckButton.addClickListener(e -> {
            // TODO: Open deck creation dialog
            getUI().ifPresent(ui -> ui.navigate("decks/new"));
        });
        
        searchLayout.add(searchField, addDeckButton);
        getContent().add(searchLayout);
    }

    private void createDecksGrid() {
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
        
        // Create grid layout for decks (2 columns)
        HorizontalLayout currentRow = null;
        
        for (int i = 0; i < decks.size(); i++) {
            if (i % 2 == 0) {
                currentRow = new HorizontalLayout();
                currentRow.setWidth("100%");
                currentRow.setSpacing(true);
                decksContainer.add(currentRow);
            }
            
            Div deckCard = createDeckCard(decks.get(i));
            deckCard.getStyle().set("flex", "1");
            currentRow.add(deckCard);
        }
    }

    private Div createDeckCard(Deck deck) {
        Div card = new Div();
        card.addClassName("deck-card");
        card.getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("cursor", "pointer")
            .set("transition", "box-shadow 0.2s")
            .set("background", "var(--lumo-base-color)");
        
        // Add hover effect
        card.getElement().addEventListener("mouseenter", e -> 
            card.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)"));
        card.getElement().addEventListener("mouseleave", e -> 
            card.getStyle().set("box-shadow", "none"));
        
        // Click to navigate to deck view
        card.addClickListener(e -> {
            System.out.println("Navigating to deck with ID: " + deck.getId());
            if (deck.getId() != null) {
                getUI().ifPresent(ui -> ui.navigate(DeskviewView.class, deck.getId().toString()));
            } else {
                Notification.show("Ошибка: ID колоды не найден", 3000, Notification.Position.MIDDLE);
            }
        });
        
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(false);
        cardContent.setSpacing(false);
        
        // Deck icon and title
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span icon = new Span("📚");
        icon.getStyle().set("font-size", "1.5em");
        
        H3 title = new H3(deck.getTitle() + " (" + deck.getFlashcardCount() + ")");
        title.getStyle().set("margin", "0").set("color", "var(--lumo-primary-text-color)");
        
        titleLayout.add(icon, title);
        
        // Description
        Span description = new Span(deck.getDescription());
        description.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)");
        
        // Progress bar (demo data)
        HorizontalLayout progressLayout = new HorizontalLayout();
        progressLayout.setSpacing(true);
        progressLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        progressLayout.setWidth("100%");
        
        Span progressLabel = new Span("Прогресс:");
        progressLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.random() * 0.8 + 0.2); // Demo progress
        progressBar.setWidth("100px");
        
        int progressPercent = (int) (progressBar.getValue() * 100);
        Span progressText = new Span(progressPercent + "%");
        progressText.getStyle().set("font-size", "var(--lumo-font-size-s)");
        
        progressLayout.add(progressLabel, progressBar, progressText);
        
        // Practice button
        Button practiceButton = new Button("▶ Начать практику");
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> {
            System.out.println("Navigating to practice with deck ID: " + deck.getId());
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
}
