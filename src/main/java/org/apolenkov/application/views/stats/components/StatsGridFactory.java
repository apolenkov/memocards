package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Map;
import org.apolenkov.application.domain.port.StatsRepository;

/**
 * Factory for creating statistics grid components.
 * Handles creation of horizontal grids for displaying statistics cards.
 */
public final class StatsGridFactory {

    // Dependencies
    private StatsCardFactory cardFactory;
    private StatsSectionHeaderFactory headerFactory;

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
     * Creates the overall statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for overall stats
     */
    public VerticalLayout createOverallStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = headerFactory.createStatsSectionHeader("stats.overall");
        HorizontalLayout statsGrid = createStatsGrid("stats-overall-grid", agg, true);

        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        headerFactory.setupCollapsibleSection(section, contentContainer);
        return section;
    }

    /**
     * Creates the today's statistics section with collapsible content.
     *
     * @param agg aggregated statistics data for all decks
     * @return configured vertical layout for today's stats
     */
    public VerticalLayout createTodayStatsSection(final Map<Long, StatsRepository.DeckAggregate> agg) {
        VerticalLayout section = headerFactory.createStatsSectionHeader("stats.today");
        HorizontalLayout statsGrid = createStatsGrid("stats-today-grid", agg, false);

        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.add(statsGrid);

        headerFactory.setupCollapsibleSection(section, contentContainer, true); // Today section is open by default
        return section;
    }

    /**
     * Creates a statistics grid with aggregated data.
     *
     * @param gridClassName CSS class name for the grid
     * @param agg aggregated statistics data
     * @param useOverallStats whether to use overall stats (true) or today's stats (false)
     * @return configured horizontal layout with statistics cards
     */
    private HorizontalLayout createStatsGrid(
            final String gridClassName,
            final Map<Long, StatsRepository.DeckAggregate> agg,
            final boolean useOverallStats) {

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);
        statsGrid.addClassName(gridClassName);
        statsGrid.setJustifyContentMode(JustifyContentMode.EVENLY);

        // Calculate aggregated statistics
        int sessions = agg.values().stream()
                .mapToInt(
                        useOverallStats
                                ? StatsRepository.DeckAggregate::sessionsAll
                                : StatsRepository.DeckAggregate::sessionsToday)
                .sum();

        int viewed = agg.values().stream()
                .mapToInt(
                        useOverallStats
                                ? StatsRepository.DeckAggregate::viewedAll
                                : StatsRepository.DeckAggregate::viewedToday)
                .sum();

        int correct = agg.values().stream()
                .mapToInt(
                        useOverallStats
                                ? StatsRepository.DeckAggregate::correctAll
                                : StatsRepository.DeckAggregate::correctToday)
                .sum();

        int hard = agg.values().stream()
                .mapToInt(
                        useOverallStats
                                ? StatsRepository.DeckAggregate::hardAll
                                : StatsRepository.DeckAggregate::hardToday)
                .sum();

        // Add statistics cards to grid
        statsGrid.add(
                cardFactory.createStatCard(StatsConstants.STATS_SESSIONS, sessions),
                cardFactory.createStatCard(StatsConstants.STATS_VIEWED, viewed),
                cardFactory.createStatCard(StatsConstants.STATS_CORRECT, correct),
                cardFactory.createStatCard(StatsConstants.STATS_HARD, hard));

        return statsGrid;
    }
}
