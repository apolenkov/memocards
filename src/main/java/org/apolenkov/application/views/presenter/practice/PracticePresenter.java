package org.apolenkov.application.views.presenter.practice;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
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
 */
@Component
public final class PracticePresenter {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;

    /**
     * Creates a new PracticePresenter with required dependencies.
     *
     * @param useCase service for deck operations (non-null)
     * @param flashcardUseCaseValue service for flashcard operations (non-null)
     * @param stats service for statistics recording (non-null)
     * @param practiceSettings service for practice configuration (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public PracticePresenter(
            final DeckUseCase useCase,
            final FlashcardUseCase flashcardUseCaseValue,
            final StatsService stats,
            final PracticeSettingsService practiceSettings) {

        if (useCase == null) {
            throw new IllegalArgumentException("DeckUseCase cannot be null");
        }
        if (flashcardUseCaseValue == null) {
            throw new IllegalArgumentException("FlashcardUseCase cannot be null");
        }
        if (stats == null) {
            throw new IllegalArgumentException("StatsService cannot be null");
        }
        if (practiceSettings == null) {
            throw new IllegalArgumentException("PracticeSettingsService cannot be null");
        }

        this.deckUseCase = useCase;
        this.flashcardUseCase = flashcardUseCaseValue;
        this.statsService = stats;
        this.practiceSettingsService = practiceSettings;
    }

    /**
     * Loads a deck by its ID.
     *
     * @param deckId the ID of the deck to load (must be positive)
     * @return an Optional containing the deck if found, empty otherwise, never null
     * @throws IllegalArgumentException if deckId is not positive
     */
    public Optional<Deck> loadDeck(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        return deckUseCase.getDeckById(deckId);
    }

    /**
     * Gets cards that are not yet marked as known in a deck.
     *
     * @param deckId the ID of the deck to check (must be positive)
     * @return a list of flashcards not yet known by the user, never null (maybe empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public List<Flashcard> getNotKnownCards(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        List<Flashcard> all = flashcardUseCase.getFlashcardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return all.stream().filter(fc -> !known.contains(fc.getId())).toList();
    }

    /**
     * Determines the default number of cards for a practice session.
     *
     * @param deckId the ID of the deck to calculate count for (must be positive)
     * @return the number of cards to include in the practice session (1 to configured default)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public int resolveDefaultCount(final long deckId) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        int configured = practiceSettingsService.getDefaultCount();
        int notKnown = getNotKnownCards(deckId).size();
        return Math.clamp(notKnown, 1, configured);
    }

    /**
     * Determines if practice sessions should use random card order.
     *
     * @return true if random ordering is enabled, false for sequential ordering
     */
    public boolean isRandom() {
        return practiceSettingsService.isDefaultRandomOrder();
    }

    /**
     * Gets the default practice direction for sessions.
     *
     * @return the default practice direction, never null
     */
    public PracticeDirection defaultDirection() {
        return Optional.ofNullable(practiceSettingsService.getDefaultDirection())
                .orElse(PracticeDirection.FRONT_TO_BACK);
    }

    /**
     * Prepares a practice session with the specified number of cards.
     * Creates a practice session by filtering unknown cards and optionally
     * randomizing their order, limited to available unknown cards or requested count.
     *
     * @param deckId the ID of the deck to practice
     * @param count the desired number of cards in the session
     * @param random whether to randomize the card order
     * @return a list of flashcards prepared for the practice session
     */
    public List<Flashcard> prepareSession(final long deckId, final int count, final boolean random) {
        // Filter to only unknown cards for focused practice
        List<Flashcard> filtered = new ArrayList<>(getNotKnownCards(deckId));
        if (filtered.isEmpty()) {
            return filtered;
        }

        // Randomize card order if requested for varied practice experience
        if (random) {
            Collections.shuffle(filtered);
        }

        // Limit session size to requested count or available cards
        if (count < filtered.size()) {
            return new ArrayList<>(filtered.subList(0, count));
        }
        return filtered;
    }

