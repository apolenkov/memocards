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

@Component
public class PracticePresenter {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;

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

    public Optional<Deck> loadDeck(long deckId) {
        return deckUseCase.getDeckById(deckId);
    }

    public List<Flashcard> getNotKnownCards(long deckId) {
        List<Flashcard> all = flashcardUseCase.getFlashcardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return all.stream().filter(fc -> !known.contains(fc.getId())).toList();
    }

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

    public List<Flashcard> prepareSession(long deckId, int count, boolean random) {
        List<Flashcard> filtered = new ArrayList<>(getNotKnownCards(deckId));
        if (filtered.isEmpty()) return filtered;
        if (random) Collections.shuffle(filtered);
        if (count < filtered.size()) return new ArrayList<>(filtered.subList(0, count));
        return filtered;
    }

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

    // Session logic encapsulation
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

        // Getters
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

        // Setters
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

    public Session startSession(long deckId, int count, boolean random, PracticeDirection direction) {
        List<Flashcard> cards = prepareSession(deckId, count, random);
        return new Session(deckId, cards, direction);
    }

    public boolean isComplete(Session s) {
        return s.getCards() == null
                || s.getCards().isEmpty()
                || s.getIndex() >= s.getCards().size();
    }

    public Flashcard currentCard(Session s) {
        if (isComplete(s)) return null;
        return s.getCards().get(s.getIndex());
    }

    public void startQuestion(Session s) {
        s.setShowingAnswer(false);
        s.setCardShowTime(Instant.now());
    }

    public void reveal(Session s) {
        if (s.getCardShowTime() != null) {
            long delay = Duration.between(s.getCardShowTime(), Instant.now()).toMillis();
            long clampedDelay = Math.clamp(delay, 0L, Long.MAX_VALUE);
            s.setTotalAnswerDelayMs(s.getTotalAnswerDelayMs() + clampedDelay);
        }
        s.setShowingAnswer(true);
    }

    public void markKnow(Session s) {
        if (isComplete(s)) return;
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setCorrectCount(s.getCorrectCount() + 1);
        s.getKnownCardIdsDelta().add(currentCard(s).getId());
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
    }

    public void markHard(Session s) {
        if (isComplete(s)) return;
        s.setTotalViewed(s.getTotalViewed() + 1);
        s.setHardCount(s.getHardCount() + 1);
        s.getFailedCardIds().add(currentCard(s).getId());
        s.setIndex(s.getIndex() + 1);
        s.setShowingAnswer(false);
    }

    public record Progress(long current, int total, int totalViewed, int correct, int hard, long percent) {}

    public Progress progress(Session s) {
        int total = s.getCards() != null ? s.getCards().size() : 0;
        long current = Math.clamp(s.getIndex() + 1L, 1L, total);
        long percent = total > 0 ? Math.round((current * 100.0) / total) : 0;
        return new Progress(current, total, s.getTotalViewed(), s.getCorrectCount(), s.getHardCount(), percent);
    }

    public void recordAndPersist(Session s) {
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
