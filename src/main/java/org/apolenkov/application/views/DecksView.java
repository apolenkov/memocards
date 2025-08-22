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

/**
 * View for displaying and managing user's flashcard decks.
 *
 * <p>This view provides functionality for listing all user's decks,
 * searching through them, and creating new decks. It serves as the main
 * dashboard for deck management operations.</p>
 *
 */
@Route(value = "decks", layout = PublicLayout.class)
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
public class DecksView extends VerticalLayout implements HasDynamicTitle {

    private final transient HomePresenter homePresenter;
    private final transient DeckFacade deckFacade;
    private final transient UserUseCase userUseCase;
    private final VerticalLayout deckList;
    private final TextField search;

    public DecksView(HomePresenter homePresenter, DeckFacade deckFacade, UserUseCase userUseCase) {
        this.homePresenter = homePresenter;
        this.deckFacade = deckFacade;
        this.userUseCase = userUseCase;

        setPadding(false);
        setSpacing(false);
        addClassName("decks-view");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("decks-view__content");

        H2 title = TextHelper.createPageTitle(getTranslation("home.title"));
        title.addClassName("decks-view__title");

        search = FormHelper.createOptionalTextField("", getTranslation("home.search.placeholder"));
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(IconHelper.createSearchIcon());

        deckList = new VerticalLayout();
        deckList.setPadding(false);
        deckList.setSpacing(true);
        deckList.setWidthFull();
        deckList.setAlignItems(Alignment.CENTER);

        search.addValueChangeListener(e -> refreshDecks(e.getValue()));

        content.add(deckList);
        add(content);

        refreshDecks("");
    }

    private void refreshDecks(String query) {
        deckList.removeAll();
        List<DeckCardViewModel> decks = homePresenter.listDecksForCurrentUser(query);

        VerticalLayout deckContainer = new VerticalLayout();
        deckContainer.setSpacing(true);
        deckContainer.setAlignItems(Alignment.CENTER);
        deckContainer.setWidthFull();
        deckContainer.addClassName("container-md");
        deckContainer.addClassName("decks-section");
        deckContainer.addClassName("surface-panel");

        H2 title = TextHelper.createPageTitle(getTranslation("home.title"));
        title.addClassName("decks-view__title");

        Button addDeckBtn = ButtonHelper.createPlusButton(e -> openCreateDeckDialog());
        addDeckBtn.setText(getTranslation("home.addDeck"));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addDeckBtn);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setJustifyContentMode(JustifyContentMode.CENTER);
        toolbar.addClassName("decks-toolbar");

        deckContainer.add(title, toolbar);

        decks.stream().map(DeckCard::new).forEach(deckContainer::add);

        deckList.add(deckContainer);
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
