package org.apolenkov.application.service.seed;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.apolenkov.application.config.seed.SeedConfig;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Service for generating test data using batch operations and virtual threads.
 * Only active in dev and test profiles for safety.
 */
@Service
@Profile({"dev", "test"})
public class DataSeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeedService.class);

    private final DeckRepository deckRepository;
    private final StatsRepository statsRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final DataSeedRepository seedRepository;
    private final SecureRandom random = new SecureRandom();

    private String cachedPasswordHash;

    // Configurable batch sizes via environment variables
    private final int batchSizeUsers;

    // Configurable generation limits via environment variables
    private final int limitUsers;
    private final int limitDecksPerUser;
    private final int limitCardsPerDeck;
    private final int limitNews;

    // Test user password from configuration
    private final String testUserPassword;

    private static final String[] USER_NAMES = {"Alex", "Maria", "John", "Anna", "David", "Elena", "Michael", "Sophia"};
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"
    };
    private static final String[] DECK_TITLES = {
        "English Vocabulary", "Programming Terms", "Math Basics", "History Facts"
    };
    private static final String[] CARD_FRONTS = {"Algorithm", "Database", "API", "Framework", "Bug", "Testing"};
    private static final String[] CARD_BACKS = {
        "Step-by-step procedure",
        "Data storage",
        "Application interface",
        "Development platform",
        "Program error",
        "Quality assurance"
    };
    private static final String[] CARD_EXAMPLES = {
        "Binary search, Quick sort, Dijkstra's algorithm",
        "MySQL, PostgreSQL, MongoDB collections",
        "REST endpoints, GraphQL queries, HTTP requests",
        "Spring Boot, React, Angular, Vue.js",
        "NullPointerException, IndexOutOfBounds, StackOverflow",
        "Unit tests, Integration tests, E2E testing"
    };

    // Batch processing configuration
    private static final int STATS_DAYS_TO_GENERATE = 90;
    private static final double STATS_PROBABILITY = 0.7;

    /**
     * Constructs DataSeedService with required dependencies and configuration.
     *
     * @param deckRepositoryValue repository for deck read operations (findByUserId)
     * @param statsRepositoryValue repository for statistics operations
     * @param seedRepositoryValue repository for batch seed operations
     * @param passwordEncoderValue encoder for password hashing
     * @param transactionManager transaction manager for manual TX control
     * @param config seed configuration with batch sizes and generation limits
     */
    public DataSeedService(
            final DeckRepository deckRepositoryValue,
            final StatsRepository statsRepositoryValue,
            final DataSeedRepository seedRepositoryValue,
            final PasswordEncoder passwordEncoderValue,
            final PlatformTransactionManager transactionManager,
            final SeedConfig config) {
        this.deckRepository = deckRepositoryValue;
        this.statsRepository = statsRepositoryValue;
        this.seedRepository = seedRepositoryValue;
        this.passwordEncoder = passwordEncoderValue;
        this.transactionTemplate = new TransactionTemplate(transactionManager);

        // Batch size
        this.batchSizeUsers = config.test().batch().users();

        // Generation limits
        this.limitUsers = config.test().limits().users();
        this.limitDecksPerUser = config.test().limits().decksPerUser();
        this.limitCardsPerDeck = config.test().limits().cardsPerDeck();
        this.limitNews = config.test().limits().news();

        // Test user password
        this.testUserPassword = config.test().testUserPassword();

        LOGGER.info(
                "DataSeedService initialized with batch size: users={}",
                config.test().batch().users());
        LOGGER.info(
                "Generation limits: {} users, {} decks/user, {} cards/deck, {} news",
                config.test().limits().users(),
                config.test().limits().decksPerUser(),
                config.test().limits().cardsPerDeck(),
                config.test().limits().news());
    }

    /**
     * Initializes cached password hash after bean construction.
     * Avoids expensive password encoding in loops.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    void initPasswordCache() {
        LOGGER.debug("Caching password hash for test data generation...");
        this.cachedPasswordHash = passwordEncoder.encode(testUserPassword);
        LOGGER.debug("Password hash cached successfully");
    }

    /**
     * Generates test data for load testing using batch operations and virtual threads.
     * Use LIMIT_* environment variables to control data volume.
     */
    public void generateTestData() {
        LOGGER.info("=== Starting OPTIMIZED test data generation ===");
        long startTime = System.currentTimeMillis();

        // Step 1: Generate users in batches
        List<User> users = generateTestUsersBatch();

        // Step 2: Generate decks and flashcards using Virtual Threads for parallelization
        int[] deckAndCardCounts = generateDecksAndCardsParallel(users);

        // Step 3: Generate statistics and news in parallel (non-blocking)
        CompletableFuture<Integer> statsFuture = CompletableFuture.supplyAsync(() -> generateStatistics(users));
        CompletableFuture<Integer> newsFuture = CompletableFuture.supplyAsync(this::generateNewsArticlesBatch);

        // Wait for parallel operations to complete
        int statsGenerated = statsFuture.join();
        int newsGenerated = newsFuture.join();

        // Log completion summary
        logGenerationSummary(
                startTime, users.size(), deckAndCardCounts[0], deckAndCardCounts[1], statsGenerated, newsGenerated);
    }

    /**
     * Generates test users in batches for better performance.
     * Uses configured LIMIT_USERS for total count.
     *
     * @return list of generated users
     */
    private List<User> generateTestUsersBatch() {
        LOGGER.info("Generating {} test users in batches of {}...", limitUsers, batchSizeUsers);
        List<User> allUsers = new ArrayList<>();

        for (int i = 0; i < limitUsers; i += batchSizeUsers) {
            int end = Math.min(i + batchSizeUsers, limitUsers);
            int currentBatch = i;

            // Create batch in separate transaction to reduce lock time
            List<User> batch = transactionTemplate.execute(status -> {
                List<User> users = new ArrayList<>(batchSizeUsers);
                for (int j = currentBatch; j < end; j++) {
                    users.add(createTestUser(j));
                }
                return seedRepository.batchInsertUsers(users);
            });

            assert batch != null;
            allUsers.addAll(batch);

            if ((i / batchSizeUsers) % 5 == 0) {
                LOGGER.info("Generated {}/{} users", allUsers.size(), limitUsers);
            }
        }

        LOGGER.info("Successfully generated {} users", allUsers.size());
        return allUsers;
    }

    /**
     * Generates decks and flashcards using Virtual Threads for parallel processing.
     * Uses configured LIMIT_DECKS and LIMIT_CARDS for quantities.
     *
     * @param users list of users to generate data for
     * @return array with [totalDecks, totalCards] counts
     */
    @SuppressWarnings("java:S2139") // Security audit requires logging before rethrow (OWASP compliance)
    private int[] generateDecksAndCardsParallel(final List<User> users) {
        LOGGER.info("Generating decks and flashcards using Virtual Threads...");

        int totalDecks = 0;
        int totalCards = 0;

        LOGGER.info(
                "Using generation limits: {} decks per user, {} cards per deck", limitDecksPerUser, limitCardsPerDeck);

        // Process users in chunks with Virtual Threads
        int chunkSize = batchSizeUsers;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<java.util.concurrent.Future<int[]>> futures = new ArrayList<>();

            for (int i = 0; i < users.size(); i += chunkSize) {
                int endIdx = Math.min(i + chunkSize, users.size());
                List<User> userChunk = users.subList(i, endIdx);

                futures.add(executor.submit(() -> processUserChunk(userChunk, limitDecksPerUser, limitCardsPerDeck)));
            }

            // Collect results
            for (var future : futures) {
                int[] counts = future.get();
                totalDecks += counts[0];
                totalCards += counts[1];
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Thread interrupted during parallel processing", e);
            throw new DataGenerationException("Failed to generate decks and cards - interrupted", e);
        } catch (Exception e) {
            LOGGER.error("Error in parallel processing", e);
            throw new DataGenerationException("Failed to generate decks and cards", e);
        }

        LOGGER.info("Successfully generated {} decks and {} flashcards", totalDecks, totalCards);
        return new int[] {totalDecks, totalCards};
    }

    /**
     * Processes a chunk of users to generate their decks and flashcards.
     *
     * @param userChunk chunk of users to process
     * @param deckCount number of decks per user
     * @param cardCount number of cards per deck
     * @return array with [decks, cards] counts
     */
    private int[] processUserChunk(final List<User> userChunk, final int deckCount, final int cardCount) {
        return transactionTemplate.execute(status -> {
            int decks = 0;
            int cards = 0;

            for (User user : userChunk) {
                // Generate decks for this user in batch
                List<Deck> userDecks = new ArrayList<>(deckCount);
                for (int i = 0; i < deckCount; i++) {
                    userDecks.add(createTestDeck(user, i));
                }

                List<Deck> savedDecks = seedRepository.batchInsertDecks(userDecks);
                decks += savedDecks.size();

                // Generate flashcards for all decks in batch
                List<Flashcard> allCards = new ArrayList<>(deckCount * cardCount);
                for (Deck deck : savedDecks) {
                    for (int j = 0; j < cardCount; j++) {
                        allCards.add(createTestCard(deck, j));
                    }
                }

                seedRepository.batchInsertFlashcards(allCards);
                cards += allCards.size();
            }

            return new int[] {decks, cards};
        });
    }

    /**
     * Creates a single test user with random data.
     * Uses cached password hash for performance.
     *
     * @param index user index for unique email generation
     * @return configured test user
     */
    private User createTestUser(final int index) {
        String firstName = USER_NAMES[random.nextInt(USER_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + index + "@example.com";
        String name = firstName + " " + lastName;

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(cachedPasswordHash); // Use cached hash instead of encoding each time
        user.setRoles(Set.of("USER"));

        return user;
    }

    /**
     * Creates a test deck for a user.
     *
     * @param user the deck owner
     * @param index deck index for unique title
     * @return configured test deck
     */
    private Deck createTestDeck(final User user, final int index) {
        String title = DECK_TITLES[random.nextInt(DECK_TITLES.length)] + " " + (index + 1);
        return new Deck(null, user.getId(), title, "Test deck for load testing");
    }

    /**
     * Creates a test flashcard.
     *
     * @param deck the parent deck
     * @param index card index for unique content
     * @return configured test flashcard
     */
    private Flashcard createTestCard(final Deck deck, final int index) {
        int cardIndex = random.nextInt(CARD_FRONTS.length);
        String front = CARD_FRONTS[cardIndex] + " " + (index + 1);
        String back = CARD_BACKS[cardIndex];
        String example = CARD_EXAMPLES[cardIndex];
        return new Flashcard(null, deck.getId(), front, back, example);
    }

    /**
     * Generates statistics data for all user decks.
     *
     * @param users list of users to generate stats for
     * @return number of statistics records generated
     */
    private int generateStatistics(final List<User> users) {
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

    /**
     * Generates news articles in batch for testing.
     * Uses configured LIMIT_NEWS for count.
     *
     * @return number of news articles generated
     */
    private int generateNewsArticlesBatch() {
        LOGGER.info("Generating {} news articles in batch...", limitNews);

        List<News> newsList = new ArrayList<>(limitNews);
        for (int i = 0; i < limitNews; i++) {
            newsList.add(createTestNews(i));
        }

        transactionTemplate.execute(status -> {
            seedRepository.batchInsertNews(newsList);
            return null;
        });

        LOGGER.info("Successfully generated {} news articles", limitNews);
        return limitNews;
    }

    /**
     * Creates a test news article.
     *
     * @param index news index for unique content
     * @return configured test news
     */
    private News createTestNews(final int index) {
        return new News(
                null,
                "Test News " + (index + 1),
                "Test content for news item " + (index + 1),
                "admin",
                LocalDateTime.now());
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
