package org.apolenkov.application.views.practice.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PracticeSession Records Tests")
class PracticeSessionRecordsTest {

    @Test
    @DisplayName("Should create SessionData with valid parameters")
    void shouldCreateSessionDataWithValidParameters() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSessionRecords.SessionData sessionData =
                PracticeSessionRecords.SessionData.create(1L, cards, Instant.now());

        assertThat(sessionData.deckId()).isEqualTo(1L);
        assertThat(sessionData.cards()).hasSize(1);
        assertThat(sessionData.sessionStart()).isNotNull();
        assertThat(sessionData.knownCardIdsDelta()).isEmpty();
        assertThat(sessionData.failedCardIds()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for invalid deck ID")
    void shouldThrowExceptionForInvalidDeckId() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSessionRecords.SessionData.create(0L, cards, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck ID must be positive");
    }

    @Test
    @DisplayName("Should throw exception for null cards")
    void shouldThrowExceptionForNullCards() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSessionRecords.SessionData.create(1L, null, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cards list cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for empty cards")
    void shouldThrowExceptionForEmptyCards() {
        List<Flashcard> emptyCards = List.of();
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSessionRecords.SessionData.create(1L, emptyCards, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cards list cannot be empty");
    }

    @Test
    @DisplayName("Should add known card to SessionData")
    void shouldAddKnownCardToSessionData() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSessionRecords.SessionData sessionData =
                PracticeSessionRecords.SessionData.create(1L, cards, Instant.now());

        PracticeSessionRecords.SessionData updated = sessionData.addKnownCard(1L);

        assertThat(updated.knownCardIdsDelta()).hasSize(1).contains(1L);
        assertThat(updated.failedCardIds()).isEmpty();
    }

    @Test
    @DisplayName("Should add failed card to SessionData")
    void shouldAddFailedCardToSessionData() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSessionRecords.SessionData sessionData =
                PracticeSessionRecords.SessionData.create(1L, cards, Instant.now());

        PracticeSessionRecords.SessionData updated = sessionData.addFailedCard(1L);

        assertThat(updated.failedCardIds()).hasSize(1).contains(1L);
        assertThat(updated.knownCardIdsDelta()).isEmpty();
    }

    @Test
    @DisplayName("Should create initial SessionState")
    void shouldCreateInitialSessionState() {
        PracticeSessionRecords.SessionState state = PracticeSessionRecords.SessionState.initial();

        assertThat(state.index()).isZero();
        assertThat(state.showingAnswer()).isFalse();
        assertThat(state.correctCount()).isZero();
        assertThat(state.hardCount()).isZero();
        assertThat(state.totalViewed()).isZero();
        assertThat(state.cardShowTime()).isNull();
        assertThat(state.totalAnswerDelayMs()).isZero();
    }

    @Test
    @DisplayName("Should update SessionState with new index")
    void shouldUpdateSessionStateWithNewIndex() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withIndex(5);

        assertThat(updated.index()).isEqualTo(5);
        assertThat(updated.showingAnswer()).isFalse(); // Should reset
        assertThat(updated.cardShowTime()).isNull(); // Should reset
    }

    @Test
    @DisplayName("Should update SessionState with showing answer")
    void shouldUpdateSessionStateWithShowingAnswer() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withShowingAnswer(true);

        assertThat(updated.showingAnswer()).isTrue();
        assertThat(updated.index()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update SessionState with card show time")
    void shouldUpdateSessionStateWithCardShowTime() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();
        Instant now = Instant.now();

        PracticeSessionRecords.SessionState updated = initialState.withCardShowTime(now);

        assertThat(updated.cardShowTime()).isEqualTo(now);
        assertThat(updated.index()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update SessionState with answer delay")
    void shouldUpdateSessionStateWithAnswerDelay() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withAnswerDelay(1000L);

        assertThat(updated.totalAnswerDelayMs()).isEqualTo(1000L);
        assertThat(updated.index()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should accumulate answer delays")
    void shouldAccumulateAnswerDelays() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated =
                initialState.withAnswerDelay(500L).withAnswerDelay(300L);

        assertThat(updated.totalAnswerDelayMs()).isEqualTo(800L);
    }

    @Test
    @DisplayName("Should update SessionState with correct count")
    void shouldUpdateSessionStateWithCorrectCount() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withCorrectCount(3);

        assertThat(updated.correctCount()).isEqualTo(3);
        assertThat(updated.hardCount()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update SessionState with hard count")
    void shouldUpdateSessionStateWithHardCount() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withHardCount(2);

        assertThat(updated.hardCount()).isEqualTo(2);
        assertThat(updated.correctCount()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update SessionState with total viewed")
    void shouldUpdateSessionStateWithTotalViewed() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        PracticeSessionRecords.SessionState updated = initialState.withTotalViewed(10);

        assertThat(updated.totalViewed()).isEqualTo(10);
        assertThat(updated.correctCount()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should throw exception for negative index")
    void shouldThrowExceptionForNegativeIndex() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        assertThatThrownBy(() -> initialState.withIndex(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Index cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception for negative answer delay")
    void shouldThrowExceptionForNegativeAnswerDelay() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        assertThatThrownBy(() -> initialState.withAnswerDelay(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Answer delay cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception for negative correct count")
    void shouldThrowExceptionForNegativeCorrectCount() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        assertThatThrownBy(() -> initialState.withCorrectCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Correct count cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception for negative hard count")
    void shouldThrowExceptionForNegativeHardCount() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        assertThatThrownBy(() -> initialState.withHardCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hard count cannot be negative");
    }

    @Test
    @DisplayName("Should throw exception for negative total viewed")
    void shouldThrowExceptionForNegativeTotalViewed() {
        PracticeSessionRecords.SessionState initialState = PracticeSessionRecords.SessionState.initial();

        assertThatThrownBy(() -> initialState.withTotalViewed(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Total viewed cannot be negative");
    }
}