    /**
     * Records completed practice session with performance metrics and knowledge status changes.
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
            final long deckId,
            final int totalViewed,
            final int correct,
            final int hard,
            final Duration sessionDuration,
            final long totalAnswerDelayMs,
            final List<Long> knownCardIdsDelta) {
        SessionStatsDto sessionData = SessionStatsDto.builder()
                .deckId(deckId)
                .viewed(totalViewed)
                .correct(correct)
                .repeat(0)
                .hard(hard)
                .sessionDurationMs(sessionDuration.toMillis())
                .totalAnswerDelayMs(totalAnswerDelayMs)
                .knownCardIdsDelta(knownCardIdsDelta)
                .build();
        statsService.recordSession(sessionData);
    }

    /**
     * Represents active practice session with state tracking, performance metrics, and progress monitoring.
     */
    public static final class Session {
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
         * Constructs new practice session with deck, direction, and reset counters.
         *
         * @param deckIdValue the ID of the deck being practiced
         * @param cardsValue the list of flashcards for this session
         * @param directionValue the practice direction (front-to-back or back-to-front)
         */
        public Session(
                final long deckIdValue, final List<Flashcard> cardsValue, final PracticeDirection directionValue) {
            this.deckId = deckIdValue;
            this.cards = cardsValue;
            this.direction = directionValue;
            this.index = 0;
            this.showingAnswer = false;
            this.correctCount = 0;
            this.hardCount = 0;
            this.totalViewed = 0;
            this.totalAnswerDelayMs = 0L;
            this.sessionStart = Instant.now();
        }

        /**
         * Gets the deck ID for this practice session.
         *
         * @return the deck ID
         */
        public long getDeckId() {
            return deckId;
        }

        /**
         * Gets the list of flashcards for this practice session.
         *
         * @return the list of flashcards
         */
        public List<Flashcard> getCards() {
            return cards;
        }

        /**
         * Gets the current card index in the practice session.
         *
         * @return the current card index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Checks if the answer is currently being shown.
         *
         * @return true if answer is shown, false otherwise
         */
        public boolean isShowingAnswer() {
            return showingAnswer;
        }

        /**
         * Gets the practice direction for this session.
         *
         * @return the practice direction
         */
        public PracticeDirection getDirection() {
            return direction;
        }

        /**
         * Gets the count of correct answers in this session.
         *
         * @return the correct answer count
         */
        public int getCorrectCount() {
            return correctCount;
        }

        /**
         * Gets the count of hard cards in this session.
         *
         * @return the hard card count
         */
        public int getHardCount() {
            return hardCount;
        }

        /**
         * Gets the total number of cards viewed in this session.
         *
         * @return the total viewed count
         */
        public int getTotalViewed() {
            return totalViewed;
        }

        /**
         * Gets the session start timestamp.
         *
         * @return the session start time
         */
        public Instant getSessionStart() {
            return sessionStart;
        }

        /**
         * Gets the timestamp when the current card was shown.
         *
         * @return the card show time
         */
        public Instant getCardShowTime() {
            return cardShowTime;
        }

        /**
         * Gets the total answer delay in milliseconds for this session.
         *
         * @return the total answer delay in milliseconds
         */
        public long getTotalAnswerDelayMs() {
            return totalAnswerDelayMs;
        }

        /**
         * Gets the list of card IDs that became known during this session.
         *
         * @return the list of newly known card IDs
         */
        public List<Long> getKnownCardIdsDelta() {
            return knownCardIdsDelta;
        }

        /**
         * Gets the list of card IDs that failed during this session.
         *
         * @return the list of failed card IDs
         */
        public List<Long> getFailedCardIds() {
            return failedCardIds;
        }

        /**
         * Sets the current card index in the practice session.
         *
         * @param indexValue the new card index
         */
        public void setIndex(final int indexValue) {
            this.index = indexValue;
        }

        /**
         * Sets whether the answer is currently being shown.
         *
         * @param showingAnswerValue true to show answer, false to hide
         */
        public void setShowingAnswer(final boolean showingAnswerValue) {
            this.showingAnswer = showingAnswerValue;
        }

        /**
         * Sets the practice direction for this session.
         *
         * @param directionValue the new practice direction
         */
        public void setDirection(final PracticeDirection directionValue) {
            this.direction = directionValue;
        }

        /**
         * Sets the count of correct answers in this session.
         *
         * @param correctCountValue the new correct answer count
         */
        public void setCorrectCount(final int correctCountValue) {
            this.correctCount = correctCountValue;
        }

