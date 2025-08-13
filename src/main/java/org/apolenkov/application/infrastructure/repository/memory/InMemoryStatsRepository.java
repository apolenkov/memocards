package org.apolenkov.application.infrastructure.repository.memory;

import org.apolenkov.application.domain.port.StatsRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStatsRepository implements StatsRepository {

    private final Map<Long, Map<LocalDate, DailyStatsRecord>> deckIdToDaily = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> deckIdToKnown = new ConcurrentHashMap<>();

    @Override
    public void appendSession(long deckId, LocalDate date, int viewed, int correct, int repeat, int hard,
                              long sessionDurationMs, long totalAnswerDelayMs,
                              Collection<Long> knownCardIdsDelta) {
        if (viewed <= 0) return;
        Map<LocalDate, DailyStatsRecord> byDate = deckIdToDaily.computeIfAbsent(deckId, k -> new ConcurrentHashMap<>());
        DailyStatsRecord rec = byDate.computeIfAbsent(date, d -> new DailyStatsRecord());
        rec.date = date;
        rec.sessions += 1;
        rec.viewed += viewed;
        rec.correct += correct;
        rec.repeat += repeat;
        rec.hard += hard;
        rec.totalDurationMs += sessionDurationMs;
        rec.totalAnswerDelayMs += totalAnswerDelayMs;

        if (knownCardIdsDelta != null && !knownCardIdsDelta.isEmpty()) {
            deckIdToKnown.computeIfAbsent(deckId, k -> Collections.synchronizedSet(new HashSet<>())).addAll(knownCardIdsDelta);
        }
    }

    @Override
    public List<DailyStatsRecord> getDailyStats(long deckId) {
        return deckIdToDaily.getOrDefault(deckId, Collections.emptyMap())
                .values().stream()
                .sorted(Comparator.comparing(r -> r.date))
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


