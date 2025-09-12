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
    private static final String TEST_PASSWORD = System.getProperty("seed.test.password", "testPassword123");

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
     */
    @Transactional
    @SuppressWarnings("java:S2068") // Test password for generated data
    public void generateTestData() {
        LOGGER.info("=== Starting test data generation ===");
        long startTime = System.currentTimeMillis();

        // Generate users
        LOGGER.info("Generating 1000 test users...");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            if (i % 20 == 0) {
                LOGGER.debug("Generated {} users so far...", i);
            }
            String firstName = USER_NAMES[random.nextInt(USER_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "." + i + "@example.com";
            String name = firstName + " " + lastName;

            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
            user.setRoles(Set.of("USER"));

            users.add(userRepository.save(user));
        }
        LOGGER.info("Successfully generated {} users", users.size());

        // Generate decks and cards
        LOGGER.info("Generating decks and flashcards...");
        int totalDecks = 0;
        int totalCards = 0;
        int userCount = 0;
        for (User user : users) {
            userCount++;
            if (userCount % 10 == 0) {
                LOGGER.debug("Processing user {} of {}...", userCount, users.size());
            }
            for (int i = 0; i < 10; i++) {
                String title = DECK_TITLES[random.nextInt(DECK_TITLES.length)] + " " + (i + 1);
                Deck deck = new Deck(null, user.getId(), title, "Test deck for load testing");
                deck = deckRepository.save(deck);
                totalDecks++;

                // Generate cards
                for (int j = 0; j < 50; j++) {
                    String front = CARD_FRONTS[random.nextInt(CARD_FRONTS.length)] + " " + (j + 1);
                    String back = CARD_BACKS[random.nextInt(CARD_BACKS.length)];
                    Flashcard card = new Flashcard(null, deck.getId(), front, back, "Example: " + front);
                    flashcardRepository.save(card);
                    totalCards++;
                }
            }
        }
        LOGGER.info("Successfully generated {} decks and {} flashcards", totalDecks, totalCards);

        // Generate some statistics
        LOGGER.info("Generating statistics data...");
        List<Deck> allDecks = new ArrayList<>();
        for (User user : users) {
            allDecks.addAll(deckRepository.findByUserId(user.getId()));
        }

        int statsGenerated = 0;
        for (Deck deck : allDecks) {
            for (int day = 0; day < 90; day++) {
                if (random.nextDouble() < 0.7) {
                    LocalDate date = LocalDate.now().minusDays(day);
                    int viewed = random.nextInt(20) + 10;
                    int correct = (int) (viewed * 0.8);
                    int hard = random.nextInt(3);
                    long duration = viewed * 30000L;
                    long delay = viewed * 3000L;

                    SessionStatsDto stats = SessionStatsDto.builder()
                            .deckId(deck.getId())
                            .viewed(viewed)
                            .correct(correct)
                            .hard(hard)
                            .sessionDurationMs(duration)
                            .totalAnswerDelayMs(delay)
                            .build();

                    statsRepository.appendSession(stats, date);
                    statsGenerated++;
                }
            }
        }
        LOGGER.info("Successfully generated {} statistics records", statsGenerated);

        // Generate news
        LOGGER.info("Generating news articles...");
        for (int i = 0; i < 50; i++) {
            News news = new News(
                    null,
                    "Test News " + (i + 1),
                    "Test content for news item " + (i + 1),
                    "admin",
                    LocalDateTime.now());
            newsRepository.save(news);
        }
        LOGGER.info("Successfully generated 50 news articles");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOGGER.info("=== Test data generation completed in {} ms ===", duration);
        LOGGER.info(
                "Summary: {} users, {} decks, {} flashcards, {} statistics, 50 news",
                users.size(),
                totalDecks,
                totalCards,
                statsGenerated);
    }
}
