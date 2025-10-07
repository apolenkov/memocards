package org.apolenkov.application.views.deck.components.state;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.exceptions.EntityNotFoundException;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.deck.components.layout.DeckViewLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State management component for DeckView.
 * Handles loading states, data updates, and deck information management.
 *
 * <p>Features:
 * <ul>
 *   <li>Loading state management</li>
 *   <li>Deck data loading and validation</li>
 *   <li>Flashcard data management</li>
 *   <li>Deck information updates</li>
 * </ul>
 */
public final class DeckViewState extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckViewState.class);

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient FlashcardUseCase flashcardUseCase;

    // State
    private transient Deck currentDeck;
    private transient DeckViewLayout deckViewLayout;

    /**
     * Creates a new DeckViewState with required dependencies.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param flashcardUseCaseParam use case for flashcard operations
     */
    public DeckViewState(final DeckUseCase deckUseCaseParam, final FlashcardUseCase flashcardUseCaseParam) {
        this.deckUseCase = deckUseCaseParam;
        this.flashcardUseCase = flashcardUseCaseParam;
    }

    /**
     * Sets the deck view layout for state management.
     *
     * @param deckViewLayoutParam the deck view layout
     */
    public void setDeckViewLayout(final DeckViewLayout deckViewLayoutParam) {
        this.deckViewLayout = deckViewLayoutParam;
    }

    /**
     * Shows loading state while deck is being loaded.
     */
    public void showLoadingState() {
        getContent().removeAll();

        VerticalLayout loadingContainer = createLoadingContainer();
        VerticalLayout loadingSection = createLoadingSection();

        loadingContainer.add(loadingSection);
        getContent().add(loadingContainer);
    }

    /**
     * Creates the loading container layout.
     *
     * @return the configured loading container
     */
    private VerticalLayout createLoadingContainer() {
        VerticalLayout loadingContainer = new VerticalLayout();
        loadingContainer.setSpacing(true);
        loadingContainer.setWidthFull();
        loadingContainer.addClassName(DeckConstants.CONTAINER_MD_CLASS);
        loadingContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        return loadingContainer;
    }

    /**
     * Creates the loading section with title.
     *
     * @return the configured loading section
     */
    private VerticalLayout createLoadingSection() {
        VerticalLayout loadingSection = new VerticalLayout();
        loadingSection.setSpacing(true);
        loadingSection.setPadding(true);
        loadingSection.setWidthFull();
        loadingSection.addClassName(DeckConstants.DECK_VIEW_SECTION_CLASS);
        loadingSection.addClassName(DeckConstants.SURFACE_PANEL_CLASS);
        loadingSection.addClassName(DeckConstants.CONTAINER_MD_CLASS);

        H2 loadingTitle = new H2(getTranslation(DeckConstants.DECK_LOADING_STATE));
        loadingTitle.addClassName(DeckConstants.DECK_VIEW_TITLE_CLASS);

        loadingSection.add(loadingTitle);
        return loadingSection;
    }

    /**
     * Loads a deck by ID and initializes the state.
     *
     * @param deckId the ID of the deck to load
     * @throws EntityNotFoundException if deck is not found
     */
    public void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = deckUseCase.getDeckById(deckId);
        if (deckOpt.isPresent()) {
            setCurrentDeck(deckOpt.get());
            LOGGER.info("Deck loaded successfully: {}", currentDeck.getTitle());
        } else {
            LOGGER.warn("Deck not found with ID: {}", deckId);
            // Just throw the exception - it will be caught by EntityNotFoundErrorHandler
            throw new EntityNotFoundException(
                    String.valueOf(deckId), RouteConstants.DECKS_ROUTE, getTranslation(DeckConstants.DECK_NOT_FOUND));
        }
    }

    /**
     * Updates the display of deck information (title, stats, description).
     */
    public void updateDeckInfo() {
        if (currentDeck != null && deckViewLayout != null) {
            if (deckViewLayout.getDeckHeader() != null) {
                deckViewLayout.getDeckHeader().setDeckTitle(currentDeck.getTitle());
                deckViewLayout
                        .getDeckHeader()
                        .setDeckStats(getTranslation(
                                DeckConstants.DECK_COUNT, flashcardUseCase.countByDeckId(currentDeck.getId())));
            }
            if (deckViewLayout.getDeckInfo() != null) {
                String description = Optional.ofNullable(currentDeck.getDescription())
                        .filter(desc -> !desc.trim().isEmpty())
                        .orElse(getTranslation(DeckConstants.DECK_DESCRIPTION_EMPTY));
                deckViewLayout.getDeckInfo().setDescription(description);
            }
        }
    }

    /**
     * Loads flashcards for the current deck and updates the grid.
     */
    public void loadFlashcards() {
        if (currentDeck != null && deckViewLayout != null && deckViewLayout.getDeckGrid() != null) {
            List<Flashcard> flashcards = flashcardUseCase.getFlashcardsByDeckId(currentDeck.getId());
            deckViewLayout.getDeckGrid().setCurrentDeckId(currentDeck.getId());
            deckViewLayout.getDeckGrid().setFlashcards(flashcards);
            LOGGER.info("Loaded {} flashcards for deck: {}", flashcards.size(), currentDeck.getTitle());
        }
    }

    /**
     * Gets the current deck.
     *
     * @return the current deck, or null if not loaded
     */
    public Deck getCurrentDeck() {
        return currentDeck;
    }

    /**
     * Sets the current deck.
     *
     * @param deck the deck to set as current
     */
    public void setCurrentDeck(final Deck deck) {
        this.currentDeck = deck;
    }
}
