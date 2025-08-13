package org.apolenkov.application.views.home;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.junit.jupiter.api.Test;

class HomePresenterTest {

    @Test
    void listDecks_filtersAndMaps() {
        UserUseCase userUseCase = new UserUseCase() {
            @Override
            public List<User> getAllUsers() {
                return List.of();
            }

            @Override
            public Optional<User> getUserById(Long id) {
                return Optional.empty();
            }

            @Override
            public User getCurrentUser() {
                return new User(1L, "u@u", "User");
            }
        };
        DeckUseCase deckUseCase = new DeckUseCase() {
            @Override
            public List<Deck> getAllDecks() {
                return List.of();
            }

            @Override
            public List<Deck> getDecksByUserId(Long userId) {
                return List.of(new Deck(10L, 1L, "Alpha", "A"), new Deck(11L, 1L, "Beta", "B"));
            }

            @Override
            public Optional<Deck> getDeckById(Long id) {
                return Optional.empty();
            }

            @Override
            public Deck saveDeck(Deck deck) {
                return deck;
            }

            @Override
            public void deleteDeck(Long id) {}
        };
        StatsService stats = new StatsService(
                new org.apolenkov.application.infrastructure.repository.memory.InMemoryStatsRepository());
        FlashcardUseCase flashcardUseCase = new FlashcardUseCase() {
            @Override
            public java.util.List<org.apolenkov.application.model.Flashcard> getFlashcardsByDeckId(Long deckId) {
                return java.util.List.of();
            }

            @Override
            public java.util.Optional<org.apolenkov.application.model.Flashcard> getFlashcardById(Long id) {
                return java.util.Optional.empty();
            }

            @Override
            public org.apolenkov.application.model.Flashcard saveFlashcard(
                    org.apolenkov.application.model.Flashcard flashcard) {
                return flashcard;
            }

            @Override
            public void deleteFlashcard(Long id) {}

            @Override
            public java.util.List<org.apolenkov.application.model.Flashcard> getFlashcardsForPractice(
                    Long deckId, int count, boolean random) {
                return java.util.List.of();
            }
        };
        HomePresenter presenter = new HomePresenter(deckUseCase, userUseCase, stats, flashcardUseCase);

        List<DeckCardViewModel> vms = presenter.listDecksForCurrentUser("a");
        assertFalse(vms.isEmpty());
        assertTrue(vms.stream().anyMatch(vm -> vm.title.equals("Alpha")));
    }
}
