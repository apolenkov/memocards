package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import org.apolenkov.application.config.security.SecurityConstants;
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
 * This view provides functionality for listing all user's decks,
 * searching through them, and creating new decks. It serves as the main
 * dashboard for deck management operations.
 */
@Route(value = "decks", layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class DecksView extends VerticalLayout implements HasDynamicTitle {

    private static final String DECKS_TITLE_KEY = "main.decks";

    private final transient HomePresenter homePresenter;
    private final transient DeckFacade deckFacade;
    private final transient UserUseCase userUseCase;
    private VerticalLayout deckList;

    /**
     * Creates a new DecksView with required dependencies.
     *
     * @param homePresenterValue service for home page operations and deck listing
     * @param deckFacadeValue service for deck management operations
     * @param userUseCaseValue service for user operations and authentication
     */
    public DecksView(
            final HomePresenter homePresenterValue,
            final DeckFacade deckFacadeValue,
            final UserUseCase userUseCaseValue) {
        this.homePresenter = homePresenterValue;
        this.deckFacade = deckFacadeValue;
        this.userUseCase = userUseCaseValue;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        setPadding(false);
        setSpacing(false);
        addClassName("decks-view");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("decks-view__content");

        H2 title = TextHelper.createPageTitle(getTranslation(DECKS_TITLE_KEY));
        title.addClassName("decks-view__title");

        TextField search = FormHelper.createOptionalTextField("", getTranslation("home.search.placeholder"));
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(IconHelper.createSearchIcon());

        deckList = new VerticalLayout();
        deckList.setPadding(false);
        deckList.setSpacing(true);
        deckList.setWidthFull();
        deckList.setAlignItems(Alignment.CENTER);

        search.addValueChangeListener(e -> refreshDecks(e.getValue()));

        Button addDeckBtn = ButtonHelper.createPlusButton(e -> openCreateDeckDialog());
        addDeckBtn.setText(getTranslation("home.addDeck"));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addDeckBtn);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setJustifyContentMode(JustifyContentMode.CENTER);
        toolbar.addClassName("decks-toolbar");

        VerticalLayout deckContainer = new VerticalLayout();
        deckContainer.setSpacing(true);
        deckContainer.setAlignItems(Alignment.CENTER);
        deckContainer.setWidthFull();
        deckContainer.addClassName("container-md");
        deckContainer.addClassName("decks-section");
        deckContainer.addClassName("surface-panel");

        deckContainer.add(title, toolbar, deckList);

        content.add(deckContainer);
        add(content);

        refreshDecks("");
    }

    /**
     * Refreshes the deck list display based on the search query.
     * This method updates the deck list by filtering decks based on the
     * provided search query. It handles empty results gracefully by displaying
     * an appropriate message when no decks match the search criteria.
     *
     * @param query the search query to filter decks by title or description
     */
    private void refreshDecks(final String query) {
        deckList.removeAll();
        List<DeckCardViewModel> decks = homePresenter.listDecksForCurrentUser(query);
        if (decks == null || decks.isEmpty()) {
            Span empty = new Span(getTranslation("home.search.noResults"));
            empty.addClassName("decks-empty-message");
            deckList.add(empty);
            return;
        }
        decks.stream().map(DeckCard::new).forEach(deckList::add);
    }

    /**
     * Opens the dialog for creating a new deck.
     * Creates and displays a dialog that allows users to input deck details
     * and create new flashcard decks. After successful creation, the deck list
     * is automatically refreshed to show the new deck.
     */
    private void openCreateDeckDialog() {
        CreateDeckDialog dialog = new CreateDeckDialog(deckFacade, userUseCase, created -> refreshDecks(""));
        dialog.open();
    }

    /**
     * Returns the page title for this view.
     * Implements the HasDynamicTitle interface to provide localized
     * page titles for the deck management view.
     *
     * @return the localized page title for the decks view
     */
    @Override
    public String getPageTitle() {
        return getTranslation(DECKS_TITLE_KEY);
    }
}
