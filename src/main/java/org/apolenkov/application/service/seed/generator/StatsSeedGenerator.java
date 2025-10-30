package org.apolenkov.application.service.seed.generator;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Generator for test statistics data.
 * Creates practice session statistics for load testing.
 */
@Component
@Profile({"dev", "test"})
public class StatsSeedGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsSeedGenerator.class);

    private static final int STATS_DAYS_TO_GENERATE = 90;
    private static final double STATS_PROBABILITY = 0.7;

    private final DeckRepository deckRepository;
    private final StatsRepository statsRepository;
    private final SecureRandom random = new SecureRandom();

    /**
     * Creates StatsSeedGenerator with required dependencies.
     *
     * @param deckRepositoryValue repository for reading decks
     * @param statsRepositoryValue repository for stats operations
     */
    public StatsSeedGenerator(final DeckRepository deckRepositoryValue, final StatsRepository statsRepositoryValue) {
        this.deckRepository = deckRepositoryValue;
        this.statsRepository = statsRepositoryValue;
    }

    /**
     * Generates statistics data for all user decks.
     *
     * @param users list of users to generate stats for
     * @return number of statistics records generated
     */
    public int generateStatistics(final List<User> users) {
        LOGGER.info("Generating statistics data...");
        List<Deck> allDecks = collectAllUserDecks(users);

        int statsGenerated = 0;
        for (Deck deck : allDecks) {
            statsGenerated += generateDeckStatistics(deck);
        }

        LOGGER.info("Successfully generated {} statistics records", statsGenerated);
        return statsGenerated;
    }

    /**
     * Collects all decks from all users.
     *
     * @param users list of users
     * @return combined list of all user decks
     */
    private List<Deck> collectAllUserDecks(final List<User> users) {
        List<Deck> allDecks = new ArrayList<>();
        for (User user : users) {
            allDecks.addAll(deckRepository.findByUserId(user.getId()));
        }
        return allDecks;
    }

    /**
     * Generates statistics for a single deck.
     *
     * @param deck the deck to generate stats for
     * @return number of statistics records generated for this deck
     */
    private int generateDeckStatistics(final Deck deck) {
        int statsGenerated = 0;

        for (int day = 0; day < STATS_DAYS_TO_GENERATE; day++) {
            if (shouldGenerateStatsForDay()) {
                SessionStatsDto stats = createSessionStats(deck);
                statsRepository.appendSession(stats, LocalDate.now().minusDays(day));
                statsGenerated++;
            }
        }

        return statsGenerated;
    }

    /**
     * Determines if statistics should be generated for a specific day.
     *
     * @return true if stats should be generated based on probability
     */
    private boolean shouldGenerateStatsForDay() {
        return random.nextDouble() < STATS_PROBABILITY;
    }

    /**
     * Creates session statistics for a deck.
     *
     * @param deck the deck
     * @return configured session stats
     */
    private SessionStatsDto createSessionStats(final Deck deck) {
        int viewed = random.nextInt(20) + 10;
        int correct = (int) (viewed * 0.8);
        int hard = random.nextInt(3);
        long duration = viewed * 30000L;
        long delay = viewed * 3000L;

        return SessionStatsDto.builder()
                .deckId(deck.getId())
                .viewed(viewed)
                .correct(correct)
                .hard(hard)
                .sessionDurationMs(duration)
                .totalAnswerDelayMs(delay)
                .knownCardIdsDelta(null)
                .build();
    }
}
