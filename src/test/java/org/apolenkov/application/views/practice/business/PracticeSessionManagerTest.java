package org.apolenkov.application.views.practice.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.apolenkov.application.model.Card;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PracticeSessionManager Tests")
class PracticeSessionManagerTest {

    @Mock
    private PracticeSessionService practiceSessionService;

    private final PracticeSessionManager sessionManager = new PracticeSessionManager();

    private List<Card> testCards;
    private PracticeSession testSession;

    @Test
    @DisplayName("Should detect complete session")
    void shouldDetectCompleteSession() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        // Session is complete when index >= cards.size()
        PracticeSession completeSession =
                testSession.withState(testSession.state().withIndex(1));

        assertThat(sessionManager.isComplete(completeSession)).isTrue();
    }

    @Test
    @DisplayName("Should detect incomplete session")
    void shouldDetectIncompleteSession() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        assertThat(sessionManager.isComplete(testSession)).isFalse();
    }

    @Test
    @DisplayName("Should return null for complete session current card")
    void shouldReturnNullForCompleteSessionCurrentCard() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession completeSession =
                testSession.withState(testSession.state().withIndex(1));

        assertThat(sessionManager.currentCard(completeSession)).isNull();
    }

    @Test
    @DisplayName("Should return current card for incomplete session")
    void shouldReturnCurrentCardForIncompleteSession() {
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        Card currentCard = sessionManager.currentCard(testSession);

        assertThat(currentCard).isEqualTo(testCards.getFirst());
    }

    @Test
    @DisplayName("Should start question and reset answer display")
    void shouldStartQuestionAndResetAnswerDisplay() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession updated = sessionManager.startQuestion(testSession);

        assertThat(updated.isShowingAnswer()).isFalse();
        assertThat(updated.getCardShowTime()).isNotNull();
        assertThat(updated.getIndex()).isZero(); // Should remain unchanged
    }

    @Test
    @DisplayName("Should reveal answer and calculate delay")
    void shouldRevealAnswerAndCalculateDelay() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        // Start question first
        PracticeSession questionSession = sessionManager.startQuestion(testSession);

        // Simulate some delay by creating a session with a past timestamp
        PracticeSession delayedSession = questionSession.withState(
                questionSession.state().withCardShowTime(Instant.now().minusMillis(50)));

        PracticeSession revealed = sessionManager.reveal(delayedSession);

        assertThat(revealed.isShowingAnswer()).isTrue();
        assertThat(revealed.getTotalAnswerDelayMs()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should reveal answer without delay when no card show time")
    void shouldRevealAnswerWithoutDelayWhenNoCardShowTime() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession revealed = sessionManager.reveal(testSession);

        assertThat(revealed.isShowingAnswer()).isTrue();
        assertThat(revealed.getTotalAnswerDelayMs()).isZero();
    }

    @Test
    @DisplayName("Should mark card as known and advance")
    void shouldMarkCardAsKnownAndAdvance() {
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession updated = sessionManager.markKnow(testSession);

        assertThat(updated.getIndex()).isEqualTo(1);
        assertThat(updated.getCorrectCount()).isEqualTo(1);
        assertThat(updated.getTotalViewed()).isEqualTo(1);
        assertThat(updated.isShowingAnswer()).isFalse();
        assertThat(updated.getKnownCardIdsDelta()).hasSize(1).contains(1L);
        assertThat(updated.getFailedCardIds()).isEmpty();
    }

    @Test
    @DisplayName("Should mark card as hard and advance")
    void shouldMarkCardAsHardAndAdvance() {
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession updated = sessionManager.markHard(testSession);

        assertThat(updated.getIndex()).isEqualTo(1);
        assertThat(updated.getHardCount()).isEqualTo(1);
        assertThat(updated.getTotalViewed()).isEqualTo(1);
        assertThat(updated.isShowingAnswer()).isFalse();
        assertThat(updated.getFailedCardIds()).hasSize(1).contains(1L);
        assertThat(updated.getKnownCardIdsDelta()).isEmpty();
    }

    @Test
    @DisplayName("Should not advance for complete session")
    void shouldNotAdvanceForCompleteSession() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession completeSession =
                testSession.withState(testSession.state().withIndex(1));

        PracticeSession updated = sessionManager.markKnow(completeSession);

        assertThat(updated).isSameAs(completeSession);
    }

    @Test
    @DisplayName("Should calculate progress correctly")
    void shouldCalculateProgressCorrectly() {
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession updated = testSession.withState(testSession
                .state()
                .withIndex(1)
                .withCorrectCount(1)
                .withHardCount(1)
                .withTotalViewed(2));

        PracticeSessionManager.Progress progress = sessionManager.progress(updated);

        assertThat(progress.current()).isEqualTo(2); // 1-based index
        assertThat(progress.total()).isEqualTo(3);
        assertThat(progress.totalViewed()).isEqualTo(2);
        assertThat(progress.correct()).isEqualTo(1);
        assertThat(progress.hard()).isEqualTo(1);
        assertThat(progress.percent()).isEqualTo(67); // 2/3 * 100
    }

    @Test
    @DisplayName("Should calculate progress for single card")
    void shouldCalculateProgressForSingleCard() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSessionManager.Progress progress = sessionManager.progress(testSession);

        assertThat(progress.current()).isEqualTo(1);
        assertThat(progress.total()).isEqualTo(1);
        assertThat(progress.percent()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should record and persist session")
    void shouldRecordAndPersistSession() {
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));
        testSession = PracticeSession.create(1L, testCards, Instant.now());

        PracticeSession completedSession = testSession
                .withState(testSession
                        .state()
                        .withCorrectCount(1)
                        .withHardCount(0)
                        .withTotalViewed(1)
                        .withAnswerDelay(1000L))
                .withData(testSession.data().addKnownCard(1L));

        // This should not throw an exception
        sessionManager.recordAndPersist(completedSession, practiceSessionService);

        // Verify that the service was called with correct parameters
        verify(practiceSessionService)
                .recordSession(eq(1L), eq(1), eq(1), eq(0), any(Duration.class), eq(1000L), eq(List.of(1L)));
    }
}
