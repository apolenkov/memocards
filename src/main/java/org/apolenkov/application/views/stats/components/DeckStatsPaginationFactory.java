package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.interfaces.TranslationProvider;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Factory for creating deck statistics pagination components.
 * Handles creation of paginated deck statistics with navigation controls.
 */
public final class DeckStatsPaginationFactory {

    // Dependencies
    private StatsCardFactory cardFactory;
    private StatsSectionHeaderFactory headerFactory;
    private TranslationProvider translationProvider;

    /**
     * Sets the card factory for creating statistics cards.
     *
     * @param cardFactoryParam factory for creating cards
     */
    public void setCardFactory(final StatsCardFactory cardFactoryParam) {
        this.cardFactory = cardFactoryParam;
    }

    /**
     * Sets the header factory for creating section headers.
     *
     * @param headerFactoryParam factory for creating headers
     */
    public void setHeaderFactory(final StatsSectionHeaderFactory headerFactoryParam) {
        this.headerFactory = headerFactoryParam;
    }

    /**
     * Sets the translation provider for localized strings.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Creates the deck-specific statistics section with collapsible content.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for deck stats
     */
    public VerticalLayout createDeckStatsSection(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = headerFactory.createStatsSectionHeader("stats.byDeck");
        VerticalLayout contentContainer = createDeckStatsContent(decks, agg);
        headerFactory.setupCollapsibleSection(section, contentContainer);
        return section;
    }

    /**
     * Creates the content container for deck statistics with pagination.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout with paginated deck stats
     */
    private VerticalLayout createDeckStatsContent(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);

        VerticalLayout paginatedContainer = createPaginatedDeckStats(decks, agg);
        contentContainer.add(paginatedContainer);

        return contentContainer;
    }

    /**
     * Creates paginated container for deck statistics with navigation controls.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout with pagination
     */
    private VerticalLayout createPaginatedDeckStats(
            final List<Deck> decks, final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout paginatedContainer = new VerticalLayout();
        paginatedContainer.setSpacing(true);
        paginatedContainer.setAlignItems(Alignment.CENTER);

        // Initialize pagination state
        final int[] currentIndex = {0};
        final int totalDecks = decks.size();

        // Create navigation controls
        HorizontalLayout navigationLayout = createDeckNavigationControls();
        Span pageIndicator = (Span) navigationLayout
                .getChildren()
                .filter(Span.class::isInstance)
                .findFirst()
                .orElse(null);

        // Current deck display
        Div currentDeckContainer = new Div();
        currentDeckContainer.setWidthFull();
        currentDeckContainer.addClassName("stats-current-deck__container");

        // Create update display function
        IntConsumer updateDisplay = createDeckUpdateDisplayFunction(
                decks, agg, currentDeckContainer, pageIndicator, navigationLayout, totalDecks);

        // Setup button click handlers
        setupDeckNavigationHandlers(navigationLayout, currentIndex, updateDisplay, totalDecks);

        // Initial display
        updateDisplay.accept(0);

        paginatedContainer.add(navigationLayout, currentDeckContainer);
        return paginatedContainer;
    }

    /**
     * Creates navigation controls for deck statistics pagination.
     *
     * @return configured horizontal layout with navigation buttons
     */
    private HorizontalLayout createDeckNavigationControls() {
        HorizontalLayout navigationLayout = new HorizontalLayout();
        navigationLayout.setSpacing(true);
        navigationLayout.setAlignItems(Alignment.CENTER);
        navigationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Button prevButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_LEFT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        prevButton
                .getElement()
                .setAttribute(StatsConstants.TITLE_ATTRIBUTE, translationProvider.getTranslation("stats.previousDeck"));

        Button nextButton = ButtonHelper.createIconButton(
                VaadinIcon.CHEVRON_RIGHT, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        nextButton
                .getElement()
                .setAttribute(StatsConstants.TITLE_ATTRIBUTE, translationProvider.getTranslation("stats.nextDeck"));

        // Page indicator
        Span pageIndicator = new Span();
        pageIndicator.addClassName("stats-pagination__indicator");

        navigationLayout.add(prevButton, pageIndicator, nextButton);
        return navigationLayout;
    }

    /**
     * Creates the update display function for deck statistics.
     *
     * @param decks list of user's decks
     * @param agg aggregated statistics data for all decks
     * @param currentDeckContainer container for current deck display
     * @param pageIndicator span for page indicator
     * @param navigationLayout layout containing navigation buttons
     * @param totalDecks total number of decks
     * @return function to update display based on current index
     */
    private IntConsumer createDeckUpdateDisplayFunction(
            final List<Deck> decks,
            final Map<Long, StatsRepository.DeckAggregate> agg,
            final Div currentDeckContainer,
            final Span pageIndicator,
            final HorizontalLayout navigationLayout,
            final int totalDecks) {
        return index -> {
            if (totalDecks == 0) {
                currentDeckContainer.removeAll();
                currentDeckContainer.add(new Span(translationProvider.getTranslation("stats.noDecks")));
                pageIndicator.setText("");
                setNavigationButtonsEnabled(navigationLayout, false, false);
                return;
            }

            currentDeckContainer.removeAll();
            Deck currentDeck = decks.get(index);
            var stats =
                    agg.getOrDefault(currentDeck.getId(), new StatsRepository.DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0));
            currentDeckContainer.add(cardFactory.createDeckStatCard(currentDeck, stats));

            // Update page indicator
            pageIndicator.setText(translationProvider.getTranslation("stats.deckPage", index + 1, totalDecks));

            // Update button states
            setNavigationButtonsEnabled(navigationLayout, index > 0, index < totalDecks - 1);
        };
    }

    /**
     * Sets up click handlers for deck navigation buttons.
     *
     * @param navigationLayout layout containing navigation buttons
     * @param currentIndex current page index (mutable array)
     * @param updateDisplay function to update display
     * @param totalDecks total number of decks
     */
    private void setupDeckNavigationHandlers(
            final HorizontalLayout navigationLayout,
            final int[] currentIndex,
            final IntConsumer updateDisplay,
            final int totalDecks) {
        Button prevButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);
        Button nextButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .skip(1)
                .findFirst()
                .orElse(null);

        if (prevButton != null) {
            prevButton.addClickListener(e -> {
                if (currentIndex[0] > 0) {
                    currentIndex[0]--;
                    updateDisplay.accept(currentIndex[0]);
                }
            });
        }

        if (nextButton != null) {
            nextButton.addClickListener(e -> {
                if (currentIndex[0] < totalDecks - 1) {
                    currentIndex[0]++;
                    updateDisplay.accept(currentIndex[0]);
                }
            });
        }
    }

    /**
     * Sets the enabled state of navigation buttons.
     *
     * @param navigationLayout layout containing navigation buttons
     * @param prevEnabled whether previous button should be enabled
     * @param nextEnabled whether next button should be enabled
     */
    private void setNavigationButtonsEnabled(
            final HorizontalLayout navigationLayout, final boolean prevEnabled, final boolean nextEnabled) {
        Button prevButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .findFirst()
                .orElse(null);
        Button nextButton = (Button) navigationLayout
                .getChildren()
                .filter(Button.class::isInstance)
                .skip(1)
                .findFirst()
                .orElse(null);

        if (prevButton != null) {
            prevButton.setEnabled(prevEnabled);
        }
        if (nextButton != null) {
            nextButton.setEnabled(nextEnabled);
        }
    }
}
