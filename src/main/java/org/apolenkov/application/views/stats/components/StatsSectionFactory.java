package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.interfaces.TranslationProvider;

/**
 * Factory for creating statistics section components.
 * Coordinates creation of collapsible sections with consistent styling and behavior.
 * Delegates specific responsibilities to specialized factories.
 */
public final class StatsSectionFactory {

    // Dependencies
    private StatsCardFactory cardFactory;
    private StatsGridFactory gridFactory;
    private DeckStatsPaginationFactory paginationFactory;
    private TranslationProvider translationProvider;

    /**
     * Sets the card factory for creating statistics cards.
     *
     * @param cardFactoryParam factory for creating cards
     */
    public void setCardFactory(final StatsCardFactory cardFactoryParam) {
        this.cardFactory = cardFactoryParam;
        initializeFactories();
    }

    /**
     * Sets the translation provider for localized strings.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
        initializeFactories();
    }

    /**
     * Initializes all factory dependencies.
     */
    private void initializeFactories() {
        if (cardFactory != null && translationProvider != null) {
            // Initialize header factory
            StatsSectionHeaderFactory headerFactory = new StatsSectionHeaderFactory();
            headerFactory.setTranslationProvider(translationProvider);

            // Initialize grid factory
            gridFactory = new StatsGridFactory();
            gridFactory.setCardFactory(cardFactory);
            gridFactory.setHeaderFactory(headerFactory);

            // Initialize pagination factory
            paginationFactory = new DeckStatsPaginationFactory();
            paginationFactory.setCardFactory(cardFactory);
            paginationFactory.setHeaderFactory(headerFactory);
            paginationFactory.setTranslationProvider(translationProvider);
        }
    }

    /**
     * Creates the overall statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for overall stats
     */
    public VerticalLayout createOverallStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        return gridFactory.createOverallStatsSection(agg);
    }

    /**
     * Creates the today's statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for today's stats
     */
    public VerticalLayout createTodayStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        return gridFactory.createTodayStatsSection(agg);
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
        return paginationFactory.createDeckStatsSection(decks, agg);
    }
}
