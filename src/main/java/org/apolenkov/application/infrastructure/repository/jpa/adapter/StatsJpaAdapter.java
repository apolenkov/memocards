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

/**
 * JPA implementation of the StatsRepository interface.
 *
 * <p>This adapter provides JPA-based persistence for user statistics and progress tracking.
 * It handles daily statistics accumulation, known card management, and deck progress aggregation.
 * The implementation uses Spring Data JPA repositories for data access and provides
 * transactional guarantees for all operations.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Session statistics recording with automatic daily aggregation</li>
 *   <li>Known card tracking to monitor user progress</li>
 *   <li>Comprehensive deck statistics with today vs. all-time metrics</li>
 *   <li>Efficient batch operations for multiple decks</li>
 * </ul>
 */
@Repository
@Profile({"dev", "jpa", "prod"})
public class StatsJpaAdapter implements StatsRepository {

    private final StatsJpaRepository statsRepo;
    private final KnownCardJpaRepository knownRepo;

    /**
     * Constructs a new StatsJpaAdapter with the required repositories.
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
     * <p>This method handles the recording of a complete practice session, including
     * performance metrics and any changes to card knowledge status. The implementation
     * first attempts to accumulate session data to existing daily records, and if
     * no record exists for the date, creates a new one.</p>
     *
     * <p>The method also processes any cards that changed knowledge status during
     * the session, ensuring the known cards list is kept up to date without duplicates.</p>
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
     * <p>Fetches all daily statistics records for the specified deck and maps them
     * to domain objects. The results are ordered chronologically by date to provide
     * a timeline view of user progress.</p>
     *
     * @param deckId the ID of the deck to retrieve statistics for
     * @return a chronologically sorted list of daily statistics records
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
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all card IDs marked as known in a specific deck.
     *
     * <p>Returns a set of card IDs that the user has successfully learned
     * in the specified deck. This information is used to calculate progress
     * percentages and track user advancement.</p>
     *
     * @param deckId the ID of the deck to check for known cards
     * @return a set of card IDs marked as known
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(long deckId) {
        return knownRepo.findKnownCardIds(deckId);
    }

    /**
     * Sets the knowledge status of a specific card in a deck.
     *
     * <p>Updates the known status of a card based on user performance. When marking
     * a card as known, the system checks for duplicates to avoid creating redundant
     * entries. When marking as unknown, the card is removed from the known cards list.</p>
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
     * <p>Removes all known card entries for the specified deck, effectively
     * resetting the user's progress to zero. This is useful when a user wants
     * to start over or when deck content has been significantly changed.</p>
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
     * <p>Fetches comprehensive statistics for the specified decks, providing both
     * all-time totals and today's metrics. The method efficiently processes multiple
     * decks in a single database query and ensures all requested deck IDs have
     * entries in the result, even if no statistics exist.</p>
     *
     * <p>The aggregation logic separates today's statistics from historical totals,
     * allowing for both current progress tracking and long-term performance analysis.</p>
     *
     * @param deckIds collection of deck IDs to retrieve statistics for
     * @param today the reference date for "today's" statistics
     * @return a map of deck ID to aggregated statistics
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, DeckAggregate> getAggregatesForDecks(
            java.util.Collection<Long> deckIds, java.time.LocalDate today) {
        if (deckIds == null || deckIds.isEmpty()) return java.util.Collections.emptyMap();

        // Ensure unique deck IDs and convert to Long type
        java.util.List<Long> ids =
                deckIds.stream().distinct().map(Long::valueOf).toList();

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
