package org.apolenkov.application.views.presenter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeckPresenter {

  private final DeckUseCase deckUseCase;
  private final FlashcardUseCase flashcardUseCase;
  private final StatsService statsService;

  public DeckPresenter(
      DeckUseCase deckUseCase, FlashcardUseCase flashcardUseCase, StatsService statsService) {
    this.deckUseCase = deckUseCase;
    this.flashcardUseCase = flashcardUseCase;
    this.statsService = statsService;
  }

  public Optional<Deck> loadDeck(long deckId) {
    return deckUseCase.getDeckById(deckId);
  }

  public List<Flashcard> loadFlashcards(long deckId) {
    return flashcardUseCase.getFlashcardsByDeckId(deckId);
  }

  public List<Flashcard> filterFlashcards(
      List<Flashcard> base, String query, Set<Long> knownIds, boolean hideKnown) {
    String q = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
    return base.stream()
        .filter(
            fc ->
                q.isEmpty()
                    || (fc.getFrontText() != null
                        && fc.getFrontText().toLowerCase(Locale.ROOT).contains(q))
                    || (fc.getBackText() != null
                        && fc.getBackText().toLowerCase(Locale.ROOT).contains(q))
                    || (fc.getExample() != null
                        && fc.getExample().toLowerCase(Locale.ROOT).contains(q)))
        .filter(fc -> !hideKnown || !knownIds.contains(fc.getId()))
        .collect(Collectors.toList());
  }

  public Set<Long> getKnown(long deckId) {
    return statsService.getKnownCardIds(deckId);
  }

  public boolean isKnown(long deckId, long cardId) {
    return statsService.isCardKnown(deckId, cardId);
  }

  public void toggleKnown(long deckId, long cardId) {
    boolean known = statsService.isCardKnown(deckId, cardId);
    statsService.setCardKnown(deckId, cardId, !known);
  }

  public void resetProgress(long deckId) {
    statsService.resetDeckProgress(deckId);
  }

  public int deckSize(long deckId) {
    return loadFlashcards(deckId).size();
  }

  public int knownCount(long deckId) {
    return getKnown(deckId).size();
  }

  public int progressPercent(long deckId) {
    int size = deckSize(deckId);
    return statsService.getDeckProgressPercent(deckId, size);
  }
}
