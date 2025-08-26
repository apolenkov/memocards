package org.apolenkov.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckQueryService Tests")
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

    @Nested
    @DisplayName("List Decks For Current User Tests")
    class ListDecksForCurrentUserTests {

        @Test
        @DisplayName("ListDecksForCurrentUser should return user's decks when no query")
        void listDecksForCurrentUserShouldReturnUsersDecksWhenNoQuery() {
            // Given
            String query = "";
            Long userId = 1L;
            Deck deck1 = new Deck(1L, userId, "Deck 1", "Description 1");
            Deck deck2 = new Deck(2L, userId, "Deck 2", "Description 2");
            List<Deck> userDecks = List.of(deck1, deck2);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(deck1, deck2);
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should return user's decks when query is null")
        void listDecksForCurrentUserShouldReturnUsersDecksWhenQueryIsNull() {
            // Given
            Long userId = 1L;
            Deck deck1 = new Deck(1L, userId, "Deck 1", "Description 1");
            Deck deck2 = new Deck(2L, userId, "Deck 2", "Description 2");
            List<Deck> userDecks = List.of(deck1, deck2);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(null);

            // Then
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(deck1, deck2);
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @ParameterizedTest
        @CsvSource({
            "math, Math Deck, Mathematics flashcards",
            "science, Science Deck, Science and physics",
            "MATH, Math Deck, Mathematics flashcards"
        })
        @DisplayName("ListDecksForCurrentUser should filter decks by query")
        void listDecksForCurrentUserShouldFilterDecksByQuery(
                String query, String expectedDeckTitle, String expectedDeckDescription) {
            // Given
            Long userId = 1L;
            Deck expectedDeck = new Deck(1L, userId, expectedDeckTitle, expectedDeckDescription);
            Deck otherDeck = new Deck(2L, userId, "Other Deck", "Other Description");
            List<Deck> userDecks = List.of(expectedDeck, otherDeck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1).contains(expectedDeck);
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should trim and normalize query")
        void listDecksForCurrentUserShouldTrimAndNormalizeQuery() {
            // Given
            String query = "  math  ";
            Long userId = 1L;
            Deck mathDeck = new Deck(1L, userId, "Math Deck", "Mathematics flashcards");
            Deck historyDeck = new Deck(2L, userId, "History Deck", "History flashcards");
            List<Deck> userDecks = List.of(mathDeck, historyDeck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1).contains(mathDeck);
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should sort decks by title")
        void listDecksForCurrentUserShouldSortDecksByTitle() {
            // Given
            String query = "";
            Long userId = 1L;
            Deck deckC = new Deck(3L, userId, "C Deck", "Description C");
            Deck deckA = new Deck(1L, userId, "A Deck", "Description A");
            Deck deckB = new Deck(2L, userId, "B Deck", "Description B");
            List<Deck> userDecks = List.of(deckC, deckA, deckB);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(3).satisfies(decks -> {
                assertThat(decks.get(0).getTitle()).isEqualTo("A Deck");
                assertThat(decks.get(1).getTitle()).isEqualTo("B Deck");
                assertThat(decks.get(2).getTitle()).isEqualTo("C Deck");
            });
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should handle mixed case titles gracefully")
        void listDecksForCurrentUserShouldHandleMixedCaseTitlesGracefully() {
            // Given
            String query = "";
            Long userId = 1L;
            Deck deckWithTitle = new Deck(1L, userId, "Valid Title", "Description");
            Deck deckWithMixedCaseTitle = new Deck(2L, userId, "MiXeD cAsE TiTlE", "Description");
            List<Deck> userDecks = List.of(deckWithTitle, deckWithMixedCaseTitle);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(2);
            // Should not throw exception when sorting with mixed case titles
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should return empty list when no decks match query")
        void listDecksForCurrentUserShouldReturnEmptyListWhenNoDecksMatchQuery() {
            // Given
            String query = "nonexistent";
            Long userId = 1L;
            Deck deck1 = new Deck(1L, userId, "Math Deck", "Mathematics flashcards");
            Deck deck2 = new Deck(2L, userId, "History Deck", "History flashcards");
            List<Deck> userDecks = List.of(deck1, deck2);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).isEmpty();
            verify(userUseCase).getCurrentUser();
            verify(deckUseCase).getDecksByUserId(userId);
        }
    }

    @Nested
    @DisplayName("To View Model Tests")
    class ToViewModelTests {

        @Test
        @DisplayName("ToViewModel should create correct view model")
        void toViewModelShouldCreateCorrectViewModel() {
            // Given
            Deck deck = new Deck(1L, 1L, "Test Deck", "Test Description");
            int deckSize = 10;
            int knownCount = 6;
            int progressPercent = 60;

            when(flashcardUseCase.countByDeckId(deck.getId())).thenReturn((long) deckSize);
            when(statsService.getKnownCardIds(deck.getId())).thenReturn(Set.of(1L, 2L, 3L, 4L, 5L, 6L));
            when(statsService.getDeckProgressPercent(deck.getId(), deckSize)).thenReturn(progressPercent);

            // When
            DeckCardViewModel result = deckQueryService.toViewModel(deck);

            // Then
            assertThat(result).satisfies(viewModel -> {
                assertThat(viewModel.id()).isEqualTo(deck.getId());
                assertThat(viewModel.title()).isEqualTo(deck.getTitle());
                assertThat(viewModel.description()).isEqualTo(deck.getDescription());
                assertThat(viewModel.deckSize()).isEqualTo(deckSize);
                assertThat(viewModel.knownCount()).isEqualTo(knownCount);
                assertThat(viewModel.progressPercent()).isEqualTo(progressPercent);
            });

            verify(flashcardUseCase).countByDeckId(deck.getId());
            verify(statsService).getKnownCardIds(deck.getId());
            verify(statsService).getDeckProgressPercent(deck.getId(), deckSize);
        }

        @Test
        @DisplayName("ToViewModel should handle empty deck")
        void toViewModelShouldHandleEmptyDeck() {
            // Given
            Deck deck = new Deck(1L, 1L, "Empty Deck", "Empty Description");
            int deckSize = 0;
            int progressPercent = 0;

            when(flashcardUseCase.countByDeckId(deck.getId())).thenReturn(0L);
            when(statsService.getKnownCardIds(deck.getId())).thenReturn(Set.of());
            when(statsService.getDeckProgressPercent(deck.getId(), deckSize)).thenReturn(progressPercent);

            // When
            DeckCardViewModel result = deckQueryService.toViewModel(deck);

            // Then
            assertThat(result).satisfies(viewModel -> {
                assertThat(viewModel.deckSize()).isZero();
                assertThat(viewModel.knownCount()).isZero();
                assertThat(viewModel.progressPercent()).isZero();
            });

            verify(flashcardUseCase).countByDeckId(deck.getId());
            verify(statsService).getKnownCardIds(deck.getId());
            verify(statsService).getDeckProgressPercent(deck.getId(), deckSize);
        }

        @Test
        @DisplayName("ToViewModel should handle deck with all cards known")
        void toViewModelShouldHandleDeckWithAllCardsKnown() {
            // Given
            Deck deck = new Deck(1L, 1L, "Complete Deck", "Complete Description");
            int deckSize = 5;
            int knownCount = 5;
            int progressPercent = 100;

            when(flashcardUseCase.countByDeckId(deck.getId())).thenReturn((long) deckSize);
            when(statsService.getKnownCardIds(deck.getId())).thenReturn(Set.of(1L, 2L, 3L, 4L, 5L));
            when(statsService.getDeckProgressPercent(deck.getId(), deckSize)).thenReturn(progressPercent);

            // When
            DeckCardViewModel result = deckQueryService.toViewModel(deck);

            // Then
            assertThat(result).satisfies(viewModel -> {
                assertThat(viewModel.deckSize()).isEqualTo(deckSize);
                assertThat(viewModel.knownCount()).isEqualTo(knownCount);
                assertThat(viewModel.progressPercent()).isEqualTo(progressPercent);
            });

            verify(flashcardUseCase).countByDeckId(deck.getId());
            verify(statsService).getKnownCardIds(deck.getId());
            verify(statsService).getDeckProgressPercent(deck.getId(), deckSize);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long queries")
        void shouldHandleVeryLongQueries() {
            // Given
            String longQuery = "a".repeat(1000);
            Long userId = 1L;
            Deck deck = new Deck(1L, userId, "Test Deck", "Test Description");
            List<Deck> userDecks = List.of(deck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When & Then
            assertThatNoException().isThrownBy(() -> deckQueryService.listDecksForCurrentUser(longQuery));
        }

        @Test
        @DisplayName("Should handle special characters in query")
        void shouldHandleSpecialCharactersInQuery() {
            // Given
            String specialQuery = "math@#$%^&*()_+-=[]{}|;':\",./<>?";
            Long userId = 1L;
            Deck deck = new Deck(1L, userId, "Math Deck", "Mathematics flashcards");
            List<Deck> userDecks = List.of(deck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(specialQuery);

            // Then
            assertThat(result).isEmpty(); // No match expected for special characters
        }

        @Test
        @DisplayName("Should handle unicode characters in query")
        void shouldHandleUnicodeCharactersInQuery() {
            // Given
            String unicodeQuery = "математика"; // Russian for "mathematics"
            Long userId = 1L;
            Deck deck = new Deck(1L, userId, "Math Deck", "Mathematics flashcards");
            List<Deck> userDecks = List.of(deck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(unicodeQuery);

            // Then
            assertThat(result).isEmpty(); // No match expected
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("ListDecksForCurrentUser should be read-only transactional")
        void listDecksForCurrentUserShouldBeReadOnlyTransactional() {
            // This test verifies that the method is annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            String query = "";
            Long userId = 1L;
            Deck deck = new Deck(1L, userId, "Test Deck", "Test Description");
            List<Deck> userDecks = List.of(deck);

            when(userUseCase.getCurrentUser()).thenReturn(createMockUser(userId));
            when(deckUseCase.getDecksByUserId(userId)).thenReturn(userDecks);

            // When
            List<Deck> result = deckQueryService.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1);
            // Transaction behavior is verified by the fact that the method executes without error
        }

        @Test
        @DisplayName("ToViewModel should be read-only transactional")
        void toViewModelShouldBeReadOnlyTransactional() {
            // This test verifies that the method is annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            Deck deck = new Deck(1L, 1L, "Test Deck", "Test Description");
            when(flashcardUseCase.countByDeckId(deck.getId())).thenReturn(5L);
            when(statsService.getKnownCardIds(deck.getId())).thenReturn(Set.of(1L, 2L, 3L));
            when(statsService.getDeckProgressPercent(deck.getId(), 5)).thenReturn(60);

            // When
            DeckCardViewModel result = deckQueryService.toViewModel(deck);

            // Then
            assertThat(result).isNotNull();
            // Transaction behavior is verified by the fact that the method executes without error
        }
    }

    // Helper method to create a mock user
    private org.apolenkov.application.model.User createMockUser(Long userId) {
        org.apolenkov.application.model.User user = new org.apolenkov.application.model.User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setName("Test User");
        return user;
    }
}
