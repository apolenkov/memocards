package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for daily deck statistics.
 *
 * <p>Provides operations for aggregating and querying practice statistics,
 * with optimized queries for analytics and performance tracking.</p>
 */
public interface StatsJpaRepository extends JpaRepository<DeckDailyStatsEntity, DeckDailyStatsEntity.Id> {

    /**
     * Accumulates practice statistics for a deck on a specific date.
     *
     * @param deckId deck identifier
     * @param date practice date
     * @param viewed cards viewed count
     * @param correct correct answers count
     * @param repeat repeat attempts count
     * @param hard hard difficulty count
     * @param duration total practice duration in ms
     * @param totalAnswerDelayMs total answer delay in ms
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeckDailyStatsEntity s " + "SET s.sessions = s.sessions + 1, "
            + "    s.viewed = s.viewed + :viewed, "
            + "    s.correct = s.correct + :correct, "
            + "    s.repeatCount = s.repeatCount + :repeat, "
            + "    s.hard = s.hard + :hard, "
            + "    s.totalDurationMs = s.totalDurationMs + :dur, "
            + "    s.totalAnswerDelayMs = s.totalAnswerDelayMs + :ans, "
            + "    s.updatedAt = CURRENT_TIMESTAMP "
            + "WHERE s.id.deckId = :deckId AND s.id.date = :date")
    @SuppressWarnings({"java:S107", "checkstyle:ParameterNumber"})
    int accumulate(
            @Param("deckId") long deckId,
            @Param("date") LocalDate date,
            @Param("viewed") int viewed,
            @Param("correct") int correct,
            @Param("repeat") int repeat,
            @Param("hard") int hard,
            @Param("dur") long duration,
            @Param("ans") long totalAnswerDelayMs);

    /**
     * Finds daily statistics for a deck ordered by date.
     * Uses optimized query with index hints for performance.
     *
     * @param deckId the deck identifier
     * @return list of daily statistics ordered by date ascending
     */
    // Optimized query with index hints
    @QueryHints({
        @QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_deck_date"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    @Query("SELECT s FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId = :deckId " + "ORDER BY s.id.date ASC")
    List<DeckDailyStatsEntity> findByDeckIdOrderByDateAsc(@Param("deckId") long deckId);

    /**
     * Retrieves aggregated statistics for multiple decks.
     * Uses optimized query with index hints for bulk operations.
     *
     * @param deckIds list of deck identifiers
     * @return list of aggregated statistics as object arrays
     */
    // Optimized query for aggregates with index hints
    @QueryHints({
        @QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_deck_date"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    @Query("SELECT s.id.deckId, s.id.date, s.sessions, s.viewed, s.correct, s.repeatCount, s.hard "
            + "FROM DeckDailyStatsEntity s "
            + "WHERE s.id.deckId IN :deckIds "
            + "ORDER BY s.id.deckId, s.id.date DESC")
    List<Object[]> findAllByDeckIds(@Param("deckIds") List<Long> deckIds);
}
