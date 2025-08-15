package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.components.CreateDeckDialog;
import org.apolenkov.application.views.components.DeckCard;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.apolenkov.application.views.home.HomePresenter;

@Route(value = "decks", layout = PublicLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class DecksView extends VerticalLayout implements HasDynamicTitle {

    private final HomePresenter homePresenter;
    private final DeckFacade deckFacade;
    private final UserUseCase userUseCase;
    private final VerticalLayout deckList;

    public DecksView(HomePresenter homePresenter, DeckFacade deckFacade, UserUseCase userUseCase) {
        this.homePresenter = homePresenter;
        this.deckFacade = deckFacade;
        this.userUseCase = userUseCase;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        H2 title = new H2(getTranslation("home.title"));

        TextField search = new TextField();
        search.setPlaceholder(getTranslation("home.search"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.EAGER);

        Button addDeckBtn = new Button(getTranslation("home.addDeck"));
        addDeckBtn.addClickListener(e -> openCreateDeckDialog());

        HorizontalLayout toolbar = new HorizontalLayout(search, addDeckBtn);
        toolbar.setAlignItems(Alignment.END);

        deckList = new VerticalLayout();
        deckList.setPadding(false);
        deckList.setSpacing(true);
        deckList.setWidthFull();

        search.addValueChangeListener(e -> refreshDecks(e.getValue()));

        content.add(title, toolbar, deckList);
        add(content);

        refreshDecks("");
    }

    private void refreshDecks(String query) {
        deckList.removeAll();
        List<DeckCardViewModel> decks = homePresenter.listDecksForCurrentUser(query);
        decks.stream().map(DeckCard::new).forEach(deckList::add);
    }

    private void openCreateDeckDialog() {
        CreateDeckDialog dialog = new CreateDeckDialog(deckFacade, userUseCase, created -> refreshDecks(""));
        dialog.open();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("home.title");
    }
}
