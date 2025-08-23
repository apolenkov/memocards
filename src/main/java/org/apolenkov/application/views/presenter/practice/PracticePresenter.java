package org.apolenkov.application.views.presenter.practice;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Component;

/**
 * Presenter for managing flashcard practice sessions.
 *
 * <p>This component orchestrates the practice workflow, including session preparation,
 * progress tracking, and statistics recording. It provides a clean interface for
 * managing practice state and coordinates between the UI layer and business services.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Loading and filtering cards for practice sessions</li>
 *   <li>Managing session state and progress</li>
 *   <li>Recording user performance and session statistics</li>
 *   <li>Coordinating with practice settings and statistics services</li>
 * </ul>
 */
@Component
public class PracticePresenter {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;

    /**
     * Creates a new PracticePresenter with required dependencies.
     *
     * @param deckUseCase service for deck operations
     * @param flashcardUseCase service for flashcard operations
     * @param statsService service for statistics recording
     * @param practiceSettingsService service for practice configuration
     */
    public PracticePresenter(
            DeckUseCase deckUseCase,
            FlashcardUseCase flashcardUseCase,
            StatsService statsService,
            PracticeSettingsService practiceSettingsService) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
    }

    /**
     * Loads a deck by its ID.
     *
     * @param deckId the ID of the deck to load
     * @return an Optional containing the deck if found, empty otherwise
     */
    public Optional<Deck> loadDeck(long deckId) {
        return deckUseCase.getDeckById(deckId);
    }

    /**
     * Gets cards that are not yet marked as known in a deck.
     *
     * <p>Filters the deck's flashcards to return only those that the user
     * hasn't successfully learned yet. This is used to prepare practice
     * sessions with appropriate content.</p>
     *
     * @param deckId the ID of the deck to check
     * @return a list of flashcards not yet known by the user
     */
    public List<Flashcard> getNotKnownCards(long deckId) {
        List<Flashcard> all = flashcardUseCase.getFlashcardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return all.stream().filter(fc -> !known.contains(fc.getId())).toList();
    }

    /**
     * Determines the default number of cards for a practice session.
     *
     * <p>Calculates the appropriate card count based on user configuration
     * and available unknown cards. The result is clamped between 1 and
     * the configured default count to ensure a meaningful practice session.</p>
     *
     * @param deckId the ID of the deck to calculate count for
     * @return the number of cards to include in the practice session
     */
    public int resolveDefaultCount(long deckId) {
        int configured = practiceSettingsService.getDefaultCount();
        int notKnown = getNotKnownCards(deckId).size();
        return Math.clamp(notKnown, 1, configured);
    }

    public boolean isRandom() {
        return practiceSettingsService.isDefaultRandomOrder();
    }

    public PracticeDirection defaultDirection() {
        return Optional.ofNullable(practiceSettingsService.getDefaultDirection())
                .orElse(PracticeDirection.FRONT_TO_BACK);
    }

    /**
     * Prepares a practice session with the specified number of cards.
     *
     * <p>Creates a practice session by filtering unknown cards and optionally
     * randomizing their order. The session size is limited to the available
     * unknown cards or the requested count, whichever is smaller.</p>
     *
     * @param deckId the ID of the deck to practice
     * @param count the desired number of cards in the session
     * @param random whether to randomize the card order
     * @return a list of flashcards prepared for the practice session
     */
    public List<Flashcard> prepareSession(long deckId, int count, boolean random) {
        // Filter to only unknown cards for focused practice
        List<Flashcard> filtered = new ArrayList<>(getNotKnownCards(deckId));
        if (filtered.isEmpty()) return filtered;

        // Randomize card order if requested for varied practice experience
        if (random) Collections.shuffle(filtered);

        // Limit session size to requested count or available cards
        if (count < filtered.size()) return new ArrayList<>(filtered.subList(0, count));
        return filtered;
    }

    /**
     * Records a completed practice session.
     *
     * <p>Persists session statistics including performance metrics and any
     * changes to card knowledge status. This method delegates to the
     * statistics service for data persistence.</p>
     *
     * @param deckId the ID of the deck that was practiced
     * @param totalViewed the total number of cards viewed in the session
     * @param correct the number of cards answered correctly
     * @param hard the number of cards marked as difficult
     * @param sessionDuration the total duration of the practice session
     * @param totalAnswerDelayMs the total time spent thinking before answering
     * @param knownCardIdsDelta the collection of card IDs that changed knowledge status
     */
    public void recordSession(
            long deckId,
            int totalViewed,
            int correct,
            int hard,
            Duration sessionDuration,
            long totalAnswerDelayMs,
            List<Long> knownCardIdsDelta) {
        statsService.recordSession(
                deckId, totalViewed, correct, 0, hard, sessionDuration, totalAnswerDelayMs, knownCardIdsDelta);
    }

    /**
     * Represents an active practice session with state tracking.
     *
     * <p>This class maintains the complete state of a practice session including
     * current position, performance metrics, timing information, and progress
     * tracking. It provides methods for navigating through cards and recording
     * user responses.</p>
     *
     * <p>The session tracks both individual card performance and overall session
     * statistics, enabling comprehensive progress monitoring and analytics.</p>
     */
    public static class Session {
        private final long deckId;
        private final List<Flashcard> cards;
        private int index;
        private boolean showingAnswer;
        private PracticeDirection direction;
        private int correctCount;
        private int hardCount;
        private int totalViewed;
        private final Instant sessionStart;
        private Instant cardShowTime;
        private long totalAnswerDelayMs;
        private final List<Long> knownCardIdsDelta = new ArrayList<>();
        private final List<Long> failedCardIds = new ArrayList<>();

        /**
         * Constructs a new practice session.
         *
         * <p>Initializes a practice session with the specified deck and direction.
         * All counters and timers are reset to initial values, and the session
         * is ready to begin with the first card.</p>
         *
         * @param deckId the ID of the deck being practiced
         * @param cards the list of flashcards for this session
         * @param direction the practice direction (front-to-back or back-to-front)
         */
        public Session(long deckId, List<Flashcard> cards, PracticeDirection direction) {
            this.deckId = deckId;
            this.cards = cards;
            this.direction = direction;
            this.index = 0;
            this.showingAnswer = false;
            this.correctCount = 0;
            this.hardCount = 0;
            this.totalViewed = 0;
            this.totalAnswerDelayMs = 0L;
            this.sessionStart = Instant.now();
        }

        public long getDeckId() {
            return deckId;
        }

        public List<Flashcard> getCards() {
            return cards;
        }

        public int getIndex() {
            return index;
        }

        public boolean isShowingAnswer() {
            return showingAnswer;
        }

        public PracticeDirection getDirection() {
            return direction;
        }

        public int getCorrectCount() {
            return correctCount;
        }

        public int getHardCount() {
            return hardCount;
        }

        public int getTotalViewed() {
            return totalViewed;
        }

        public Instant getSessionStart() {
            return sessionStart;
        }

        public Instant getCardShowTime() {
            return cardShowTime;
        }

        public long getTotalAnswerDelayMs() {
            return totalAnswerDelayMs;
        }

        public List<Long> getKnownCardIdsDelta() {
            return knownCardIdsDelta;
        }

        public List<Long> getFailedCardIds() {
            return failedCardIds;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setShowingAnswer(boolean showingAnswer) {
            this.showingAnswer = showingAnswer;
        }

        public void setDirection(PracticeDirection direction) {
            this.direction = direction;
        }

        public void setCorrectCount(int correctCount) {
            this.correctCount = correctCount;
        }

        public void setHardCount(int hardCount) {
            this.hardCount = hardCount;
        }

        public void setTotalViewed(int totalViewed) {
            this.totalViewed = totalViewed;
        }

        public void setCardShowTime(Instant cardShowTime) {
            this.cardShowTime = cardShowTime;
        }

        public void setTotalAnswerDelayMs(long totalAnswerDelayMs) {
            this.totalAnswerDelayMs = totalAnswerDelayMs;
        }
    }

    /**
     * Starts a new practice session.
     *
     * <p>Creates and initializes a new practice session with the specified
     * parameters. The session is prepared with appropriate cards and ready
     * to begin practice immediately.</p>
     *
     * @param deckId the ID of the deck to practice
     * @param count the number of cards to include in the session
     * @param random whether to randomize the card order
     * @param direction the practice direction for the session
     * @return a new Session instance ready for practice
     */
    public Session startSession(long deckId, int count, boolean random, PracticeDirection direction) {
        List<Flashcard> cards = prepareSession(deckId, count, random);
        return new Session(deckId, cards, direction);
    }

    /**
     * Checks if a practice session is complete.
     *
     * <p>A session is considered complete when there are no cards available
     * or when all cards have been processed. This method handles edge cases
     * where cards might be null or empty.</p>
     *
     * @param s the session to check
     * @return true if the session is complete, false otherwise
     */
    public boolean isComplete(Session s) {
        return s.getCards() == null
                || s.getCards().isEmpty()
                || s.getIndex() >= s.getCards().size();
    }

    /**
     * Retrieves the current card in the practice session.
     *
     * <p>Returns the flashcard at the current index position. If the session
     * is complete, returns null to indicate no more cards are available.</p>
     *
     * @param s the session to get the current card from
     * @return the current flashcard, or null if session is complete
     */
    public Flashcard currentCard(Session s) {
        if (isComplete(s)) return null;
        return s.getCards().get(s.getIndex());
    }

    public void startQuestion(Session s) {
        s.setShowingAnswer(false);
        s.setCardShowTime(Instant.now());
    }

    /**
     * Reveals the answer for the current card.
     *
     * <p>Calculates and records the time spent thinking about the current card
     * before revealing the answer. This timing information is used for
     * performance analytics and difficulty assessment.</p>
     *
     * @param s the session containing the current card
     */
    public void reveal(Session s) {
        if (s.getCardShowTime() != null) {
            // Calculate time spent thinking about this card
            long delay = Duration.between(s.getCardShowTime(), Instant.now()).toMillis();
            // Ensure delay is non-negative (handle edge cases with system clock)
            long clampedDelay = Math.clamp(delay, 0L, Long.MAX_VALUE);
            s.setTotalAnswerDelayMs(s.getTotalAnswerDelayMs() + clampedDelay);
        }
        s.setShowingAnswer(true);
    }

    /**
     * Marks the current card as known and advances to the next card.
     *
     * <p>Records that the user successfully answered the current card correctly.
     * Updates session statistics and moves to the next card in the sequence.</p>
     *
     * @param s the session to update
     */
    public void markKnow(Session s) {
        if (isComplete(s)) return;
        // Increment viewed count and correct answers
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setCorrectCount(s.getCorrectCount() + 1);
        // Track this card as newly learned
        s.getKnownCardIdsDelta().add(currentCard(s).getId());
        // Move to next card and reset answer display
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
    }

    /**
     * Marks the current card as difficult and advances to the next card.
     *
     * <p>Records that the user found the current card challenging. Updates
     * session statistics and moves to the next card in the sequence.</p>
     *
     * @param s the session to update
     */
    public void markHard(Session s) {
        if (isComplete(s)) return;
        // Increment viewed count and difficult cards
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setHardCount(s.getHardCount() + 1);
        // Track this card as failed for review purposes
        s.getFailedCardIds().add(currentCard(s).getId());
        // Move to next card and reset answer display
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
    }

    /**
     * Progress information for a practice session.
     *
     * <p>This record contains comprehensive progress metrics including current
     * position, total cards, performance statistics, and completion percentage.
     * It provides a snapshot of session progress for UI updates and analytics.</p>
     *
     * @param current the current card position (1-based index)
     * @param total the total number of cards in the session
     * @param totalViewed the number of cards processed so far
     * @param correct the number of cards answered correctly
     * @param hard the number of cards marked as difficult
     * @param percent the completion percentage (0-100)
     */
    public record Progress(long current, int total, int totalViewed, int correct, int hard, long percent) {}

    /**
     * Calculates current progress information for a practice session.
     *
     * <p>Computes comprehensive progress metrics including current position,
     * completion percentage, and performance statistics. The method handles
     * edge cases where cards might be null or empty.</p>
     *
     * @param s the session to calculate progress for
     * @return a Progress record with current session metrics
     */
    public Progress progress(Session s) {
        // Get total cards count, handling null/empty cases
        int total = s.getCards() != null ? s.getCards().size() : 0;
        // Calculate current position (1-based) with bounds checking
        long current = Math.clamp(s.getIndex() + 1L, 1L, total);
        // Calculate completion percentage, avoiding division by zero
        long percent = total > 0 ? Math.round((current * 100.0) / total) : 0;
        return new Progress(current, total, s.getTotalViewed(), s.getCorrectCount(), s.getHardCount(), percent);
    }

    /**
     * Records and persists a completed practice session.
     *
     * <p>Calculates the total session duration and delegates to the recordSession
     * method to persist all session statistics and performance metrics.</p>
     *
     * @param s the completed session to record
     */
    public void recordAndPersist(Session s) {
        // Calculate total session duration from start to completion
        Duration duration = Duration.between(s.getSessionStart(), Instant.now());
        recordSession(
                s.getDeckId(),
                s.getTotalViewed(),
                s.getCorrectCount(),
                s.getHardCount(),
                duration,
                s.getTotalAnswerDelayMs(),
                s.getKnownCardIdsDelta());
    }
}
