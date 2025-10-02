package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.interfaces.TranslationProvider;

/**
 * Builder for creating statistics card components.
 * Handles creation of various types of statistics cards with consistent styling.
 */
public final class StatsCardBuilder {

    private final TranslationProvider translationProvider;

    /**
     * Creates a new StatsCardBuilder with translation provider.
     *
     * @param translationProviderParam provider for translations
     */
    public StatsCardBuilder(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Creates a single statistics card.
     *
     * @param labelKey translation key for the label
     * @param value numeric value to display
     * @return configured statistics card component
     */
    public Div createStatCard(final String labelKey, final int value) {
        Div card = new Div();
        card.addClassName(StatsConstants.STATS_CARD_CLASS);
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div valueDiv = new Div();
        valueDiv.addClassName(StatsConstants.STATS_CARD_VALUE_CLASS);
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName(StatsConstants.STATS_CARD_LABEL_CLASS);
        labelDiv.setText(translationProvider.getTranslation(labelKey));

        card.add(valueDiv, labelDiv);
        return card;
    }

    /**
     * Creates a deck statistics card with comprehensive stats display.
     *
     * @param deck the deck to display statistics for
     * @param stats aggregated statistics for the deck
     * @return configured deck statistics card component
     */
    public Div createDeckStatCard(final Deck deck, final StatsRepository.DeckAggregate stats) {
        Div card = new Div();
        card.addClassName(StatsConstants.DECK_STATS_CARD_CLASS);
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        // Header
        Div header = new Div();
        header.addClassName(StatsConstants.DECK_STATS_CARD_HEADER_CLASS);

        H3 deckTitle = new H3(deck.getTitle());
        deckTitle.addClassName(StatsConstants.DECK_STATS_CARD_TITLE_CLASS);
        header.add(deckTitle);

        // Stats grid
        HorizontalLayout deckStatsGrid = new HorizontalLayout();
        deckStatsGrid.setWidthFull();
        deckStatsGrid.setSpacing(true);
        deckStatsGrid.addClassName(StatsConstants.STATS_DECK_GRID_CLASS);
        deckStatsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.EVENLY);

        deckStatsGrid.add(
                createDeckStatItem(StatsConstants.STATS_SESSIONS_KEY, stats.sessionsAll(), stats.sessionsToday()),
                createDeckStatItem(StatsConstants.STATS_VIEWED_KEY, stats.viewedAll(), stats.viewedToday()),
                createDeckStatItem(StatsConstants.STATS_CORRECT_KEY, stats.correctAll(), stats.correctToday()),
                createDeckStatItem(StatsConstants.STATS_HARD_KEY, stats.hardAll(), stats.hardToday()));

        card.add(header, deckStatsGrid);
        return card;
    }

    /**
     * Creates a deck statistics item showing total and today's values.
     *
     * @param labelKey translation key for the label
     * @param total total value for all time
     * @param today today's value
     * @return configured deck statistics item component
     */
    public Div createDeckStatItem(final String labelKey, final int total, final int today) {
        Div item = new Div();
        item.addClassName(StatsConstants.STATS_DECK_ITEM_CLASS);
        item.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div totalDiv = new Div();
        totalDiv.addClassName(StatsConstants.STATS_DECK_ITEM_TOTAL_CLASS);
        totalDiv.setText(String.valueOf(total));

        Div todayDiv = new Div();
        todayDiv.addClassName(StatsConstants.STATS_DECK_ITEM_TODAY_CLASS);
        todayDiv.setText("+" + today);

        Div labelDiv = new Div();
        labelDiv.addClassName(StatsConstants.STATS_DECK_ITEM_LABEL_CLASS);
        labelDiv.setText(translationProvider.getTranslation(labelKey));

        item.add(totalDiv, todayDiv, labelDiv);
        return item;
    }
}
