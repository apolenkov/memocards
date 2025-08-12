package org.apolenkov.application.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatsService {

    public static class DailyStats {
        public LocalDate date;
        public int sessions;
        public int viewed;
        public int correct;
        public int repeat;
        public int hard;
        public long totalDurationMs;
        public long totalAnswerDelayMs;

        public DailyStats(LocalDate date) {
            this.date = date;
        }

        public double getAvgDelayMs() {
            return viewed > 0 ? (double) totalAnswerDelayMs / viewed : 0.0;
        }
    }

    private final Map<Long, Map<LocalDate, DailyStats>> deckIdToDailyStats = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> deckIdToKnownCardIds = new ConcurrentHashMap<>();

    public void recordSession(long deckId,
                              int viewed,
                              int correct,
                              int repeat,
                              int hard,
                              Duration sessionDuration,
                              long totalAnswerDelayMs,
                              Collection<Long> knownCardIdsDelta) {
        if (viewed <= 0) return;
        LocalDate today = LocalDate.now();
        Map<LocalDate, DailyStats> byDate = deckIdToDailyStats.computeIfAbsent(deckId, k -> new ConcurrentHashMap<>());
        DailyStats stats = byDate.computeIfAbsent(today, DailyStats::new);
        stats.sessions += 1;
        stats.viewed += viewed;
        stats.correct += correct;
        stats.repeat += repeat;
        stats.hard += hard;
        stats.totalDurationMs += sessionDuration.toMillis();
        stats.totalAnswerDelayMs += totalAnswerDelayMs;

        if (knownCardIdsDelta != null && !knownCardIdsDelta.isEmpty()) {
            deckIdToKnownCardIds.computeIfAbsent(deckId, k -> Collections.synchronizedSet(new HashSet<>())).addAll(knownCardIdsDelta);
        }
    }

    public List<DailyStats> getDailyStatsForDeck(long deckId) {
        return deckIdToDailyStats.getOrDefault(deckId, Collections.emptyMap())
                .values().stream()
                .sorted(Comparator.comparing(ds -> ds.date))
                .toList();
    }

    public int getDeckProgressPercent(long deckId, int deckSize) {
        if (deckSize <= 0) return 0;
        int known = deckIdToKnownCardIds.getOrDefault(deckId, Collections.emptySet()).size();
        int percent = (int) Math.round(100.0 * known / deckSize);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    public boolean isCardKnown(long deckId, long cardId) {
        return deckIdToKnownCardIds.getOrDefault(deckId, Collections.emptySet()).contains(cardId);
    }

    public Set<Long> getKnownCardIds(long deckId) {
        return Collections.unmodifiableSet(deckIdToKnownCardIds.getOrDefault(deckId, Collections.emptySet()));
    }

    public void setCardKnown(long deckId, long cardId, boolean known) {
        deckIdToKnownCardIds.computeIfAbsent(deckId, k -> Collections.synchronizedSet(new HashSet<>()));
        if (known) {
            deckIdToKnownCardIds.get(deckId).add(cardId);
        } else {
            deckIdToKnownCardIds.get(deckId).remove(cardId);
        }
    }

    public void resetDeckProgress(long deckId) {
        deckIdToKnownCardIds.remove(deckId);
    }
}
