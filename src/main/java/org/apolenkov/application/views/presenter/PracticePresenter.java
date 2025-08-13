package org.apolenkov.application.views.presenter;

import java.time.Duration;
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
        deckId,
        totalViewed,
        correct,
        0,
        hard,
        sessionDuration,
        totalAnswerDelayMs,
        knownCardIdsDelta);
  }
}
