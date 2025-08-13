package org.apolenkov.application.infrastructure.repository.memory;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apolenkov.application.domain.port.StatsRepository;
import org.springframework.stereotype.Repository;

@org.springframework.context.annotation.Profile("memory")
@Repository
public class InMemoryStatsRepository implements StatsRepository {

  private final Map<Long, Map<LocalDate, DailyStatsRecord>> deckIdToDaily =
      new ConcurrentHashMap<>();
  private final Map<Long, Set<Long>> deckIdToKnown = new ConcurrentHashMap<>();

  @Override
  public void appendSession(
      long deckId,
      LocalDate date,
      int viewed,
      int correct,
      int repeat,
      int hard,
      long sessionDurationMs,
      long totalAnswerDelayMs,
      Collection<Long> knownCardIdsDelta) {
    if (viewed <= 0) return;
    Map<LocalDate, DailyStatsRecord> byDate =
        deckIdToDaily.computeIfAbsent(deckId, k -> new ConcurrentHashMap<>());
    byDate.compute(
        date,
        (d, existing) -> {
          if (existing == null) {
            return new DailyStatsRecord(
                date, 1, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);
          }
          return new DailyStatsRecord(
              date,
              existing.sessions() + 1,
              existing.viewed() + viewed,
              existing.correct() + correct,
              existing.repeat() + repeat,
              existing.hard() + hard,
              existing.totalDurationMs() + sessionDurationMs,
              existing.totalAnswerDelayMs() + totalAnswerDelayMs);
        });

    if (knownCardIdsDelta != null && !knownCardIdsDelta.isEmpty()) {
      deckIdToKnown
          .computeIfAbsent(deckId, k -> Collections.synchronizedSet(new HashSet<>()))
          .addAll(knownCardIdsDelta);
    }
  }

  @Override
  public List<DailyStatsRecord> getDailyStats(long deckId) {
    return deckIdToDaily.getOrDefault(deckId, Collections.emptyMap()).values().stream()
        .sorted(Comparator.comparing(DailyStatsRecord::date))
        .toList();
  }

  @Override
  public Set<Long> getKnownCardIds(long deckId) {
    return Collections.unmodifiableSet(deckIdToKnown.getOrDefault(deckId, Collections.emptySet()));
  }

  @Override
  public void setCardKnown(long deckId, long cardId, boolean known) {
    deckIdToKnown.computeIfAbsent(deckId, k -> Collections.synchronizedSet(new HashSet<>()));
    if (known) {
      deckIdToKnown.get(deckId).add(cardId);
    } else {
      deckIdToKnown.get(deckId).remove(cardId);
    }
  }

  @Override
  public void resetDeckProgress(long deckId) {
    deckIdToKnown.remove(deckId);
  }
}
