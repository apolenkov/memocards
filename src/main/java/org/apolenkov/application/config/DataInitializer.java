package org.apolenkov.application.config;

import java.util.List;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.News;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for initializing demo data in development environment.
 *
 * <p>This class provides demo data initialization for the development profile,
 * creating sample decks, flashcards, and news items to help developers and
 * testers work with the application. The data includes realistic examples
 * across different categories like travel, IT, and English basics.</p>
 *
 * <p>The initializer only runs in the "dev" profile and creates data only
 * if the system is empty, preventing duplicate data creation on subsequent
 * application starts.</p>
 *
 */
@Configuration
@org.springframework.context.annotation.Profile({"dev"})
public class DataInitializer {

    /**
     * Creates a CommandLineRunner bean for initializing demo data.
     *
     * <p>This bean runs after the application context is fully initialized
     * and creates sample data including decks, flashcards, and news items.
     * The initializer checks for existing data and only creates new data
     * if the system is empty.</p>
     *
     * <p>The demo data includes three themed decks (Travel, IT, and English)
     * with relevant flashcards, plus welcome news items. This provides
     * developers with realistic data to work with during development.</p>
     *
     * @param users the user repository for finding existing users
     * @param decks the deck repository for creating and saving decks
     * @param cards the flashcard repository for creating and saving flashcards
     * @param news the news repository for creating and saving news items
     * @return a CommandLineRunner that initializes the demo data
     */
    @Bean
    @org.springframework.core.annotation.Order(20)
    CommandLineRunner initDemoData(
            UserRepository users, DeckRepository decks, FlashcardRepository cards, NewsRepository news) {
        return args -> {
            // Find the demo user (must exist for demo data creation)
            java.util.Optional<User> opt = users.findByEmail("user@example.com");
            if (opt.isEmpty()) {
                return; // Skip if demo user doesn't exist
            }
            User user = opt.get();

            // Skip if user already has decks (avoid duplicate data)
            if (!decks.findByUserId(user.getId()).isEmpty()) return;

            // Create three themed decks for demonstration
            Deck travel = decks.save(new Deck(null, user.getId(), "Travel - phrases", "Short phrases for trips"));
            Deck it = decks.save(new Deck(null, user.getId(), "IT - terms", "Core programming terms"));
            Deck english = decks.save(new Deck(null, user.getId(), "English Basics", "Basic English words"));

            List<Flashcard> travelCards = List.of(
                    new Flashcard(null, travel.getId(), "Hello", "Hello", "Hello, how are you?"),
                    new Flashcard(null, travel.getId(), "Thank you", "Thank you", "Thank you very much!"),
                    new Flashcard(null, travel.getId(), "Excuse me", "Excuse me", "Excuse me, where is the station?"),
                    new Flashcard(null, travel.getId(), "How much?", "How much is this?", "How much does this cost?"),
                    new Flashcard(null, travel.getId(), "Where is...?", "Where is it?", "Where is the nearest bank?"));

            List<Flashcard> itCards = List.of(
                    new Flashcard(
                            null,
                            it.getId(),
                            "Algorithm",
                            "Algorithm",
                            "A step-by-step procedure for solving a problem"),
                    new Flashcard(null, it.getId(), "Database", "Database", "Organized collection of data"),
                    new Flashcard(null, it.getId(), "API", "API", "Application Programming Interface"),
                    new Flashcard(
                            null,
                            it.getId(),
                            "Framework",
                            "Framework",
                            "A platform for developing software applications"),
                    new Flashcard(null, it.getId(), "Bug", "Bug", "An error in a computer program"),
                    new Flashcard(
                            null,
                            it.getId(),
                            "Version Control",
                            "Version Control",
                            "Managing changes to documents and code"));

            List<Flashcard> englishCards = List.of(
                    new Flashcard(null, english.getId(), "Apple", "A fruit", "I eat an apple every day"),
                    new Flashcard(null, english.getId(), "Beautiful", "Attractive", "She has beautiful eyes"),
                    new Flashcard(
                            null,
                            english.getId(),
                            "Computer",
                            "A machine for processing data",
                            "I work on my computer"),
                    new Flashcard(null, english.getId(), "Dog", "An animal", "My dog is very friendly"),
                    new Flashcard(
                            null,
                            english.getId(),
                            "Education",
                            "The process of learning",
                            "Education is very important"));

            // Save all flashcards to their respective decks
            travelCards.forEach(cards::save);
            itCards.forEach(cards::save);
            englishCards.forEach(cards::save);

            // Create welcome news items only if no news exist
            if (news.findAllOrderByCreatedDesc().isEmpty()) {
                news.save(new News(
                        null,
                        "Welcome to Flashcards!",
                        "Our app helps you efficiently learn new words and phrases. "
                                + "Create decks, practice, and track your progress.",
                        "admin",
                        java.time.LocalDateTime.now()));

                news.save(new News(
                        null,
                        "New features in the application",
                        "We have added statistics tracking, practice settings, and much more. "
                                + "Stay tuned for updates!",
                        "admin",
                        java.time.LocalDateTime.now()));
            }
        };
    }
}
