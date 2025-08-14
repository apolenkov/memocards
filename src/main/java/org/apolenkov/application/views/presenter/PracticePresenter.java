package org.apolenkov.application.views.presenter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
        return all.stream().filter(fc -> !known.contains(fc.getId())).collect(Collectors.toList());
    }

    public int resolveDefaultCount(long deckId) {
        int configured = practiceSettingsService.getDefaultCount();
        int notKnown = getNotKnownCards(deckId).size();
        return Math.max(1, Math.min(configured, notKnown));
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
        public final long deckId;
        public final List<Flashcard> cards;
        public int index;
        public boolean showingAnswer;
        public PracticeDirection direction;
        public int correctCount;
        public int hardCount;
        public int totalViewed;
        public Instant sessionStart;
        public Instant cardShowTime;
        public long totalAnswerDelayMs;
        public final List<Long> knownCardIdsDelta = new ArrayList<>();
        public final List<Long> failedCardIds = new ArrayList<>();

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
    }

    public Session startSession(long deckId, int count, boolean random, PracticeDirection direction) {
        List<Flashcard> cards = prepareSession(deckId, count, random);
        return new Session(deckId, cards, direction);
    }

    public boolean isComplete(Session s) {
        return s.cards == null || s.cards.isEmpty() || s.index >= s.cards.size();
    }

    public Flashcard currentCard(Session s) {
        if (isComplete(s)) return null;
        return s.cards.get(s.index);
    }

    public void startQuestion(Session s) {
        s.showingAnswer = false;
        s.cardShowTime = Instant.now();
    }

    public void reveal(Session s) {
        if (s.cardShowTime != null) {
            long delay = Duration.between(s.cardShowTime, Instant.now()).toMillis();
            s.totalAnswerDelayMs += Math.max(delay, 0);
        }
        s.showingAnswer = true;
    }

    public void markKnow(Session s) {
        if (isComplete(s)) return;
        s.totalViewed++;
        s.correctCount++;
        s.knownCardIdsDelta.add(currentCard(s).getId());
        s.index++;
        s.showingAnswer = false;
    }

    public void markHard(Session s) {
        if (isComplete(s)) return;
        s.totalViewed++;
        s.hardCount++;
        s.failedCardIds.add(currentCard(s).getId());
        s.index++;
        s.showingAnswer = false;
    }

    public record Progress(int current, int total, int totalViewed, int correct, int hard, int percent) {}

    public Progress progress(Session s) {
        int total = s.cards != null ? s.cards.size() : 0;
        int current = Math.min(s.index + 1, Math.max(1, total));
        int percent = total > 0 ? (int) Math.round((current * 100.0) / total) : 0;
        return new Progress(current, total, s.totalViewed, s.correctCount, s.hardCount, percent);
    }

    public void recordAndPersist(Session s) {
        Duration duration = Duration.between(s.sessionStart, Instant.now());
        recordSession(
                s.deckId,
                s.totalViewed,
                s.correctCount,
                s.hardCount,
                duration,
                s.totalAnswerDelayMs,
                s.knownCardIdsDelta);
    }
}
