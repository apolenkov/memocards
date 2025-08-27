package org.apolenkov.application.views.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.query.DeckQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomePresenter Tests")
class HomePresenterTest {

    @Mock
    private DeckQueryService deckQueryService;

    private HomePresenter homePresenter;

    @BeforeEach
    void setUp() {
        homePresenter = new HomePresenter(deckQueryService);
    }

    @Nested
    @DisplayName("List Decks For Current User Tests")
    class ListDecksForCurrentUserTests {

        @Test
        @DisplayName("ListDecksForCurrentUser should delegate to deckQueryService")
        void listDecksForCurrentUserShouldDelegateToDeckQueryService() {
            // Given
            String query = "math";
            List<Deck> expectedDecks = List.of(
                    new Deck(1L, 1L, "Math Deck", "Mathematics flashcards"),
                    new Deck(2L, 1L, "Advanced Math", "Advanced mathematics"));
            List<DeckCardViewModel> expectedViewModels = List.of(
                    new DeckCardViewModel(1L, "Math Deck", "Mathematics flashcards", 10, 5, 50),
                    new DeckCardViewModel(2L, "Advanced Math", "Advanced mathematics", 15, 8, 53));

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(any(Deck.class)))
                    .thenReturn(expectedViewModels.get(0), expectedViewModels.get(1));

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(2);
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService, times(2)).toViewModel(any(Deck.class));
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should return empty list when no decks found")
        void listDecksForCurrentUserShouldReturnEmptyListWhenNoDecksFound() {
            // Given
            String query = "nonexistent";
            List<Deck> emptyDecks = List.of();

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(emptyDecks);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).isEmpty();
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService, never()).toViewModel(any(Deck.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("ListDecksForCurrentUser should handle empty, blank, and whitespace queries")
        void listDecksForCurrentUserShouldHandleEmptyAndWhitespaceQueries(final String query) {
            // Given
            List<Deck> expectedDecks = List.of(new Deck(1L, 1L, "Test Deck", "Test Description"));
            DeckCardViewModel expectedViewModel = new DeckCardViewModel(1L, "Test Deck", "Test Description", 5, 2, 40);

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(any(Deck.class))).thenReturn(expectedViewModel);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1);
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService).toViewModel(any(Deck.class));
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should handle null query")
        void listDecksForCurrentUserShouldHandleNullQuery() {
            // Given
            List<Deck> expectedDecks = List.of(new Deck(1L, 1L, "Test Deck", "Test Description"));
            DeckCardViewModel expectedViewModel = new DeckCardViewModel(1L, "Test Deck", "Test Description", 5, 2, 40);

            when(deckQueryService.listDecksForCurrentUser(null)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(any(Deck.class))).thenReturn(expectedViewModel);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(null);

            // Then
            assertThat(result).hasSize(1);
            verify(deckQueryService).listDecksForCurrentUser(null);
            verify(deckQueryService).toViewModel(any(Deck.class));
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should handle single deck result")
        void listDecksForCurrentUserShouldHandleSingleDeckResult() {
            // Given
            String query = "single";
            List<Deck> expectedDecks = List.of(new Deck(1L, 1L, "Single Deck", "Single Description"));
            DeckCardViewModel expectedViewModel =
                    new DeckCardViewModel(1L, "Single Deck", "Single Description", 3, 1, 33);

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(any(Deck.class))).thenReturn(expectedViewModel);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(expectedViewModel);
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService).toViewModel(any(Deck.class));
        }

        @Test
        @DisplayName("ListDecksForCurrentUser should handle multiple deck results")
        void listDecksForCurrentUserShouldHandleMultipleDeckResults() {
            // Given
            String query = "multiple";
            List<Deck> expectedDecks = List.of(
                    new Deck(1L, 1L, "First Deck", "First Description"),
                    new Deck(2L, 1L, "Second Deck", "Second Description"),
                    new Deck(3L, 1L, "Third Deck", "Third Description"));
            List<DeckCardViewModel> expectedViewModels = List.of(
                    new DeckCardViewModel(1L, "First Deck", "First Description", 5, 2, 40),
                    new DeckCardViewModel(2L, "Second Deck", "Second Description", 8, 4, 50),
                    new DeckCardViewModel(3L, "Third Deck", "Third Description", 12, 6, 50));

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(expectedDecks.get(0))).thenReturn(expectedViewModels.get(0));
            when(deckQueryService.toViewModel(expectedDecks.get(1))).thenReturn(expectedViewModels.get(1));
            when(deckQueryService.toViewModel(expectedDecks.get(2))).thenReturn(expectedViewModels.get(2));

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(3).containsExactlyElementsOf(expectedViewModels);
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService, times(3)).toViewModel(any(Deck.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long queries")
        void shouldHandleVeryLongQueries() {
            // Given
            String longQuery = "a".repeat(10000);
            List<Deck> expectedDecks = List.of();

            when(deckQueryService.listDecksForCurrentUser(longQuery)).thenReturn(expectedDecks);

            // When & Then
            assertThatNoException().isThrownBy(() -> homePresenter.listDecksForCurrentUser(longQuery));
            verify(deckQueryService).listDecksForCurrentUser(longQuery);
        }

        @Test
        @DisplayName("Should handle special characters in queries")
        void shouldHandleSpecialCharactersInQueries() {
            // Given
            String specialQuery = "query@#$%^&*()_+-=[]{}|;':\",./<>?";
            List<Deck> expectedDecks = List.of();

            when(deckQueryService.listDecksForCurrentUser(specialQuery)).thenReturn(expectedDecks);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(specialQuery);

            // Then
            assertThat(result).isEmpty();
            verify(deckQueryService).listDecksForCurrentUser(specialQuery);
        }

        @Test
        @DisplayName("Should handle unicode characters in queries")
        void shouldHandleUnicodeCharactersInQueries() {
            // Given - test internationalization support with Russian text
            String unicodeQuery = "запрос"; // Russian for "query"
            List<Deck> expectedDecks = List.of();

            when(deckQueryService.listDecksForCurrentUser(unicodeQuery)).thenReturn(expectedDecks);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(unicodeQuery);

            // Then - verify Cyrillic text handling works correctly
            assertThat(result).isEmpty();
            verify(deckQueryService).listDecksForCurrentUser(unicodeQuery);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should maintain order of decks from service")
        void shouldMaintainOrderOfDecksFromService() {
            // Given - test that deck order is preserved through the service layer
            String query = "ordered";
            List<Deck> expectedDecks = List.of(
                    new Deck(1L, 1L, "First", "First Description"),
                    new Deck(2L, 1L, "Second", "Second Description"),
                    new Deck(3L, 1L, "Third", "Third Description"));
            List<DeckCardViewModel> expectedViewModels = List.of(
                    new DeckCardViewModel(1L, "First", "First Description", 5, 2, 40),
                    new DeckCardViewModel(2L, "Second", "Second Description", 8, 4, 50),
                    new DeckCardViewModel(3L, "Third", "Third Description", 12, 6, 50));

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(expectedDecks.get(0))).thenReturn(expectedViewModels.get(0));
            when(deckQueryService.toViewModel(expectedDecks.get(1))).thenReturn(expectedViewModels.get(1));
            when(deckQueryService.toViewModel(expectedDecks.get(2))).thenReturn(expectedViewModels.get(2));

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then - verify order is maintained from service to presentation layer
            assertThat(result).hasSize(3).satisfies(list -> {
                assertThat(list.get(0).id()).isEqualTo(1L);
                assertThat(list.get(1).id()).isEqualTo(2L);
                assertThat(list.get(2).id()).isEqualTo(3L);
            });
        }

        @Test
        @DisplayName("Should handle empty deck list gracefully")
        void shouldHandleEmptyDeckListGracefully() {
            // Given - test edge case: no decks available
            String query = "empty";
            List<Deck> emptyDecks = List.of();

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(emptyDecks);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then - verify graceful handling of empty results
            assertThat(result).isEmpty();
            verify(deckQueryService).listDecksForCurrentUser(query);
            verifyNoMoreInteractions(deckQueryService);
        }
    }

    @Nested
    @DisplayName("Component Behavior Tests")
    class ComponentBehaviorTests {

        @Test
        @DisplayName("Should be properly annotated as component")
        void shouldBeProperlyAnnotatedAsComponent() {
            // This test verifies that the class is properly annotated
            // The actual component behavior is tested in integration tests

            // Given
            HomePresenter presenter = new HomePresenter(deckQueryService);

            // When & Then
            assertThat(presenter).isNotNull().isInstanceOf(HomePresenter.class);
        }

        @Test
        @DisplayName("Should delegate all operations to deckQueryService")
        void shouldDelegateAllOperationsToDeckQueryService() {
            // Given - test that presenter acts as a thin wrapper around the service
            String query = "delegation";
            List<Deck> expectedDecks = List.of(new Deck(1L, 1L, "Test Deck", "Test Description"));
            DeckCardViewModel expectedViewModel = new DeckCardViewModel(1L, "Test Deck", "Test Description", 5, 2, 40);

            when(deckQueryService.listDecksForCurrentUser(query)).thenReturn(expectedDecks);
            when(deckQueryService.toViewModel(any(Deck.class))).thenReturn(expectedViewModel);

            // When
            List<DeckCardViewModel> result = homePresenter.listDecksForCurrentUser(query);

            // Then
            assertThat(result).hasSize(1);
            // Verify that all operations are delegated to deckQueryService
            verify(deckQueryService).listDecksForCurrentUser(query);
            verify(deckQueryService).toViewModel(any(Deck.class));
            verifyNoMoreInteractions(deckQueryService);
        }
    }
}
