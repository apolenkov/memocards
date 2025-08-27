package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.KnownCardJpaRepository;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.StatsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter for statistics and progress tracking operations.
 *
 * <p>Manages practice session statistics, known card tracking,
 * and deck progress aggregation with transactional support.</p>
 */
@Repository
@Profile({"dev", "prod"})
public class StatsJpaAdapter implements StatsRepository {

    private final StatsJpaRepository statsRepo;
    private final KnownCardJpaRepository knownRepo;

    /**
     * Creates adapter with JPA repository dependencies.
     *
     * @param statsRepo repository for daily statistics persistence
     * @param knownRepo repository for known card tracking
     */
    public StatsJpaAdapter(StatsJpaRepository statsRepo, KnownCardJpaRepository knownRepo) {
        this.statsRepo = statsRepo;
        this.knownRepo = knownRepo;
    }

    /**
     * Records a practice session and updates deck statistics.
     *
     * @param deckId the ID of the deck being practiced
     * @param date the date of the practice session
     * @param viewed the number of cards viewed in this session
     * @param correct the number of cards answered correctly
     * @param repeat the number of cards marked for repetition
     * @param hard the number of cards marked as difficult
     * @param sessionDurationMs the total duration of the practice session in milliseconds
     * @param totalAnswerDelayMs the total time spent thinking before answering in milliseconds
     * @param knownCardIdsDelta the collection of card IDs whose knowledge status changed
     */
    @Override
    @Transactional
    @SuppressWarnings("ParameterNumber")
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
        // Try to accumulate session data to existing daily record
        int updated = statsRepo.accumulate(
                deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);

        // If no existing record was updated, create a new daily stats entry
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

        // Process any cards that changed knowledge status during this session
        if (knownCardIdsDelta != null && !knownCardIdsDelta.isEmpty()) {
            Set<Long> existing = knownRepo.findKnownCardIds(deckId);
            for (Long cardId : knownCardIdsDelta) {
                // Only add cards that aren't already marked as known
                if (!existing.contains(cardId)) {
                    KnownCardEntity k = new KnownCardEntity();
                    k.setDeckId(deckId);
                    k.setCardId(cardId);
                    knownRepo.save(k);
                }
            }
        }
    }

    /**
     * Retrieves daily statistics for a specific deck.
     *
     * @param deckId the ID of the deck to retrieve statistics for
     * @return chronologically sorted list of daily statistics records
     */
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
                .toList();
    }

    /**
     * Retrieves all card IDs marked as known in a specific deck.
     *
     * @param deckId the ID of the deck to check for known cards
     * @return set of card IDs marked as known
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(long deckId) {
        return knownRepo.findKnownCardIds(deckId);
    }

    /**
     * Sets the knowledge status of a specific card in a deck.
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to update
     * @param known true to mark as known, false to mark as unknown
     */
    @Override
    @Transactional
    public void setCardKnown(long deckId, long cardId, boolean known) {
        if (known) {
            // Check if card is already marked as known to avoid duplicates
            Set<Long> existing = knownRepo.findKnownCardIds(deckId);
            if (!existing.contains(cardId)) {
                // Create new known card entry
                KnownCardEntity k = new KnownCardEntity();
                k.setDeckId(deckId);
                k.setCardId(cardId);
                knownRepo.save(k);
            }
        } else {
            // Remove card from known cards list
            knownRepo.deleteKnown(deckId, cardId);
        }
    }

    /**
     * Resets all progress for a specific deck.
     *
     * @param deckId the ID of the deck to reset progress for
     */
    @Override
    @Transactional
    public void resetDeckProgress(long deckId) {
        knownRepo.deleteByDeckId(deckId);
    }

    /**
     * Retrieves aggregated statistics for multiple decks.
     *
     * @param deckIds collection of deck IDs to retrieve statistics for
     * @param today the reference date for "today's" statistics
     * @return map of deck ID to aggregated statistics
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, DeckAggregate> getAggregatesForDecks(
            java.util.Collection<Long> deckIds, java.time.LocalDate today) {
        if (deckIds == null || deckIds.isEmpty()) return java.util.Collections.emptyMap();

        // Ensure unique deck IDs and convert to Long type
        java.util.List<Long> ids = deckIds.stream().distinct().toList();

        // Fetch raw statistics data from database
        java.util.List<Object[]> rows = statsRepo.findAllByDeckIds(ids);
        java.util.Map<Long, DeckAggregate> result = new java.util.HashMap<>();

        // Process each row and accumulate statistics
        for (Object[] r : rows) {
            long deckId = (Long) r[0];
            java.time.LocalDate date = (java.time.LocalDate) r[1];
            int sessions = ((Number) r[2]).intValue();
            int viewed = ((Number) r[3]).intValue();
            int correct = ((Number) r[4]).intValue();
            int repeatCount = ((Number) r[5]).intValue();
            int hard = ((Number) r[6]).intValue();

            // Get existing aggregate or create default one
            DeckAggregate agg = result.getOrDefault(deckId, new DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));

            // Accumulate total statistics across all dates
            int sessionsAll = agg.sessionsAll() + sessions;
            int viewedAll = agg.viewedAll() + viewed;
            int correctAll = agg.correctAll() + correct;
            int repeatAll = agg.repeatAll() + repeatCount;
            int hardAll = agg.hardAll() + hard;

            // Accumulate today's statistics only for current date
            int sessionsToday = agg.sessionsToday() + (date.equals(today) ? sessions : 0);
            int viewedToday = agg.viewedToday() + (date.equals(today) ? viewed : 0);
            int correctToday = agg.correctToday() + (date.equals(today) ? correct : 0);
            int repeatToday = agg.repeatToday() + (date.equals(today) ? repeatCount : 0);
            int hardToday = agg.hardToday() + (date.equals(today) ? hard : 0);

            // Update result with accumulated statistics
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

        // Ensure all requested deck IDs have entries (even if no stats exist)
        for (Long id : ids) {
            result.putIfAbsent(id, new DeckAggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        }
        return result;
    }
}
