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

@Configuration
@org.springframework.context.annotation.Profile({"dev"})
public class DataInitializer {

    @Bean
    @org.springframework.core.annotation.Order(20)
    CommandLineRunner initDemoData(
            UserRepository users, DeckRepository decks, FlashcardRepository cards, NewsRepository news) {
        return args -> {
            java.util.Optional<User> opt = users.findByEmail("user@example.com");
            if (opt.isEmpty()) {
                return; // no domain user yet → skip deck seeding; created lazily on first login
            }
            User user = opt.get();

            if (!decks.findByUserId(user.getId()).isEmpty()) return;

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

            travelCards.forEach(cards::save);
            itCards.forEach(cards::save);
            englishCards.forEach(cards::save);

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
