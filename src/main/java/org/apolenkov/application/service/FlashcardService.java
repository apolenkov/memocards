package org.apolenkov.application.service;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for managing flashcards, decks and users with demo data
 */
@Service
public class FlashcardService {

    private final List<User> users = new ArrayList<>();
    private final List<Deck> decks = new ArrayList<>();
    private final List<Flashcard> flashcards = new ArrayList<>();
    
    private final AtomicLong userIdCounter = new AtomicLong(1);
    private final AtomicLong deckIdCounter = new AtomicLong(1);
    private final AtomicLong flashcardIdCounter = new AtomicLong(1);

    public FlashcardService() {
        initDemoData();
    }

    private void initDemoData() {
        // Create demo users
        User user1 = new User(userIdCounter.getAndIncrement(), "demo@example.com", "Demo User");
        users.add(user1);

        // Create demo decks
        Deck travelDeck = new Deck(deckIdCounter.getAndIncrement(), user1.getId(), 
                "Travel — фразы", "Короткие фразы для поездок");
        Deck itDeck = new Deck(deckIdCounter.getAndIncrement(), user1.getId(), 
                "IT — термины", "Основные термины программирования");
        Deck englishDeck = new Deck(deckIdCounter.getAndIncrement(), user1.getId(), 
                "English Basics", "Базовые английские слова");
        
        decks.add(travelDeck);
        decks.add(itDeck);
        decks.add(englishDeck);

        // Create demo flashcards for travel deck
        List<Flashcard> travelCards = List.of(
                new Flashcard(flashcardIdCounter.getAndIncrement(), travelDeck.getId(), 
                        "Hello", "Привет", "Hello, how are you?"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), travelDeck.getId(), 
                        "Thank you", "Спасибо", "Thank you very much!"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), travelDeck.getId(), 
                        "Excuse me", "Извините", "Excuse me, where is the station?"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), travelDeck.getId(), 
                        "How much?", "Сколько это стоит?", "How much does this cost?"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), travelDeck.getId(), 
                        "Where is...?", "Где находится...?", "Where is the nearest bank?")
        );

        // Create demo flashcards for IT deck
        List<Flashcard> itCards = List.of(
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "Algorithm", "Алгоритм", "A step-by-step procedure for solving a problem"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "Database", "База данных", "Organized collection of data"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "API", "Программный интерфейс", "Application Programming Interface"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "Framework", "Фреймворк", "A platform for developing software applications"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "Bug", "Ошибка в программе", "An error in a computer program"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), itDeck.getId(), 
                        "Version Control", "Система контроля версий", "Managing changes to documents and code")
        );

        // Create demo flashcards for English deck
        List<Flashcard> englishCards = List.of(
                new Flashcard(flashcardIdCounter.getAndIncrement(), englishDeck.getId(), 
                        "Apple", "Яблоко", "I eat an apple every day"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), englishDeck.getId(), 
                        "Beautiful", "Красивый", "She has beautiful eyes"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), englishDeck.getId(), 
                        "Computer", "Компьютер", "I work on my computer"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), englishDeck.getId(), 
                        "Dog", "Собака", "My dog is very friendly"),
                new Flashcard(flashcardIdCounter.getAndIncrement(), englishDeck.getId(), 
                        "Education", "Образование", "Education is very important")
        );

        flashcards.addAll(travelCards);
        flashcards.addAll(itCards);
        flashcards.addAll(englishCards);

        // Set flashcards to decks
        travelDeck.setFlashcards(travelCards);
        itDeck.setFlashcards(itCards);
        englishDeck.setFlashcards(englishCards);
        
        // Debug output
        System.out.println("Created decks with IDs:");
        System.out.println("Travel deck ID: " + travelDeck.getId());
        System.out.println("IT deck ID: " + itDeck.getId());
        System.out.println("English deck ID: " + englishDeck.getId());
    }

    // User methods
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public Optional<User> getUserById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public User getCurrentUser() {
        return users.get(0); // For demo, return first user
    }

    // Deck methods
    public List<Deck> getAllDecks() {
        return new ArrayList<>(decks);
    }

    public List<Deck> getDecksByUserId(Long userId) {
        return decks.stream()
                .filter(d -> d.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public Optional<Deck> getDeckById(Long id) {
        return decks.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    public Deck saveDeck(Deck deck) {
        if (deck.getId() == null) {
            deck.setId(deckIdCounter.getAndIncrement());
            decks.add(deck);
        } else {
            // Update existing deck
            for (int i = 0; i < decks.size(); i++) {
                if (decks.get(i).getId().equals(deck.getId())) {
                    decks.set(i, deck);
                    break;
                }
            }
        }
        return deck;
    }

    public void deleteDeck(Long id) {
        decks.removeIf(d -> d.getId().equals(id));
        flashcards.removeIf(f -> f.getDeckId().equals(id));
    }

    // Flashcard methods
    public List<Flashcard> getFlashcardsByDeckId(Long deckId) {
        return flashcards.stream()
                .filter(f -> f.getDeckId().equals(deckId))
                .collect(Collectors.toList());
    }

    public Optional<Flashcard> getFlashcardById(Long id) {
        return flashcards.stream().filter(f -> f.getId().equals(id)).findFirst();
    }

    public Flashcard saveFlashcard(Flashcard flashcard) {
        if (flashcard.getId() == null) {
            flashcard.setId(flashcardIdCounter.getAndIncrement());
            flashcards.add(flashcard);
        } else {
            // Update existing flashcard
            for (int i = 0; i < flashcards.size(); i++) {
                if (flashcards.get(i).getId().equals(flashcard.getId())) {
                    flashcards.set(i, flashcard);
                    break;
                }
            }
        }
        
        // Update deck's flashcards list
        getDeckById(flashcard.getDeckId()).ifPresent(deck -> {
            List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
            deck.setFlashcards(deckCards);
        });
        
        return flashcard;
    }

    public void deleteFlashcard(Long id) {
        Optional<Flashcard> flashcard = getFlashcardById(id);
        if (flashcard.isPresent()) {
            Long deckId = flashcard.get().getDeckId();
            flashcards.removeIf(f -> f.getId().equals(id));
            
            // Update deck's flashcards list
            getDeckById(deckId).ifPresent(deck -> {
                List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
                deck.setFlashcards(deckCards);
            });
        }
    }

    // Practice methods
    public List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random) {
        List<Flashcard> allCards = getFlashcardsByDeckId(deckId);
        
        if (random) {
            allCards = new ArrayList<>(allCards);
            java.util.Collections.shuffle(allCards);
        }
        
        return allCards.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}
