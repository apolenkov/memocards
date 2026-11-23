package org.apolenkov.application.config.seed;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.seed.DataSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes demo data in environment.
 * Creates sample decks, cards, and news items for development and testing.
 */
@Configuration
@Profile({"dev", "test", "prod"})
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    // ==================== Fields ====================

    private final SeedConfig seedConfig;

    // ==================== Constructor ====================

    /**
     * Creates DataInitializer with seed configuration.
     *
     * @param seedConfigValue type-safe seed configuration
     */
    public DataInitializer(final SeedConfig seedConfigValue) {
        this.seedConfig = seedConfigValue;
    }

    // ==================== Constants ====================

    /**
     * Demo user configuration constants.
     * These are development-only credentials for demo purposes.
     */
    private static final String ADMIN_EMAIL = "admin@example.com";

    private static final String ADMIN_NAME = "Administrator";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_NAME = "Demo User";

    /**
     * Creates essential domain users (user and admin) if they don't exist.
     * Runs before demo data initialization to ensure users exist.
     *
     * @param users user repository for user operations
     * @param passwordEncoder password encoder for secure password hashing
     * @return CommandLineRunner that ensures domain users exist
     */
    @Bean
    @Order(10)
    public CommandLineRunner ensureDomainUsers(final UserRepository users, final PasswordEncoder passwordEncoder) {
        return args -> {
            LOGGER.info("=== Ensuring domain users exist ===");

            syncUser(
                    users,
                    passwordEncoder,
                    USER_EMAIL,
                    getDemoPassword(false),
                    USER_NAME,
                    Set.of(SecurityConstants.ROLE_USER));

            syncUser(
                    users,
                    passwordEncoder,
                    ADMIN_EMAIL,
                    getDemoPassword(true),
                    ADMIN_NAME,
                    Set.of(SecurityConstants.ROLE_ADMIN));

            LOGGER.info("Domain users ensured");
        };
    }

    /**
     * Creates demo data initializer that runs after application startup.
     * Creates three themed decks (Travel, IT, English) with relevant cards
     * and welcome news items. Also generates test data for load testing.
     * Only initializes if no existing data is found.
     *
     * @param users user repository for finding demo user
     * @param decks deck repository for creating decks
     * @param cards card repository for creating cards
     * @param news news repository for creating news items
     * @param dataSeedServiceProvider provider for optional DataSeedService (available only in dev/test profiles)
     * @return CommandLineRunner that initializes demo data
     */
    @Bean
    @Order(20)
    public CommandLineRunner initDemoData(
            final UserRepository users,
            final DeckRepository decks,
            final CardRepository cards,
            final NewsRepository news,
            final ObjectProvider<DataSeedService> dataSeedServiceProvider) {
        return args -> {
            LOGGER.info("=== Initializing development data ===");

            final User user = findDemoUser(users);
            if (user == null) {
                LOGGER.warn("User not found, skipping demo data creation");

                return;
            }

            createDemoDataIfNeeded(user, decks, cards, news);
            dataSeedServiceProvider.ifAvailable(this::generateTestDataIfEnabled);

            LOGGER.info("=== Development data initialization completed ===");
        };
    }

    /**
     * Finds the demo user or logs warning if not found.
     *
     * @param users user repository
     * @return demo user or null if not found
     */
    private User findDemoUser(final UserRepository users) {
        final Optional<User> opt = users.findByEmail(USER_EMAIL);
        if (opt.isEmpty()) {
            LOGGER.info("Demo user not found, skipping demo data creation");
            return null;
        }
        return opt.get();
    }

    /**
     * Creates demo data (decks, cards, news) if not already present.
     *
     * @param user demo user
     * @param decks deck repository
     * @param cards card repository
     * @param news news repository
     */
    private void createDemoDataIfNeeded(
            final User user, final DeckRepository decks, final CardRepository cards, final NewsRepository news) {
        if (!seedConfig.demo().enabled()) {
            LOGGER.info("Demo data creation disabled (app.seed.demo.enabled=false)");
            return;
        }

        if (!decks.findByUserId(user.getId()).isEmpty()) {
            LOGGER.info("Demo data already exists, skipping demo data creation");
            return;
        }

        LOGGER.info("Creating demo data...");
        createDemoDecks(user, decks, cards);
        createWelcomeNews(news);
        LOGGER.info("Demo data creation completed");
    }

    /**
     * Creates demo decks with cards.
     *
     * @param user demo user
     * @param decks deck repository
     * @param cards card repository
     */
    private void createDemoDecks(final User user, final DeckRepository decks, final CardRepository cards) {
        final Deck travel = decks.save(new Deck(null, user.getId(), "Travel - phrases", "Short phrases for trips"));
        final Deck it = decks.save(new Deck(null, user.getId(), "IT - terms", "Core programming terms"));
        final Deck english = decks.save(new Deck(null, user.getId(), "English Basics", "Basic English words"));
        final Deck performance = decks.save(new Deck(
                null,
                user.getId(),
                "🚀 Performance Test (600 cards)",
                "Large deck for testing lazy loading and pagination performance"));

        createTravelCards(travel, cards);
        createItCards(it, cards);
        createEnglishCards(english, cards);
        createPerformanceTestCards(performance, cards);
    }

    /**
     * Creates travel-themed cards.
     *
     * @param travel travel deck
     * @param cards card repository
     */
    private void createTravelCards(final Deck travel, final CardRepository cards) {
        final List<Card> travelCards = List.of(
                new Card(null, travel.getId(), "Hello", "Привет", "Hello, how are you?"),
                new Card(null, travel.getId(), "Thank you", "Спасибо", "Thank you very much!"),
                new Card(null, travel.getId(), "Excuse me", "Извините", "Excuse me, where is the station?"),
                new Card(null, travel.getId(), "How much?", "Сколько стоит?", "How much does this cost?"),
                new Card(null, travel.getId(), "Where is...?", "Где находится...?", "Where is the nearest bank?"));
        travelCards.forEach(cards::save);
    }

    /**
     * Creates IT-themed cards.
     *
     * @param it IT deck
     * @param cards card repository
     */
    private void createItCards(final Deck it, final CardRepository cards) {
        final List<Card> itCards = List.of(
                new Card(null, it.getId(), "Algorithm", "Алгоритм", "A step-by-step procedure for solving a problem"),
                new Card(null, it.getId(), "Database", "База данных", "Organized collection of data"),
                new Card(null, it.getId(), "API", "Интерфейс программирования", "Application Programming Interface"),
                new Card(null, it.getId(), "Framework", "Фреймворк", "A platform for developing software applications"),
                new Card(null, it.getId(), "Bug", "Ошибка в программе", "An error in a computer program"),
                new Card(
                        null,
                        it.getId(),
                        "Version Control",
                        "Система контроля версий",
                        "Managing changes to documents and code"));
        itCards.forEach(cards::save);
    }

    /**
     * Creates English-themed cards.
     *
     * @param english English deck
     * @param cards card repository
     */
    private void createEnglishCards(final Deck english, final CardRepository cards) {
        final List<Card> englishCards = List.of(
                new Card(null, english.getId(), "Apple", "Яблоко", "I eat an apple every day"),
                new Card(null, english.getId(), "Beautiful", "Красивый", "She has beautiful eyes"),
                new Card(null, english.getId(), "Computer", "Компьютер", "I work on my computer"),
                new Card(null, english.getId(), "Dog", "Собака", "My dog is very friendly"),
                new Card(null, english.getId(), "Education", "Образование", "Education is very important"));
        englishCards.forEach(cards::save);
    }

    /**
     * Creates 600 cards for performance testing of lazy loading and pagination.
     * Tests virtual scrolling, search, and filter performance with large datasets.
     *
     * @param performance performance test deck
     * @param cards card repository
     */
    private void createPerformanceTestCards(final Deck performance, final CardRepository cards) {
        LOGGER.info("Generating 600 cards for performance testing...");

        final String[] topics = {"Animals", "Food", "Travel", "Technology", "Science", "Art", "Sports", "Music"};
        final String[] examples = {
            "This is a common word used in everyday conversation",
            "You will hear this frequently in professional settings",
            "Essential vocabulary for beginners",
            "Advanced level terminology",
            "Colloquial expression often used informally"
        };

        for (int i = 1; i <= 600; i++) {
            final String topic = topics[i % topics.length];
            final String example = examples[i % examples.length];

            final Card card = new Card(
                    null,
                    performance.getId(),
                    String.format("%s #%d - Word", topic, i),
                    String.format("%s #%d - Перевод", topic, i),
                    String.format("%s. Example %d: %s", example, i, "Sample sentence using this word"));
            cards.save(card);

            // Log progress every 100 cards
            if (i % 100 == 0) {
                LOGGER.info("Generated {}/600 cards...", i);
            }
        }

        LOGGER.info("Successfully generated 600 cards for performance testing");
    }

    /**
     * Creates welcome news items if none exist.
     *
     * @param news news repository
     */
    private void createWelcomeNews(final NewsRepository news) {
        if (!news.findAllOrderByCreatedDesc().isEmpty()) {
            return;
        }

        news.save(new News(
                null,
                "Welcome to Cards!",
                "Our app helps you efficiently learn new words and phrases. "
                        + "Create decks, practice, and track your progress.",
                ADMIN_EMAIL,
                LocalDateTime.now(ZoneId.systemDefault())));

        news.save(new News(
                null,
                "New features in the application",
                "We have added statistics tracking, practice settings, and much more. " + "Stay tuned for updates!",
                ADMIN_EMAIL,
                LocalDateTime.now(ZoneId.systemDefault())));
    }

    /**
     * Generates test data for load testing if enabled.
     *
     * @param dataSeedService service for generating test data
     */
    private void generateTestDataIfEnabled(final DataSeedService dataSeedService) {
        if (!seedConfig.test().enabled()) {
            LOGGER.info("Test data generation skipped (set app.seed.test.enabled=true to enable)");
            return;
        }

        LOGGER.info("Generating test data for load testing...");
        try {
            dataSeedService.generateTestData();
            LOGGER.info("Test data generation completed successfully");
        } catch (RuntimeException e) {
            LOGGER.error("Failed to generate test data: {}", e.getMessage(), e);
        }
    }

    /**
     * Synchronizes user with desired configuration.
     *
     * @param users user repository for user operations
     * @param passwordEncoder password encoder for secure password hashing
     * @param email email address for the user
     * @param rawPassword plain text password to hash and store
     * @param fullName display name for the user
     * @param desiredRoles set of roles the user should have
     */
    private void syncUser(
            final UserRepository users,
            final PasswordEncoder passwordEncoder,
            final String email,
            final String rawPassword,
            final String fullName,
            final Set<String> desiredRoles) {
        final Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            // Create new user with specified properties
            final User created = new User(null, email, fullName);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            users.save(created);
            return;
        }

        final User existing = existingOpt.get();
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        existing.setName(fullName);
        users.save(existing);
    }

    /**
     * Gets demo password from Spring configuration.
     * This method centralizes password handling to avoid hardcoded values.
     *
     * @param isAdmin whether to get admin or user password
     * @return demo password for development environment
     */
    private String getDemoPassword(final boolean isAdmin) {
        return isAdmin ? seedConfig.demo().adminPassword() : seedConfig.demo().userPassword();
    }
}
