package org.apolenkov.application.config;

import java.util.List;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.context.annotation.Profile({"dev", "memory"})
public class DataInitializer {

    @Bean
    @org.springframework.core.annotation.Order(20)
    CommandLineRunner initDemoData(UserRepository users, DeckRepository decks, FlashcardRepository cards) {
        return args -> {
            // Local seeding of users moved to DevSecurityUsers; here we only seed decks for existing 'user'
            java.util.Optional<User> opt = users.findByEmail("user");
            if (opt.isEmpty()) {
                return; // no domain user yet → skip deck seeding; created lazily on first login
            }
            User user = opt.get();

            if (!decks.findByUserId(user.getId()).isEmpty()) return;

            Deck travel = decks.save(new Deck(null, user.getId(), "Travel — фразы", "Короткие фразы для поездок"));
            Deck it = decks.save(new Deck(null, user.getId(), "IT — термины", "Основные термины программирования"));
            Deck english = decks.save(new Deck(null, user.getId(), "English Basics", "Базовые английские слова"));

            List<Flashcard> travelCards = List.of(
                    new Flashcard(null, travel.getId(), "Hello", "Привет", "Hello, how are you?"),
                    new Flashcard(null, travel.getId(), "Thank you", "Спасибо", "Thank you very much!"),
                    new Flashcard(null, travel.getId(), "Excuse me", "Извините", "Excuse me, where is the station?"),
                    new Flashcard(null, travel.getId(), "How much?", "Сколько это стоит?", "How much does this cost?"),
                    new Flashcard(
                            null, travel.getId(), "Where is...?", "Где находится...?", "Where is the nearest bank?"));

            List<Flashcard> itCards = List.of(
                    new Flashcard(
                            null,
                            it.getId(),
                            "Algorithm",
                            "Алгоритм",
                            "A step-by-step procedure for solving a problem"),
                    new Flashcard(null, it.getId(), "Database", "База данных", "Organized collection of data"),
                    new Flashcard(
                            null, it.getId(), "API", "Программный интерфейс", "Application Programming Interface"),
                    new Flashcard(
                            null,
                            it.getId(),
                            "Framework",
                            "Фреймворк",
                            "A platform for developing software applications"),
                    new Flashcard(null, it.getId(), "Bug", "Ошибка в программе", "An error in a computer program"),
                    new Flashcard(
                            null,
                            it.getId(),
                            "Version Control",
                            "Система контроля версий",
                            "Managing changes to documents and code"));

            List<Flashcard> englishCards = List.of(
                    new Flashcard(null, english.getId(), "Apple", "Яблоко", "I eat an apple every day"),
                    new Flashcard(null, english.getId(), "Beautiful", "Красивый", "She has beautiful eyes"),
                    new Flashcard(null, english.getId(), "Computer", "Компьютер", "I work on my computer"),
                    new Flashcard(null, english.getId(), "Dog", "Собака", "My dog is very friendly"),
                    new Flashcard(null, english.getId(), "Education", "Образование", "Education is very important"));

            travelCards.forEach(cards::save);
            itCards.forEach(cards::save);
            englishCards.forEach(cards::save);
        };
    }
}
