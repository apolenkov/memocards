package org.apolenkov.application.views.practice.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PracticeSession Tests")
class PracticeSessionTest {

    @Test
    @DisplayName("Should create PracticeSession with valid parameters")
    void shouldCreatePracticeSessionWithValidParameters() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());

        assertThat(session.getDeckId()).isEqualTo(1L);
        assertThat(session.getCards()).hasSize(1);
        assertThat(session.getIndex()).isZero();
        assertThat(session.isShowingAnswer()).isFalse();
        assertThat(session.getCorrectCount()).isZero();
        assertThat(session.getHardCount()).isZero();
        assertThat(session.getTotalViewed()).isZero();
        assertThat(session.getSessionStart()).isNotNull();
        assertThat(session.getCardShowTime()).isNull();
        assertThat(session.getTotalAnswerDelayMs()).isZero();
        assertThat(session.getKnownCardIdsDelta()).isEmpty();
        assertThat(session.getFailedCardIds()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for invalid deck ID")
    void shouldThrowExceptionForInvalidDeckId() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSession.create(0L, cards, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck ID must be positive");
    }

    @Test
    @DisplayName("Should throw exception for null cards")
    void shouldThrowExceptionForNullCards() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSession.create(1L, null, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cards list cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for empty cards")
    void shouldThrowExceptionForEmptyCards() {
        List<Flashcard> emptyCards = List.of();
        Instant now = Instant.now();

        assertThatThrownBy(() -> PracticeSession.create(1L, emptyCards, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cards list cannot be empty");
    }

    @Test
    @DisplayName("Should update session with new state")
    void shouldUpdateSessionWithNewState() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());
        PracticeSessionRecords.SessionState newState = PracticeSessionRecords.SessionState.initial()
                .withIndex(1)
                .withShowingAnswer(true)
                .withCorrectCount(1);

        PracticeSession updated = session.withState(newState);

        assertThat(updated.getIndex()).isEqualTo(1);
        assertThat(updated.isShowingAnswer()).isTrue();
        assertThat(updated.getCorrectCount()).isEqualTo(1);
        assertThat(updated.getDeckId()).isEqualTo(1L); // Should remain unchanged
    }

    @Test
    @DisplayName("Should update session with new data")
    void shouldUpdateSessionWithNewData() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());
        PracticeSessionRecords.SessionData newData = PracticeSessionRecords.SessionData.create(2L, cards, Instant.now())
                .addKnownCard(1L);

        PracticeSession updated = session.withData(newData);

        assertThat(updated.getDeckId()).isEqualTo(2L);
        assertThat(updated.getKnownCardIdsDelta()).hasSize(1).contains(1L);
        assertThat(updated.getIndex()).isZero(); // Should remain unchanged
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null state")
    void shouldThrowExceptionForNullState() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());

        assertThatThrownBy(() -> session.withState(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session state cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null data")
    void shouldThrowExceptionForNullData() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());

        assertThatThrownBy(() -> session.withData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session data cannot be null");
    }

    @Test
    @DisplayName("Should access nested data through convenience methods")
    void shouldAccessNestedDataThroughConvenienceMethods() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession session = PracticeSession.create(1L, cards, Instant.now());

        // Test data access
        assertThat(session.getDeckId()).isEqualTo(session.data().deckId());
        assertThat(session.getCards()).isEqualTo(session.data().cards());
        assertThat(session.getSessionStart()).isEqualTo(session.data().sessionStart());
        assertThat(session.getKnownCardIdsDelta()).isEqualTo(session.data().knownCardIdsDelta());
        assertThat(session.getFailedCardIds()).isEqualTo(session.data().failedCardIds());

        // Test state access
        assertThat(session.getIndex()).isEqualTo(session.state().index());
        assertThat(session.isShowingAnswer()).isEqualTo(session.state().showingAnswer());
        assertThat(session.getCorrectCount()).isEqualTo(session.state().correctCount());
        assertThat(session.getHardCount()).isEqualTo(session.state().hardCount());
        assertThat(session.getTotalViewed()).isEqualTo(session.state().totalViewed());
        assertThat(session.getCardShowTime()).isEqualTo(session.state().cardShowTime());
        assertThat(session.getTotalAnswerDelayMs()).isEqualTo(session.state().totalAnswerDelayMs());
    }

    @Test
    @DisplayName("Should maintain immutability")
    void shouldMaintainImmutability() {
        List<Flashcard> cards = List.of(new Flashcard(1L, 1L, "Front", "Back", "Example"));

        PracticeSession original = PracticeSession.create(1L, cards, Instant.now());
        PracticeSession updated =
                original.withState(original.state().withIndex(1).withCorrectCount(1));

        // Original should remain unchanged
        assertThat(original.getIndex()).isZero();
        assertThat(original.getCorrectCount()).isZero();

        // Updated should have new values
        assertThat(updated.getIndex()).isEqualTo(1);
        assertThat(updated.getCorrectCount()).isEqualTo(1);

        // They should be different objects
        assertThat(original).isNotSameAs(updated);
    }
}
