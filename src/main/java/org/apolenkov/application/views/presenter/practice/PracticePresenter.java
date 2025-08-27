package org.apolenkov.application.views.presenter.practice;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
     * @param deckUseCase service for deck operations (non-null)
     * @param flashcardUseCase service for flashcard operations (non-null)
     * @param statsService service for statistics recording (non-null)
     * @param practiceSettingsService service for practice configuration (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public PracticePresenter(
            final DeckUseCase deckUseCase,
            final FlashcardUseCase flashcardUseCase,
            final StatsService statsService,
            final PracticeSettingsService practiceSettingsService) {

        if (deckUseCase == null) {
            throw new IllegalArgumentException("DeckUseCase cannot be null");
        }
        if (flashcardUseCase == null) {
            throw new IllegalArgumentException("FlashcardUseCase cannot be null");
        }
        if (statsService == null) {
            throw new IllegalArgumentException("StatsService cannot be null");
        }
        if (practiceSettingsService == null) {
            throw new IllegalArgumentException("PracticeSettingsService cannot be null");
        }

        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
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
         * @param deckId the ID of the deck being practiced
         * @param cards the list of flashcards for this session
         * @param direction the practice direction (front-to-back or back-to-front)
         */
        public Session(final long deckId, final List<Flashcard> cards, final PracticeDirection direction) {
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

        public void setIndex(final int index) {
            this.index = index;
        }

        public void setShowingAnswer(final boolean showingAnswer) {
            this.showingAnswer = showingAnswer;
        }

        public void setDirection(final PracticeDirection direction) {
            this.direction = direction;
        }

        public void setCorrectCount(final int correctCount) {
            this.correctCount = correctCount;
        }

        public void setHardCount(final int hardCount) {
            this.hardCount = hardCount;
        }

        public void setTotalViewed(final int totalViewed) {
            this.totalViewed = totalViewed;
        }

        public void setCardShowTime(final Instant cardShowTime) {
            this.cardShowTime = cardShowTime;
        }

        public void setTotalAnswerDelayMs(final long totalAnswerDelayMs) {
            this.totalAnswerDelayMs = totalAnswerDelayMs;
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
