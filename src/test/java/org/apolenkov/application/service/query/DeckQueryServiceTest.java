package org.apolenkov.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckQueryService Core Tests")
class DeckQueryServiceTest {

    @Mock
    private DeckUseCase deckUseCase;

    @Mock
    private FlashcardUseCase flashcardUseCase;

    @Mock
    private StatsService statsService;

    @Mock
    private UserUseCase userUseCase;

    private DeckQueryService deckQueryService;

    @BeforeEach
    void setUp() {
        deckQueryService = new DeckQueryService(deckUseCase, flashcardUseCase, statsService, userUseCase);
    }

    @Test
    @DisplayName("Should list decks for current user")
    void shouldListDecksForCurrentUser() {
        // Given
        User currentUser = new User(1L, "user@example.com", "User Name");
        List<Deck> expectedDecks =
                List.of(new Deck(1L, 1L, "Deck 1", "Description 1"), new Deck(2L, 1L, "Deck 2", "Description 2"));
        when(userUseCase.getCurrentUser()).thenReturn(currentUser);
        when(deckUseCase.getDecksByUserId(1L)).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckQueryService.listDecksForCurrentUser(null);

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedDecks);
    }

    @Test
    @DisplayName("Should filter decks by search query")
    void shouldFilterDecksBySearchQuery() {
        // Given
        User currentUser = new User(1L, "user@example.com", "User Name");
        List<Deck> allDecks = List.of(
                new Deck(1L, 1L, "Math Deck", "Mathematics"),
                new Deck(2L, 1L, "History Deck", "History"),
                new Deck(3L, 1L, "Science Deck", "Science"));
        when(userUseCase.getCurrentUser()).thenReturn(currentUser);
        when(deckUseCase.getDecksByUserId(1L)).thenReturn(allDecks);

        // When
        List<Deck> result = deckQueryService.listDecksForCurrentUser("Math");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).contains("Math");
    }

    @Test
    @DisplayName("Should convert deck to view model")
    void shouldConvertDeckToViewModel() {
        // Given
        Deck deck = new Deck(1L, 1L, "Test Deck", "Description");
        when(flashcardUseCase.countByDeckId(1L)).thenReturn(10L);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(1L, 2L, 3L));
        when(statsService.getDeckProgressPercent(1L, 10)).thenReturn(30);

        // When
        DeckCardViewModel result = deckQueryService.toViewModel(deck);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Deck");
        assertThat(result.deckSize()).isEqualTo(10);
        assertThat(result.knownCount()).isEqualTo(3);
        assertThat(result.progressPercent()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should handle empty deck list")
    void shouldHandleEmptyDeckList() {
        // Given
        User currentUser = new User(1L, "user@example.com", "User Name");
        when(userUseCase.getCurrentUser()).thenReturn(currentUser);
        when(deckUseCase.getDecksByUserId(1L)).thenReturn(List.of());

        // When
        List<Deck> result = deckQueryService.listDecksForCurrentUser(null);

        // Then
        assertThat(result).isEmpty();
    }
}
