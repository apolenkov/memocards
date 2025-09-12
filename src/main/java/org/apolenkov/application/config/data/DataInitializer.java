package org.apolenkov.application.config.data;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.DataSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes demo data in development environment.
 * Creates sample decks, flashcards, and news items for development and testing.
 * Only runs in "dev" profile and creates data only if the system is empty.
 */
@Configuration
@Profile({"dev"})
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * Logic detales.
     */
    private static final String ADMIN_EMAIL = "admin@example.com";

    private static final String ADMIN_PASSWORD = "admin";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "user";
    private static final String USER_NAME = "Demo User";
    private static final String ADMIN_NAME = "Administrator";

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
    CommandLineRunner ensureDomainUsers(final UserRepository users, final PasswordEncoder passwordEncoder) {
        return args -> {
            LOGGER.info("=== Ensuring domain users exist ===");

            syncUser(users, passwordEncoder, USER_EMAIL, USER_PASSWORD, USER_NAME, Set.of(SecurityConstants.ROLE_USER));

            syncUser(
                    users,
                    passwordEncoder,
                    ADMIN_EMAIL,
                    ADMIN_PASSWORD,
                    ADMIN_NAME,
                    Set.of(SecurityConstants.ROLE_ADMIN));

            LOGGER.info("✅ Domain users ensured");
        };
    }

    /**
     * Creates demo data initializer that runs after application startup.
     * Creates three themed decks (Travel, IT, English) with relevant flashcards
     * and welcome news items. Also generates test data for load testing.
     * Only initializes if no existing data is found.
     *
     * @param users user repository for finding demo user
     * @param decks deck repository for creating decks
     * @param cards flashcard repository for creating flashcards
     * @param news news repository for creating news items
     * @param dataSeedService service for generating test data
     * @return CommandLineRunner that initializes demo data
     */
    @Bean
    @Order(20)
    CommandLineRunner initDemoData(
            final UserRepository users,
            final DeckRepository decks,
            final FlashcardRepository cards,
            final NewsRepository news,
            final DataSeedService dataSeedService) {
        return args -> {
            LOGGER.info("=== Initializing development data ===");

            final User user = findDemoUser(users);
            if (user == null) {
                LOGGER.warn("User not found, skipping demo data creation");

                return;
            }

            createDemoDataIfNeeded(user, decks, cards, news);
            generateTestDataIfEnabled(dataSeedService);

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
        final java.util.Optional<User> opt = users.findByEmail(USER_EMAIL);
        if (opt.isEmpty()) {
            LOGGER.info("ℹ️ Demo user not found, skipping demo data creation");
            return null;
        }
        return opt.get();
    }

    /**
     * Creates demo data (decks, flashcards, news) if not already present.
     *
     * @param user demo user
     * @param decks deck repository
     * @param cards flashcard repository
     * @param news news repository
     */
    private void createDemoDataIfNeeded(
            final User user, final DeckRepository decks, final FlashcardRepository cards, final NewsRepository news) {
        if (!decks.findByUserId(user.getId()).isEmpty()) {
            LOGGER.info("ℹ️ Demo data already exists, skipping demo data creation");
            return;
        }

        LOGGER.info("🌱 Creating demo data...");
        createDemoDecks(user, decks, cards);
        createWelcomeNews(news);
        LOGGER.info("✅ Demo data creation completed");
    }

    /**
     * Creates demo decks with flashcards.
     *
     * @param user demo user
     * @param decks deck repository
     * @param cards flashcard repository
     */
    private void createDemoDecks(final User user, final DeckRepository decks, final FlashcardRepository cards) {
        final Deck travel = decks.save(new Deck(null, user.getId(), "Travel - phrases", "Short phrases for trips"));
        final Deck it = decks.save(new Deck(null, user.getId(), "IT - terms", "Core programming terms"));
        final Deck english = decks.save(new Deck(null, user.getId(), "English Basics", "Basic English words"));

        createTravelCards(travel, cards);
        createItCards(it, cards);
        createEnglishCards(english, cards);
    }

    /**
     * Creates travel-themed flashcards.
     *
     * @param travel travel deck
     * @param cards flashcard repository
     */
    private void createTravelCards(final Deck travel, final FlashcardRepository cards) {
        final List<Flashcard> travelCards = List.of(
                new Flashcard(null, travel.getId(), "Hello", "Hello", "Hello, how are you?"),
                new Flashcard(null, travel.getId(), "Thank you", "Thank you", "Thank you very much!"),
                new Flashcard(null, travel.getId(), "Excuse me", "Excuse me", "Excuse me, where is the station?"),
                new Flashcard(null, travel.getId(), "How much?", "How much is this?", "How much does this cost?"),
                new Flashcard(null, travel.getId(), "Where is...?", "Where is it?", "Where is the nearest bank?"));
        travelCards.forEach(cards::save);
    }

    /**
     * Creates IT-themed flashcards.
     *
     * @param it IT deck
     * @param cards flashcard repository
     */
    private void createItCards(final Deck it, final FlashcardRepository cards) {
        final List<Flashcard> itCards = List.of(
                new Flashcard(
                        null, it.getId(), "Algorithm", "Algorithm", "A step-by-step procedure for solving a problem"),
                new Flashcard(null, it.getId(), "Database", "Database", "Organized collection of data"),
                new Flashcard(null, it.getId(), "API", "API", "Application Programming Interface"),
                new Flashcard(
                        null, it.getId(), "Framework", "Framework", "A platform for developing software applications"),
                new Flashcard(null, it.getId(), "Bug", "Bug", "An error in a computer program"),
                new Flashcard(
                        null,
                        it.getId(),
                        "Version Control",
                        "Version Control",
                        "Managing changes to documents and code"));
        itCards.forEach(cards::save);
    }

    /**
     * Creates English-themed flashcards.
     *
     * @param english English deck
     * @param cards flashcard repository
     */
    private void createEnglishCards(final Deck english, final FlashcardRepository cards) {
        final List<Flashcard> englishCards = List.of(
                new Flashcard(null, english.getId(), "Apple", "A fruit", "I eat an apple every day"),
                new Flashcard(null, english.getId(), "Beautiful", "Attractive", "She has beautiful eyes"),
                new Flashcard(
                        null, english.getId(), "Computer", "A machine for processing data", "I work on my computer"),
                new Flashcard(null, english.getId(), "Dog", "An animal", "My dog is very friendly"),
                new Flashcard(
                        null, english.getId(), "Education", "The process of learning", "Education is very important"));
        englishCards.forEach(cards::save);
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
                "Welcome to Flashcards!",
                "Our app helps you efficiently learn new words and phrases. "
                        + "Create decks, practice, and track your progress.",
                ADMIN_EMAIL,
                java.time.LocalDateTime.now()));

        news.save(new News(
                null,
                "New features in the application",
                "We have added statistics tracking, practice settings, and much more. " + "Stay tuned for updates!",
                ADMIN_EMAIL,
                java.time.LocalDateTime.now()));
    }

    /**
     * Generates test data for load testing if enabled.
     *
     * @param dataSeedService service for generating test data
     */
    private void generateTestDataIfEnabled(final DataSeedService dataSeedService) {
        final String generateTestData =
                System.getProperty("generate.test.data", System.getenv().getOrDefault("GENERATE_TEST_DATA", "false"));

        if (!"true".equalsIgnoreCase(generateTestData)) {
            LOGGER.info("ℹ️ Test data generation skipped (set -D generate.test.data=true to enable)");
            return;
        }

        LOGGER.info("🌱 Generating test data for load testing...");
        try {
            dataSeedService.generateTestData();
            LOGGER.info("✅ Test data generation completed successfully!");
        } catch (Exception e) {
            LOGGER.error("❌ Failed to generate test data: {}", e.getMessage(), e);
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
        Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            // Create new user with specified properties
            User created = new User(null, email, fullName);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            users.save(created);
            return;
        }

        User existing = existingOpt.get();
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        existing.setName(fullName);
        users.save(existing);
    }
}
