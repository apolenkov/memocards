package org.apolenkov.application.views.stats.components;

import java.util.Map;
import org.apolenkov.application.domain.port.StatsRepository;

/**
 * Calculator for aggregated statistics across all decks.
 * Handles calculation of overall and today's statistics.
 */
public final class StatsCalculator {

    private final Map<Long, StatsRepository.DeckAggregate> aggregates;

    /**
     * Creates a new StatsCalculator with aggregated data.
     *
     * @param aggregatesParam aggregated statistics for all decks
     */
    public StatsCalculator(final Map<Long, StatsRepository.DeckAggregate> aggregatesParam) {
        this.aggregates = aggregatesParam;
    }

    /**
     * Calculates aggregated statistics for all decks.
     *
     * @param useOverallStats whether to use overall stats (true) or today's stats (false)
     * @return calculated statistics values
     */
    public StatsValues calculateStats(final boolean useOverallStats) {
        return new StatsValues(
                aggregates.values().stream()
                        .mapToInt(
                                useOverallStats
                                        ? StatsRepository.DeckAggregate::sessionsAll
                                        : StatsRepository.DeckAggregate::sessionsToday)
                        .sum(),
                aggregates.values().stream()
                        .mapToInt(
                                useOverallStats
                                        ? StatsRepository.DeckAggregate::viewedAll
                                        : StatsRepository.DeckAggregate::viewedToday)
                        .sum(),
                aggregates.values().stream()
                        .mapToInt(
                                useOverallStats
                                        ? StatsRepository.DeckAggregate::correctAll
                                        : StatsRepository.DeckAggregate::correctToday)
                        .sum(),
                aggregates.values().stream()
                        .mapToInt(
                                useOverallStats
                                        ? StatsRepository.DeckAggregate::hardAll
                                        : StatsRepository.DeckAggregate::hardToday)
                        .sum());
    }

    /**
     * Data container for statistics values.
     *
     * @param sessions number of sessions
     * @param viewed number of viewed items
     * @param correct number of correct answers
     * @param hard number of hard items
     */
    public record StatsValues(int sessions, int viewed, int correct, int hard) {}
}
