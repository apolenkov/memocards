package org.apolenkov.application.views.home;

import org.apolenkov.application.application.usecase.DeckUseCase;
import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.StatsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HomePresenterTest {

    @Test
    void listDecks_filtersAndMaps() {
        UserUseCase userUseCase = new UserUseCase() {
            @Override public List<User> getAllUsers() { return List.of(); }
            @Override public Optional<User> getUserById(Long id) { return Optional.empty(); }
            @Override public User getCurrentUser() { return new User(1L, "u@u", "User"); }
        };
        DeckUseCase deckUseCase = new DeckUseCase() {
            @Override public List<Deck> getAllDecks() { return List.of(); }
            @Override public List<Deck> getDecksByUserId(Long userId) { return List.of(
                    new Deck(10L, 1L, "Alpha", "A"),
                    new Deck(11L, 1L, "Beta", "B")
            ); }
            @Override public Optional<Deck> getDeckById(Long id) { return Optional.empty(); }
            @Override public Deck saveDeck(Deck deck) { return deck; }
            @Override public void deleteDeck(Long id) { }
        };
        StatsService stats = new StatsService(new org.apolenkov.application.infrastructure.repository.memory.InMemoryStatsRepository());
        HomePresenter presenter = new HomePresenter(deckUseCase, userUseCase, stats);

        List<DeckCardViewModel> vms = presenter.listDecksForCurrentUser("a");
        assertFalse(vms.isEmpty());
        assertTrue(vms.stream().anyMatch(vm -> vm.title.equals("Alpha")));
    }
}


