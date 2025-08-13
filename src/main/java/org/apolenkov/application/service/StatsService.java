package org.apolenkov.application.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    public static class DailyStats {
        public final LocalDate date;
        public final int sessions;
        public final int viewed;
        public final int correct;
        public final int repeat;
        public final int hard;
        public final long totalDurationMs;
        public final long totalAnswerDelayMs;

        public DailyStats(
                LocalDate date,
                int sessions,
                int viewed,
                int correct,
                int repeat,
                int hard,
                long totalDurationMs,
                long totalAnswerDelayMs) {
            this.date = date;
            this.sessions = sessions;
            this.viewed = viewed;
            this.correct = correct;
            this.repeat = repeat;
            this.hard = hard;
            this.totalDurationMs = totalDurationMs;
            this.totalAnswerDelayMs = totalAnswerDelayMs;
        }

        public double getAvgDelayMs() {
            return viewed > 0 ? (double) totalAnswerDelayMs / viewed : 0.0;
        }
    }

    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public void recordSession(
            long deckId,
            int viewed,
            int correct,
            int repeat,
            int hard,
            Duration sessionDuration,
            long totalAnswerDelayMs,
            Collection<Long> knownCardIdsDelta) {
        if (viewed <= 0) return;
        LocalDate today = LocalDate.now();
        statsRepository.appendSession(
                deckId,
                today,
                viewed,
                correct,
                repeat,
                hard,
                sessionDuration.toMillis(),
                totalAnswerDelayMs,
                knownCardIdsDelta);
    }

    public List<DailyStats> getDailyStatsForDeck(long deckId) {
        return statsRepository.getDailyStats(deckId).stream()
                .map(r -> new DailyStats(
                        r.date(),
                        r.sessions(),
                        r.viewed(),
                        r.correct(),
                        r.repeat(),
                        r.hard(),
                        r.totalDurationMs(),
                        r.totalAnswerDelayMs()))
                .sorted(Comparator.comparing(ds -> ds.date))
                .toList();
    }

    public int getDeckProgressPercent(long deckId, int deckSize) {
        if (deckSize <= 0) return 0;
        int known = statsRepository.getKnownCardIds(deckId).size();
        int percent = (int) Math.round(100.0 * known / deckSize);
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return percent;
    }

    public boolean isCardKnown(long deckId, long cardId) {
        return statsRepository.getKnownCardIds(deckId).contains(cardId);
    }

    public Set<Long> getKnownCardIds(long deckId) {
        return statsRepository.getKnownCardIds(deckId);
    }

    public void setCardKnown(long deckId, long cardId, boolean known) {
        statsRepository.setCardKnown(deckId, cardId, known);
    }

    public void resetDeckProgress(long deckId) {
        statsRepository.resetDeckProgress(deckId);
    }
}
