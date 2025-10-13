package org.apolenkov.application.service.seed;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apolenkov.application.config.seed.SeedConfig;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.seed.generator.DeckAndCardSeedGenerator;
import org.apolenkov.application.service.seed.generator.NewsSeedGenerator;
import org.apolenkov.application.service.seed.generator.StatsSeedGenerator;
import org.apolenkov.application.service.seed.generator.UserSeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Coordinating service for generating test data.
 * Delegates actual generation to specialized generator components.
 * Only active in dev and test profiles for safety.
 */
@Service
@Profile({"dev", "test"})
public class DataSeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeedService.class);

    private final UserSeedGenerator userGenerator;
    private final DeckAndCardSeedGenerator deckAndCardGenerator;
    private final StatsSeedGenerator statsGenerator;
    private final NewsSeedGenerator newsGenerator;
    private final PasswordEncoder passwordEncoder;

    private String cachedPasswordHash;

    // Configuration
    private final int batchSizeUsers;
    private final int limitUsers;
    private final int limitDecksPerUser;
    private final int limitCardsPerDeck;
    private final int limitNews;
    private final String testUserPassword;

    /**
     * Constructs DataSeedService with required dependencies and configuration.
     *
     * @param userGeneratorValue generator for users
     * @param deckAndCardGeneratorValue generator for decks and cards
     * @param statsGeneratorValue generator for statistics
     * @param newsGeneratorValue generator for news
     * @param passwordEncoderValue encoder for password hashing
     * @param config seed configuration with batch sizes and generation limits
     */
    public DataSeedService(
            final UserSeedGenerator userGeneratorValue,
            final DeckAndCardSeedGenerator deckAndCardGeneratorValue,
            final StatsSeedGenerator statsGeneratorValue,
            final NewsSeedGenerator newsGeneratorValue,
            final PasswordEncoder passwordEncoderValue,
            final SeedConfig config) {
        this.userGenerator = userGeneratorValue;
        this.deckAndCardGenerator = deckAndCardGeneratorValue;
        this.statsGenerator = statsGeneratorValue;
        this.newsGenerator = newsGeneratorValue;
        this.passwordEncoder = passwordEncoderValue;

        // Batch sizes
        this.batchSizeUsers = config.test().batch().users();

        // Generation limits
        this.limitUsers = config.test().limits().users();
        this.limitDecksPerUser = config.test().limits().decksPerUser();
        this.limitCardsPerDeck = config.test().limits().cardsPerDeck();
        this.limitNews = config.test().limits().news();

        // Test user password
        this.testUserPassword = config.test().testUserPassword();

        LOGGER.info("DataSeedService initialized with batch size: users={}", batchSizeUsers);
        LOGGER.info(
                "Generation limits: {} users, {} decks/user, {} cards/deck, {} news",
                limitUsers,
                limitDecksPerUser,
                limitCardsPerDeck,
                limitNews);
    }

    /**
     * Initializes cached password hash after bean construction.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    void initPasswordCache() {
        LOGGER.debug("Caching password hash for test data generation...");
        this.cachedPasswordHash = passwordEncoder.encode(testUserPassword);
        LOGGER.debug("Password hash cached successfully");
    }

    /**
     * Generates complete test data set for load testing.
     * Coordinates generation across all entity types using specialized generators.
     */
    public void generateTestData() {
        LOGGER.info("=== Starting test data generation ===");
        long startTime = System.currentTimeMillis();

        // Step 1: Generate users
        List<User> users = userGenerator.generateUsers(limitUsers, batchSizeUsers, cachedPasswordHash);

        // Step 2: Generate decks and flashcards
        int[] deckAndCardCounts =
                deckAndCardGenerator.generateDecksAndCards(users, limitDecksPerUser, limitCardsPerDeck, batchSizeUsers);

        // Step 3: Generate statistics and news in parallel
        CompletableFuture<Integer> statsFuture =
                CompletableFuture.supplyAsync(() -> statsGenerator.generateStatistics(users));
        CompletableFuture<Integer> newsFuture =
                CompletableFuture.supplyAsync(() -> newsGenerator.generateNews(limitNews));

        // Wait for parallel operations
        int statsGenerated = statsFuture.join();
        int newsGenerated = newsFuture.join();

        logGenerationSummary(
                startTime, users.size(), deckAndCardCounts[0], deckAndCardCounts[1], statsGenerated, newsGenerated);
    }

    /**
     * Logs final generation summary.
     *
     * @param startTime generation start time
     * @param userCount number of users generated
     * @param deckCount number of decks generated
     * @param cardCount number of cards generated
     * @param statsCount number of statistics records generated
     * @param newsCount number of news articles generated
     */
    private void logGenerationSummary(
            final long startTime,
            final int userCount,
            final int deckCount,
            final int cardCount,
            final int statsCount,
            final int newsCount) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long seconds = duration / 1000;
        long minutes = seconds / 60;

        LOGGER.info("=== Test data generation completed in {} ms ({} min {} sec) ===", duration, minutes, seconds % 60);
        LOGGER.info(
                "Summary: {} users, {} decks, {} flashcards, {} statistics, {} news",
                userCount,
                deckCount,
                cardCount,
                statsCount,
                newsCount);
        LOGGER.info("Average speed: {} flashcards/sec", cardCount / Math.max(1, seconds));
    }
}
