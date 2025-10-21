package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.stats.constants.StatsConstants;

/**
 * UI component for displaying compact deck statistics in horizontal layout.
 * Shows deck title and multiple stat metrics in a single row for efficient space usage.
 */
public final class DeckStatCardCompact extends Composite<Div> {

    private final transient Deck deck;
    private final transient StatsRepository.DeckAggregate stats;

    /**
     * Creates a new compact deck statistics card.
     *
     * @param deckParam the deck to display statistics for
     * @param statsParam aggregated statistics for the deck
     */
    public DeckStatCardCompact(final Deck deckParam, final StatsRepository.DeckAggregate statsParam) {
        this.deck = deckParam;
        this.stats = statsParam;
    }

    @Override
    protected Div initContent() {
        Div card = new Div();
        card.addClassName(StatsConstants.DECK_STATS_CARD_CLASS);
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);
        card.addClassName("deck-stat-card-compact");

        // Header with deck title
        Div header = new Div();
        header.addClassName(StatsConstants.DECK_STATS_CARD_HEADER_CLASS);
        header.addClassName("deck-stat-card-compact__header");

        H3 deckTitle = new H3(deck.getTitle());
        deckTitle.addClassName(StatsConstants.DECK_STATS_CARD_TITLE_CLASS);
        deckTitle.addClassName("deck-stat-card-compact__title");
        header.add(deckTitle);

        // Compact stats grid - horizontal layout
        HorizontalLayout compactStatsGrid = new HorizontalLayout();
        compactStatsGrid.setWidthFull();
        compactStatsGrid.setSpacing(true);
        compactStatsGrid.addClassName("deck-stat-card-compact__grid");
        compactStatsGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        compactStatsGrid.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add compact stat items
        compactStatsGrid.add(
                createCompactStatItem(StatsConstants.STATS_SESSIONS_KEY, stats.sessionsAll(), stats.sessionsToday()),
                createCompactStatItem(StatsConstants.STATS_VIEWED_KEY, stats.viewedAll(), stats.viewedToday()),
                createCompactStatItem(StatsConstants.STATS_CORRECT_KEY, stats.correctAll(), stats.correctToday()),
                createCompactStatItem(StatsConstants.STATS_HARD_KEY, stats.hardAll(), stats.hardToday()));

        card.add(header, compactStatsGrid);
        return card;
    }

    /**
     * Creates a compact statistics item showing total and today's values in minimal space.
     *
     * @param labelKey translation key for the label
     * @param total total value for all time
     * @param today today's value
     * @return configured compact statistics item component
     */
    private Div createCompactStatItem(final String labelKey, final int total, final int today) {
        Div item = new Div();
        item.addClassName("deck-stat-card-compact__item");

        // Total value (main)
        Div totalDiv = new Div();
        totalDiv.addClassName("deck-stat-card-compact__total");
        totalDiv.setText(String.valueOf(total));

        // Today's value (smaller, with + prefix)
        Div todayDiv = new Div();
        todayDiv.addClassName("deck-stat-card-compact__today");
        todayDiv.setText("+" + today);

        // Label (smallest)
        Div labelDiv = new Div();
        labelDiv.addClassName("deck-stat-card-compact__label");
        labelDiv.setText(getTranslation(labelKey));

        item.add(totalDiv, todayDiv, labelDiv);
        return item;
    }
}
