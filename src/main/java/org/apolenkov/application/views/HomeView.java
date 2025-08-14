package org.apolenkov.application.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.function.Consumer;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.components.CreateDeckDialog;
import org.apolenkov.application.views.components.DeckCard;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.apolenkov.application.views.home.HomePresenter;

@Route(value = "home", layout = MainLayout.class)
@PermitAll
public class HomeView extends Composite<VerticalLayout> implements HasDynamicTitle {

    private final DeckFacade deckFacade;
    private final UserUseCase userUseCase;
    private VerticalLayout decksContainer;
    private final HomePresenter presenter;
    private TextField searchField;

    public HomeView(DeckFacade deckFacade, UserUseCase userUseCase, HomePresenter presenter) {
        this.deckFacade = deckFacade;
        this.userUseCase = userUseCase;
        this.presenter = presenter;
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
        Consumer<Deck> onCreated = d -> loadDecks();
        CreateDeckDialog dialog = new CreateDeckDialog(deckFacade, userUseCase, onCreated);
        dialog.open();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("home.title");
    }
}
