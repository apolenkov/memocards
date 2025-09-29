package org.apolenkov.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple service for generating test data.
 */
@Service
public class DataSeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeedService.class);

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final StatsRepository statsRepository;
    private final NewsRepository newsRepository;
    private final PasswordEncoder passwordEncoder;
    private final java.security.SecureRandom random = new java.security.SecureRandom();

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
    private static final String DEFAULT_TEST_PASSWORD = "testPassword123";
    private static final String TEST_PASSWORD_PROPERTY = "seed.test.password";

    /**
     * Constructs DataSeedService with required dependencies.
     *
     * @param userRepositoryValue repository for user operations
     * @param deckRepositoryValue repository for deck operations
     * @param flashcardRepositoryValue repository for flashcard operations
     * @param statsRepositoryValue repository for statistics operations
     * @param newsRepositoryValue repository for news operations
     * @param passwordEncoderValue encoder for password hashing
     */
    public DataSeedService(
            final UserRepository userRepositoryValue,
            final DeckRepository deckRepositoryValue,
            final FlashcardRepository flashcardRepositoryValue,
            final StatsRepository statsRepositoryValue,
            final NewsRepository newsRepositoryValue,
            final PasswordEncoder passwordEncoderValue) {
        this.userRepository = userRepositoryValue;
        this.deckRepository = deckRepositoryValue;
        this.flashcardRepository = flashcardRepositoryValue;
        this.statsRepository = statsRepositoryValue;
        this.newsRepository = newsRepositoryValue;
        this.passwordEncoder = passwordEncoderValue;
    }

    /**
     * Generates test data for load testing.
     * Delegates to specialized methods to reduce cognitive complexity.
     */
    @Transactional
    public void generateTestData() {
        LOGGER.info("=== Starting test data generation ===");
        long startTime = System.currentTimeMillis();

        // Generate all test data using specialized methods
        List<User> users = generateTestUsers();
        int[] deckAndCardCounts = generateDecksAndCards(users);
        int statsGenerated = generateStatistics(users);
        generateNewsArticles();

        // Log completion summary
        logGenerationSummary(startTime, users.size(), deckAndCardCounts[0], deckAndCardCounts[1], statsGenerated);
    }

    /**
     * Generates test users for load testing.
     *
     * @return list of generated users
     */
    private List<User> generateTestUsers() {
        LOGGER.info("Generating 1000 test users...");
        List<User> users = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            logProgress(i, 20, "Generated {} users so far...", i);

            User user = createTestUser(i);
            users.add(userRepository.save(user));
        }

        LOGGER.info("Successfully generated {} users", users.size());
        return users;
    }

    /**
     * Creates a single test user with random data.
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
        user.setPasswordHash(passwordEncoder.encode(getTestPassword()));
        user.setRoles(Set.of("USER"));

        return user;
    }

    /**
     * Generates decks and flashcards for all users.
     *
     * @param users list of users to generate data for
     * @return array with [totalDecks, totalCards] counts
     */
    private int[] generateDecksAndCards(final List<User> users) {
        LOGGER.info("Generating decks and flashcards...");
        int totalDecks = 0;
        int totalCards = 0;
        int userCount = 0;

        for (User user : users) {
            userCount++;
            logProgress(userCount, 10, "Processing user {} of {}...", userCount, users.size());

            int[] userCounts = generateUserDecksAndCards(user);
            totalDecks += userCounts[0];
            totalCards += userCounts[1];
        }

        LOGGER.info("Successfully generated {} decks and {} flashcards", totalDecks, totalCards);
        return new int[] {totalDecks, totalCards};
    }

    /**
     * Generates decks and cards for a single user.
     *
     * @param user the user to generate data for
     * @return array with [decks, cards] counts for this user
     */
    private int[] generateUserDecksAndCards(final User user) {
        int userDecks = 0;
        int userCards = 0;

        for (int i = 0; i < 10; i++) {
            Deck deck = createTestDeck(user, i);
            deck = deckRepository.save(deck);
            userDecks++;

            int cardsInDeck = generateDeckCards(deck);
            userCards += cardsInDeck;
        }

        return new int[] {userDecks, userCards};
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
     * Generates flashcards for a deck.
     *
     * @param deck the deck to generate cards for
     * @return number of cards generated
     */
    private int generateDeckCards(final Deck deck) {
        int cardsGenerated = 0;

        for (int j = 0; j < 50; j++) {
            Flashcard card = createTestCard(deck, j);
            flashcardRepository.save(card);
            cardsGenerated++;
        }

        return cardsGenerated;
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

        for (int day = 0; day < 90; day++) {
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
     * @return true if stats should be generated (70% probability)
     */
    private boolean shouldGenerateStatsForDay() {
        return random.nextDouble() < 0.7;
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
                .build();
    }

    /**
     * Generates news articles for testing.
     */
    private void generateNewsArticles() {
        LOGGER.info("Generating news articles...");

        for (int i = 0; i < 50; i++) {
            News news = createTestNews(i);
            newsRepository.save(news);
        }

        LOGGER.info("Successfully generated 50 news articles");
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
     * Logs generation progress at specified intervals.
     *
     * @param current current progress
     * @param interval logging interval
     * @param message log message template
     * @param args message arguments
     */
    private void logProgress(final int current, final int interval, final String message, final Object... args) {
        if (current % interval == 0) {
            LOGGER.debug(message, args);
        }
    }

    /**
     * Logs final generation summary.
     *
     * @param startTime generation start time
     * @param userCount number of users generated
     * @param deckCount number of decks generated
     * @param cardCount number of cards generated
     * @param statsCount number of statistics records generated
     */
    private void logGenerationSummary(
            final long startTime, final int userCount, final int deckCount, final int cardCount, final int statsCount) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        LOGGER.info("=== Test data generation completed in {} ms ===", duration);
        LOGGER.info(
                "Summary: {} users, {} decks, {} flashcards, {} statistics, 50 news",
                userCount,
                deckCount,
                cardCount,
                statsCount);
    }

    /**
     * Gets test password from system property or uses default.
     * This method centralizes password handling to avoid hardcoded values.
     *
     * @return test password for generated users
     */
    private String getTestPassword() {
        return System.getProperty(TEST_PASSWORD_PROPERTY, DEFAULT_TEST_PASSWORD);
    }
}
