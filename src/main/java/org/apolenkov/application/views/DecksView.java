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
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.IconHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.TextHelper;

@Route(value = "decks", layout = PublicLayout.class)
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
public class DecksView extends VerticalLayout implements HasDynamicTitle {

    private final transient HomePresenter homePresenter;
    private final transient DeckFacade deckFacade;
    private final transient UserUseCase userUseCase;
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
        content.addClassName("decklist-view");

        H2 title = TextHelper.createPageTitle(getTranslation("home.title"));

        TextField search = FormHelper.createOptionalTextField("", getTranslation("home.search.placeholder"));
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(IconHelper.createSearchIcon());
        search.setMaxWidth("250px");

        Button addDeckBtn = ButtonHelper.createPlusButton(e -> openCreateDeckDialog());
        addDeckBtn.setText(getTranslation("home.addDeck"));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addDeckBtn);
        toolbar.addClassName("toolbar-with-bottom-margin");

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
