package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.KnownCardJpaRepository;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.StatsJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of StatsRepository.
 * Handles persistence and retrieval of statistics data using JPA entities.
 */
@Repository
@Profile({"dev", "prod"})
public class StatsJpaAdapter implements StatsRepository {

    private final StatsJpaRepository statsRepo;
    private final KnownCardJpaRepository knownRepo;

    /**
     * Constructs StatsJpaAdapter with required dependencies.
     *
     * @param statsRepository repository for daily statistics
     * @param knownCardRepository repository for known card tracking
     */
    public StatsJpaAdapter(final StatsJpaRepository statsRepository, final KnownCardJpaRepository knownCardRepository) {
        this.statsRepo = statsRepository;
        this.knownRepo = knownCardRepository;
    }

    /**
     * Records a practice session and updates deck statistics.
     *
     * @param sessionStats session statistics data
     * @param date the date of the practice session
     */
    @Override
    @Transactional
    public void appendSession(final SessionStatsDto sessionStats, final LocalDate date) {
        // Try to accumulate session data to existing daily record
        SessionStatsDto accumulateStats = SessionStatsDto.builder()
                .deckId(sessionStats.deckId())
                .viewed(sessionStats.viewed())
                .correct(sessionStats.correct())
                .repeat(sessionStats.repeat())
                .hard(sessionStats.hard())
                .sessionDurationMs(sessionStats.sessionDurationMs())
                .totalAnswerDelayMs(sessionStats.totalAnswerDelayMs())
                .knownCardIdsDelta(null)
                .build();
        int updated = statsRepo.accumulate(accumulateStats, date);

        // If no existing record was updated, create a new daily stats entry
        if (updated == 0) {
            DeckDailyStatsEntity entry = createNewDailyStatsEntry(sessionStats, date);
            statsRepo.save(entry);
        }

        // Process any cards that changed knowledge status during this session
        processKnownCardChanges(sessionStats);
    }

    /**
     * Creates a new daily statistics entry for the session.
     *
     * @param sessionStats session statistics data
     * @param date date for the statistics
     * @return new DeckDailyStatsEntity instance
     */
    private DeckDailyStatsEntity createNewDailyStatsEntry(final SessionStatsDto sessionStats, final LocalDate date) {
        DeckDailyStatsEntity entry = new DeckDailyStatsEntity();
        DeckDailyStatsEntity.Id id = new DeckDailyStatsEntity.Id(sessionStats.deckId(), date);
        entry.setId(id);
        entry.setSessions(1);
        entry.setViewed(sessionStats.viewed());
        entry.setCorrect(sessionStats.correct());
        entry.setRepeatCount(sessionStats.repeat());
        entry.setHard(sessionStats.hard());
        entry.setTotalDurationMs(sessionStats.sessionDurationMs());
        entry.setTotalAnswerDelayMs(sessionStats.totalAnswerDelayMs());
        return entry;
    }

    /**
     * Processes changes in card knowledge status during the session.
     *
     * @param sessionStats session statistics data
     */
    private void processKnownCardChanges(final SessionStatsDto sessionStats) {
        if (sessionStats.knownCardIdsDelta() != null
                && !sessionStats.knownCardIdsDelta().isEmpty()) {
            Set<Long> existing = knownRepo.findKnownCardIds(sessionStats.deckId());
            for (Long cardId : sessionStats.knownCardIdsDelta()) {
                // Only add cards that aren't already marked as known
                if (!existing.contains(cardId)) {
                    KnownCardEntity knownCard = new KnownCardEntity();
                    knownCard.setDeckId(sessionStats.deckId());
                    knownCard.setCardId(cardId);
                    knownRepo.save(knownCard);
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
    public List<DailyStatsRecord> getDailyStats(final long deckId) {
        return statsRepo.findByDeckIdOrderByDateAsc(deckId).stream()
                .map(this::mapToDailyStatsRecord)
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
    public Set<Long> getKnownCardIds(final long deckId) {
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
    public void setCardKnown(final long deckId, final long cardId, final boolean known) {
        if (known) {
            // Check if card is already marked as known
            Set<Long> existing = knownRepo.findKnownCardIds(deckId);
            if (!existing.contains(cardId)) {
                KnownCardEntity knownCard = new KnownCardEntity();
                knownCard.setDeckId(deckId);
                knownCard.setCardId(cardId);
                knownRepo.save(knownCard);
            }
        } else {
            // Remove card from known list
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
    public void resetDeckProgress(final long deckId) {
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
            final java.util.Collection<Long> deckIds, final java.time.LocalDate today) {
        if (deckIds == null || deckIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

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

    /**
     * Maps JPA entity to domain record.
     *
     * @param entity JPA entity
     * @return domain record
     */
    private DailyStatsRecord mapToDailyStatsRecord(final DeckDailyStatsEntity entity) {
        return new DailyStatsRecord(
                entity.getId().getDate(),
                entity.getSessions(),
                entity.getViewed(),
                entity.getCorrect(),
                entity.getRepeatCount(),
                entity.getHard(),
                entity.getTotalDurationMs(),
                entity.getTotalAnswerDelayMs());
    }
}
