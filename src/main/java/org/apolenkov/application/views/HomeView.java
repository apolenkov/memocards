package org.apolenkov.application.views;

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
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.views.components.CreateDeckDialog;
import org.apolenkov.application.views.components.DeckCard;
import org.apolenkov.application.views.home.HomePresenter;
import org.apolenkov.application.views.home.DeckCardViewModel;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@PageTitle("Колоды")
@Route("")
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    private final FlashcardService flashcardService;
    private final StatsService statsService;
    private VerticalLayout decksContainer;
    private final HomePresenter presenter;
    private TextField searchField;

    public HomeView(FlashcardService flashcardService, StatsService statsService) {
        this.flashcardService = flashcardService;
        this.statsService = statsService;
        this.presenter = new HomePresenter(flashcardService, statsService);
        getContent().addClassName("home-view");
        
        createHeader();
        createSearchAndActions();
        createDecksList();
        loadDecks();
    }

    private void createHeader() {
        H2 title = new H2(getTranslation("home.title"));
        title.addClassName("home-view__title");
        getContent().add(title);
    }

    private void createSearchAndActions() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.addClassName("home-view__toolbar");
        
        searchField = new TextField();
        searchField.setPlaceholder(getTranslation("home.search"));
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("home-view__search");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> loadDecks());
        
        Button addDeckButton = new Button(getTranslation("home.addDeck"), VaadinIcon.PLUS.create());
        addDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addDeckButton.addClickListener(e -> openCreateDeckDialog());
        
        searchLayout.add(searchField, addDeckButton);
        getContent().add(searchLayout);
    }

    private void createDecksList() {
        decksContainer = new VerticalLayout();
        decksContainer.addClassName("home-view__decks");
        getContent().add(decksContainer);
    }

    private void loadDecks() {
        decksContainer.removeAll();
        
        String query = searchField != null ? searchField.getValue() : null;
        for (DeckCardViewModel vm : presenter.listDecksForCurrentUser(query)) {
            decksContainer.add(new DeckCard(vm));
        }
    }

    private void openCreateDeckDialog() {
        CreateDeckDialog dialog = new CreateDeckDialog(flashcardService, saved -> loadDecks());
        dialog.open();
    }
}
