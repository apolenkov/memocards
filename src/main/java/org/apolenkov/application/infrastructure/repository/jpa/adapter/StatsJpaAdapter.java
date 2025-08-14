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
}
