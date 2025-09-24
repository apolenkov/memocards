package org.apolenkov.application.views.deck.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.business.HomePresenter;
import org.apolenkov.application.views.deck.components.CreateDeckDialog;
import org.apolenkov.application.views.deck.components.DeckCard;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.LayoutHelper;

/**
 * View for displaying and managing user's flashcard decks.
 * This view provides functionality for listing all user's decks,
 * searching through them, and creating new decks. It serves as the main
 * dashboard for deck management operations.
 */
@Route(value = RouteConstants.DECKS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class DecksView extends BaseView {

    private static final String DECKS_TITLE_KEY = "main.decks";

    private final transient HomePresenter homePresenter;
    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private VerticalLayout deckList;

    /**
     * Creates a new DecksView with required dependencies.
     *
     * @param homePresenterValue service for home page operations and deck listing
     * @param deckUseCaseValue use case for deck management operations
     * @param userUseCaseValue service for user operations and authentication
     */
    public DecksView(
            final HomePresenter homePresenterValue,
            final DeckUseCase deckUseCaseValue,
            final UserUseCase userUseCaseValue) {
        this.homePresenter = homePresenterValue;
        this.deckUseCase = deckUseCaseValue;
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

        H2 title = new H2(getTranslation(DECKS_TITLE_KEY));
        title.addClassName("decks-view__title");

        TextField search = new TextField();
        search.setPlaceholder(getTranslation("home.search.placeholder"));
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.EAGER);
        search.setPrefixComponent(VaadinIcon.SEARCH.create());

        deckList = new VerticalLayout();
        deckList.setPadding(false);
        deckList.setSpacing(true);
        deckList.setWidthFull();
        deckList.setAlignItems(Alignment.CENTER);

        search.addValueChangeListener(e -> refreshDecks(e.getValue()));

        Button addDeckBtn = ButtonHelper.createButton(
                getTranslation("common.add"), VaadinIcon.PLUS, e -> openCreateDeckDialog(), ButtonVariant.LUMO_PRIMARY);
        addDeckBtn.setText(getTranslation("home.addDeck"));

        HorizontalLayout toolbar = LayoutHelper.createSearchRow(search, addDeckBtn);
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
        CreateDeckDialog dialog = new CreateDeckDialog(deckUseCase, userUseCase, created -> refreshDecks(""));
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
