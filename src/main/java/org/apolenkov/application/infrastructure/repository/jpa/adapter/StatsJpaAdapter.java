package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.KnownCardJpaRepository;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.StatsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile({"dev", "jpa", "prod"})
public class StatsJpaAdapter implements StatsRepository {

    private final StatsJpaRepository statsRepo;
    private final KnownCardJpaRepository knownRepo;

    public StatsJpaAdapter(StatsJpaRepository statsRepo, KnownCardJpaRepository knownRepo) {
        this.statsRepo = statsRepo;
        this.knownRepo = knownRepo;
    }

    @Override
    @Transactional
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
        int updated = statsRepo.accumulate(
                deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);
        if (updated == 0) {
            DeckDailyStatsEntity e = new DeckDailyStatsEntity();
            DeckDailyStatsEntity.Id id = new DeckDailyStatsEntity.Id(deckId, date);
            e.setId(id);
            e.setSessions(1);
            e.setViewed(viewed);
            e.setCorrect(correct);
            e.setRepeatCount(repeat);
            e.setHard(hard);
            e.setTotalDurationMs(sessionDurationMs);
            e.setTotalAnswerDelayMs(totalAnswerDelayMs);
            statsRepo.save(e);
        }

        if (knownCardIdsDelta != null && !knownCardIdsDelta.isEmpty()) {
            Set<Long> existing = knownRepo.findKnownCardIds(deckId);
            for (Long cardId : knownCardIdsDelta) {
                if (!existing.contains(cardId)) {
                    KnownCardEntity k = new KnownCardEntity();
                    k.setDeckId(deckId);
                    k.setCardId(cardId);
                    knownRepo.save(k);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyStatsRecord> getDailyStats(long deckId) {
        return statsRepo.findById_DeckIdOrderById_DateAsc(deckId).stream()
                .map(e -> new DailyStatsRecord(
                        e.getId().getDate(),
                        e.getSessions(),
                        e.getViewed(),
                        e.getCorrect(),
                        e.getRepeatCount(),
                        e.getHard(),
                        e.getTotalDurationMs(),
                        e.getTotalAnswerDelayMs()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(long deckId) {
        return knownRepo.findKnownCardIds(deckId);
    }

    @Override
    @Transactional
    public void setCardKnown(long deckId, long cardId, boolean known) {
        if (known) {
            Set<Long> existing = knownRepo.findKnownCardIds(deckId);
            if (!existing.contains(cardId)) {
                KnownCardEntity k = new KnownCardEntity();
                k.setDeckId(deckId);
                k.setCardId(cardId);
                knownRepo.save(k);
            }
        } else {
            knownRepo.deleteKnown(deckId, cardId);
        }
    }

    @Override
    @Transactional
    public void resetDeckProgress(long deckId) {
        knownRepo.deleteByDeckId(deckId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, DeckAggregate> getAggregatesForDecks(
            java.util.Collection<Long> deckIds, java.time.LocalDate today) {
        if (deckIds == null || deckIds.isEmpty()) return java.util.Collections.emptyMap();
        java.util.List<Long> ids =
                deckIds.stream().distinct().map(Long::valueOf).toList();
        java.util.List<Object[]> rows = statsRepo.findAllByDeckIds(ids);
        java.util.Map<Long, DeckAggregate> result = new java.util.HashMap<>();
        for (Object[] r : rows) {
            long deckId = (Long) r[0];
            java.time.LocalDate date = (java.time.LocalDate) r[1];
            int sessions = ((Number) r[2]).intValue();
            int viewed = ((Number) r[3]).intValue();
            int correct = ((Number) r[4]).intValue();
            int repeatCount = ((Number) r[5]).intValue();
            int hard = ((Number) r[6]).intValue();
            DeckAggregate agg = result.getOrDefault(deckId, new DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            int sessionsAll = agg.sessionsAll() + sessions;
            int viewedAll = agg.viewedAll() + viewed;
            int correctAll = agg.correctAll() + correct;
            int repeatAll = agg.repeatAll() + repeatCount;
            int hardAll = agg.hardAll() + hard;
            int sessionsToday = agg.sessionsToday() + (date.equals(today) ? sessions : 0);
            int viewedToday = agg.viewedToday() + (date.equals(today) ? viewed : 0);
            int correctToday = agg.correctToday() + (date.equals(today) ? correct : 0);
            int repeatToday = agg.repeatToday() + (date.equals(today) ? repeatCount : 0);
            int hardToday = agg.hardToday() + (date.equals(today) ? hard : 0);
            result.put(
                    deckId,
                    new DeckAggregate(
                            sessionsAll,
                            viewedAll,
                            correctAll,
                            repeatAll,
                            hardAll,
                            sessionsToday,
                            viewedToday,
                            correctToday,
                            repeatToday,
                            hardToday));
        }
        for (Long id : ids) {
            result.putIfAbsent(id, new DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        }
        return result;
    }
}
