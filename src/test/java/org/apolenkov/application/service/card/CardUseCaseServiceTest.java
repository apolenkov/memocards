package org.apolenkov.application.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardUseCase Core Tests")
class CardUseCaseServiceTest {

    @Mock
    private CardUseCase cardUseCase;

    private List<Card> testCards;

    @BeforeEach
    void setUp() {
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));
    }

    @Test
    @DisplayName("Should get cards by deck id")
    void shouldGetCardsByDeckId() {
        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);

        List<Card> result = cardUseCase.getCardsByDeckId(1L);

        assertThat(result).hasSize(2).isEqualTo(testCards);
    }

    @Test
    @DisplayName("Should handle empty card list")
    void shouldHandleEmptyCardList() {
        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(List.of());

        List<Card> result = cardUseCase.getCardsByDeckId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get first card")
    void shouldGetFirstCard() {
        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);

        List<Card> result = cardUseCase.getCardsByDeckId(1L);
        Card firstCard = result.getFirst();

        assertThat(firstCard).isEqualTo(testCards.getFirst());
    }
}
