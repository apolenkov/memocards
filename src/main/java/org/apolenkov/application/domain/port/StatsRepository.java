package org.apolenkov.application.domain.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface StatsRepository {
    record DailyStatsRecord(
            LocalDate date,
            int sessions,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long totalDurationMs,
            long totalAnswerDelayMs) {}

    void appendSession(
            long deckId,
            LocalDate date,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long sessionDurationMs,
            long totalAnswerDelayMs,
            Collection<Long> knownCardIdsDelta);

    List<DailyStatsRecord> getDailyStats(long deckId);

    Set<Long> getKnownCardIds(long deckId);

    void setCardKnown(long deckId, long cardId, boolean known);

    void resetDeckProgress(long deckId);
}
