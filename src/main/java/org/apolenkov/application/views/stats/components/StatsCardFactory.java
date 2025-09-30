package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.shared.interfaces.TranslationProvider;

/**
 * Factory for creating statistics card components.
 * Handles creation of various types of statistics cards with consistent styling.
 */
public final class StatsCardFactory {

    // Translation provider interface
    private TranslationProvider translationProvider;

    /**
     * Sets the translation provider for internationalization.
     *
     * @param translationProviderParam provider for translations
     */
    public void setTranslationProvider(final TranslationProvider translationProviderParam) {
        this.translationProvider = translationProviderParam;
    }

    /**
     * Creates a statistics card with label, value and CSS modifier.
     *
     * @param labelKey translation key for the label
     * @param value numeric value to display
     * @return configured statistics card component
     */
    public Div createStatCard(final String labelKey, final int value) {
        Div card = new Div();
        card.addClassName("stats-card");
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div valueDiv = new Div();
        valueDiv.addClassName("stats-card__value");
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-card__label");
        labelDiv.setText(getTranslation(labelKey));

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
        card.addClassName("deck-stats-card");
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div header = new Div();
        header.addClassName("deck-stats-card__header");

        H3 deckTitle = new H3(deck.getTitle());
        deckTitle.addClassName("deck-stats-card__title");

        header.add(deckTitle);

        HorizontalLayout deckStatsGrid = new HorizontalLayout();
        deckStatsGrid.setWidthFull();
        deckStatsGrid.setSpacing(true);
        deckStatsGrid.addClassName("stats-deck-grid");
        deckStatsGrid.setJustifyContentMode(JustifyContentMode.EVENLY);

        deckStatsGrid.add(
                createDeckStatItem(StatsConstants.STATS_SESSIONS, stats.sessionsAll(), stats.sessionsToday()),
                createDeckStatItem(StatsConstants.STATS_VIEWED, stats.viewedAll(), stats.viewedToday()),
                createDeckStatItem(StatsConstants.STATS_CORRECT, stats.correctAll(), stats.correctToday()),
                createDeckStatItem(StatsConstants.STATS_HARD, stats.hardAll(), stats.hardToday()));

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
        item.addClassName("stats-deck-item");
        item.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div totalDiv = new Div();
        totalDiv.addClassName("stats-deck-item__total");
        totalDiv.setText(String.valueOf(total));

        Div todayDiv = new Div();
        todayDiv.addClassName("stats-deck-item__today");
        todayDiv.setText("+" + today);

        Div labelDiv = new Div();
        labelDiv.addClassName("stats-deck-item__label");
        labelDiv.setText(getTranslation(labelKey));

        item.add(totalDiv, todayDiv, labelDiv);
        return item;
    }

    /**
     * Gets translation for the given key.
     *
     * @param key the translation key
     * @param params optional parameters for message formatting
     * @return translated text
     */
    private String getTranslation(final String key, final Object... params) {
        if (translationProvider == null) {
            return key;
        }
        return translationProvider.getTranslation(key, params);
    }
}