        /**
         * Sets the count of hard cards in this session.
         *
         * @param hardCountValue the new hard card count
         */
        public void setHardCount(final int hardCountValue) {
            this.hardCount = hardCountValue;
        }

        /**
         * Sets the total number of cards viewed in this session.
         *
         * @param totalViewedValue the new total viewed count
         */
        public void setTotalViewed(final int totalViewedValue) {
            this.totalViewed = totalViewedValue;
        }

        /**
         * Sets the timestamp when the current card was shown.
         *
         * @param cardShowTimeValue the new card show time
         */
        public void setCardShowTime(final Instant cardShowTimeValue) {
            this.cardShowTime = cardShowTimeValue;
        }

        /**
         * Sets the total answer delay in milliseconds for this session.
         *
         * @param totalAnswerDelayMsValue the new total answer delay
         */
        public void setTotalAnswerDelayMs(final long totalAnswerDelayMsValue) {
            this.totalAnswerDelayMs = totalAnswerDelayMsValue;
        }
    }

    /**
     * Starts a new practice session.
     * Creates and initializes a new practice session with the specified parameters.
     *
     * @param deckId the ID of the deck to practice
     * @param count the number of cards to include in the session
     * @param random whether to randomize the card order
     * @param direction the practice direction for the session
     * @return a new Session instance ready for practice
     */
    public Session startSession(
            final long deckId, final int count, final boolean random, final PracticeDirection direction) {
        List<Flashcard> cards = prepareSession(deckId, count, random);
        return new Session(deckId, cards, direction);
    }

    /**
     * Checks if a practice session is complete.
     *
     * @param s the session to check
     * @return true if the session is complete, false otherwise
     */
    public boolean isComplete(final Session s) {
        return s.getCards() == null
                || s.getCards().isEmpty()
                || s.getIndex() >= s.getCards().size();
    }

    /**
     * Retrieves the current card in the practice session.
     *
     * @param s the session to get the current card from
     * @return the current flashcard, or null if session is complete
     */
    public Flashcard currentCard(final Session s) {
        if (isComplete(s)) {
            return null;
        }
        return s.getCards().get(s.getIndex());
    }

    /**
     * Starts a new question in the practice session.
     * Resets the answer display and records the start time for timing calculations.
     *
     * @param s the session to start the question for
     */
    public void startQuestion(final Session s) {
        s.setShowingAnswer(false);
        s.setCardShowTime(Instant.now());
    }

    /**
     * Reveals the answer for the current card.
     * Calculates and records the time spent thinking about the current card
     * before revealing the answer. This timing information is used for
     * performance analytics and difficulty assessment.
     *
     * @param s the session containing the current card
     */
    public void reveal(final Session s) {
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
     * Records that the user successfully answered the current card correctly.
     * Updates session statistics and moves to the next card in the sequence.
     *
     * @param s the session to update
     */
    public void markKnow(final Session s) {
        if (isComplete(s)) {
            return;
        }

        // Increment viewed count and correct answers
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setCorrectCount(s.getCorrectCount() + 1);

        // Track this card as newly learned
        s.getKnownCardIdsDelta().add(Objects.requireNonNull(currentCard(s)).getId());

        // Move to next card and reset answer display
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
    }

    /**
     * Marks the current card as difficult and advances to the next card.
     * Records that the user found the current card challenging. Updates
     * session statistics and moves to the next card in the sequence.
     *
     * @param s the session to update
     */
    public void markHard(final Session s) {
        if (isComplete(s)) {
            return;
        }

        // Increment viewed count and difficult cards
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setHardCount(s.getHardCount() + 1);

        // Track this card as failed for review purposes
        s.getFailedCardIds().add(Objects.requireNonNull(currentCard(s)).getId());

        // Move to next card and reset answer display
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
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

    /**
     * Calculates current progress information for a practice session.
     * Computes comprehensive progress metrics including current position, completion percentage, and performance statistics.
     *
     * @param s the session to calculate progress for
     * @return a Progress record with current session metrics
     */
    public Progress progress(final Session s) {
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
     * Calculates the total session duration and delegates to the recordSession
     * method to persist all session statistics and performance metrics.
     *
     * @param s the completed session to record
     */
    public void recordAndPersist(final Session s) {
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
