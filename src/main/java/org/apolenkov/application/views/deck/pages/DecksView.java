package org.apolenkov.application.views.deck.pages;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.business.DeckListPresenter;
import org.apolenkov.application.views.deck.components.dialogs.CreateDeckDialog;
import org.apolenkov.application.views.deck.components.list.DeckContainer;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.base.BaseView;

/**
 * View for displaying and managing user's flashcard decks.
 * This view provides functionality for listing all user's decks,
 * searching through them, and creating new decks. It serves as the main
 * dashboard for deck management operations.
 */
@Route(value = RouteConstants.DECKS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public final class DecksView extends BaseView {

    // Dependencies
    private final transient DeckListPresenter deckListPresenter;
    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;

    // UI Components
    private DeckContainer deckContainer;

    // Event Registrations
    private Registration searchListenerRegistration;
    private Registration addClickListenerRegistration;

    /**
     * Creates a new DecksView with required dependencies.
     *
     * @param deckListPresenterValue service for home page operations and deck listing
     * @param deckUseCaseValue use case for deck management operations
     * @param userUseCaseValue service for user operations and authentication
     */
    public DecksView(
            final DeckListPresenter deckListPresenterValue,
            final DeckUseCase deckUseCaseValue,
            final UserUseCase userUseCaseValue) {
        this.deckListPresenter = deckListPresenterValue;
        this.deckUseCase = deckUseCaseValue;
        this.userUseCase = userUseCaseValue;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * Sets up layout, creates components, configures event listeners, and loads data.
     */
    @PostConstruct
    public void init() {
        // Configure main view layout
        setPadding(false);
        setSpacing(false);
        addClassName(DeckConstants.DECKS_VIEW_CLASS);

        // Create content layout
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName(DeckConstants.DECKS_VIEW_CONTENT_CLASS);
        add(content);

        // Create and add deck container
        deckContainer = new DeckContainer();
        content.add(deckContainer);
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (searchListenerRegistration == null) {
            searchListenerRegistration = deckContainer.getToolbar().addSearchListener(this::refreshDecks);
        }
        if (addClickListenerRegistration == null) {
            addClickListenerRegistration = deckContainer.getToolbar().addAddClickListener(e -> openCreateDeckDialog());
        }
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
        final java.util.List<DeckCardViewModel> decks = deckListPresenter.listDecksForCurrentUser(query);
        deckContainer.refreshDecks(decks);
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
        return getTranslation(DeckConstants.DECKS_TITLE_KEY);
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (searchListenerRegistration != null) {
            searchListenerRegistration.remove();
            searchListenerRegistration = null;
        }

        if (addClickListenerRegistration != null) {
            addClickListenerRegistration.remove();
            addClickListenerRegistration = null;
        }
    }
}
