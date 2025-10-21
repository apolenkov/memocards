package org.apolenkov.application.views.practice.business;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.views.practice.business.PracticeSessionRecords.SessionData;
import org.apolenkov.application.views.practice.business.PracticeSessionRecords.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service for managing active practice session state and operations.
 * Handles card progression, timing, and session statistics.
 */
@Component
public final class PracticeSessionManager {

    // ==================== Fields ====================

    // Dependencies
    private final Clock clock;

    // ==================== Constructors ====================

    /**
     * Creates a new PracticeSessionManager with system clock.
     */
    public PracticeSessionManager() {
        this(Clock.systemUTC());
    }

    /**
     * Creates a new PracticeSessionManager with specified clock.
     *
     * @param clockValue the clock to use for time operations
     * @throws IllegalArgumentException if clock is null
     */
    @Autowired
    public PracticeSessionManager(final Clock clockValue) {
        if (clockValue == null) {
            throw new IllegalArgumentException("Clock cannot be null");
        }
        this.clock = clockValue;
    }

    // ==================== Public API ====================

    /**
     * Checks if a practice session is complete.
     *
     * @param session the session to check
     * @return true if the session is complete, false otherwise
     */
    public boolean isComplete(final PracticeSession session) {
        return session.getCards() == null
                || session.getCards().isEmpty()
                || session.getIndex() >= session.getCards().size();
    }

    /**
     * Retrieves the current card in the practice session.
     *
     * @param session the session to get the current card from
     * @return the current card, or null if session is complete
     */
    public Card currentCard(final PracticeSession session) {
        if (isComplete(session)) {
            return null;
        }
        return session.getCards().get(session.getIndex());
    }

    /**
     * Starts a new question in the practice session.
     * Resets the answer display and records the start time for timing calculations.
     *
     * @param session the session to start the question for
     * @return updated session with new question state
     */
    public PracticeSession startQuestion(final PracticeSession session) {
        SessionState newState = session.state().withShowingAnswer(false).withCardShowTime(clock.instant());
        return session.withState(newState);
    }

    /**
     * Reveals the answer for the current card.
     * Records the time spent before revealing the answer.
     *
     * @param session the session containing the current card
     * @return updated session with answer revealed
     */
    public PracticeSession reveal(final PracticeSession session) {
        if (session.getCardShowTime() != null) {
            // Calculate time spent thinking about this card
            long delay =
                    clock.instant().toEpochMilli() - session.getCardShowTime().toEpochMilli();
            // Ensure delay is non-negative (handle edge cases with system clock)
            long clampedDelay = Math.clamp(delay, 0L, Long.MAX_VALUE);

            SessionState newState =
                    session.state().withAnswerDelay(clampedDelay).withShowingAnswer(true);
            return session.withState(newState);
        }

        SessionState newState = session.state().withShowingAnswer(true);
        return session.withState(newState);
    }

    /**
     * Marks the current card as known and advances to the next card.
     * Records that the user successfully answered the current card correctly.
     * Updates session statistics and moves to the next card in the sequence.
     *
     * @param session the session to update
     * @return updated session with card marked as known
     */
    public PracticeSession markKnow(final PracticeSession session) {
        if (isComplete(session)) {
            return session;
        }

        Card currentCard = Objects.requireNonNull(currentCard(session));

        // Update session data with known card
        SessionData newData = session.data().addKnownCard(currentCard.getId());

        // Update session state with new statistics
        SessionState newState = session.state()
                .withIndex(session.getIndex() + 1)
                .withShowingAnswer(false)
                .withCorrectCount(session.getCorrectCount() + 1)
                .withTotalViewed(session.getTotalViewed() + 1);

        return session.withData(newData).withState(newState);
    }

    /**
     * Marks the current card as difficult and advances to the next card.
     * Records that the user found the current card challenging. Updates
     * session statistics and moves to the next card in the sequence.
     *
     * @param session the session to update
     * @return updated session with card marked as hard
     */
    public PracticeSession markHard(final PracticeSession session) {
        if (isComplete(session)) {
            return session;
        }

        Card currentCard = Objects.requireNonNull(currentCard(session));

        // Update session data with failed card
        SessionData newData = session.data().addFailedCard(currentCard.getId());

        // Update session state with new statistics
        SessionState newState = session.state()
                .withIndex(session.getIndex() + 1)
                .withShowingAnswer(false)
                .withHardCount(session.getHardCount() + 1)
                .withTotalViewed(session.getTotalViewed() + 1);

        return session.withData(newData).withState(newState);
    }

    /**
     * Calculates current progress information for a practice session.
     *
     * @param session the session to calculate progress for
     * @return a Progress record with current session metrics
     */
    public Progress progress(final PracticeSession session) {
        // Get total cards count, handling null/empty cases
        int total = session.getCards() != null ? session.getCards().size() : 0;

        // Calculate current position (1-based) with bounds checking
        long current = Math.clamp(session.getIndex() + 1L, 1L, total);

        // Calculate completion percentage, avoiding division by zero
        long percent = total > 0 ? Math.round((current * 100.0) / total) : 0;

        return new Progress(
                current, total, session.getTotalViewed(), session.getCorrectCount(), session.getHardCount(), percent);
    }

    /**
     * Records and persists a completed practice session.
     *
     * @param session the completed session to record
     * @param sessionService the service for recording session data
     */
    public void recordAndPersist(final PracticeSession session, final PracticeSessionService sessionService) {
        // Calculate total session duration from start to completion
        long durationMs =
                clock.instant().toEpochMilli() - session.getSessionStart().toEpochMilli();
        Duration duration = Duration.ofMillis(durationMs);
        sessionService.recordSession(
                session.getDeckId(),
                session.getTotalViewed(),
                session.getCorrectCount(),
                session.getHardCount(),
                duration,
                session.getTotalAnswerDelayMs(),
                session.getKnownCardIdsDelta());
    }

    /**
     * Progress information for a practice session.
     * Contains comprehensive progress metrics for UI updates and analytics.
     *
     * @param current the current card position (1-based index)
     * @param total the total number of cards in the session
     * @param totalViewed the number of cards processed so far
     * @param correct the number of cards answered correctly
     * @param hard the number of cards marked as difficult
     * @param percent the completion percentage (0-100)
     */
    public record Progress(long current, int total, int totalViewed, int correct, int hard, long percent) {}
}
