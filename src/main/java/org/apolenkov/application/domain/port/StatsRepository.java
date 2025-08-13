package org.apolenkov.application.domain.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface StatsRepository {
    class DailyStatsRecord {
        public LocalDate date;
        public int sessions;
        public int viewed;
        public int correct;
        public int repeat;
        public int hard;
        public long totalDurationMs;
        public long totalAnswerDelayMs;
    }

    void appendSession(long deckId,
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


