package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
     *
     * <p>Uses optimized query with index hints for performance.
     * Returns statistics in chronological order for trend analysis.</p>
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
    List<DeckDailyStatsEntity> findById_DeckIdOrderById_DateAsc(@Param("deckId") long deckId);

    /**
     * Retrieves aggregated statistics for multiple decks.
     *
     * <p>Uses optimized query with index hints for bulk operations.
     * Returns summary data for analytics and reporting.</p>
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

    /**
     * Calculates aggregated statistics for decks within a date range.
     *
     * <p>Uses optimized query for analytics with performance hints.
     * Groups data by deck for trend analysis.</p>
     *
     * @param deckIds list of deck identifiers
     * @param fromDate start date for aggregation
     * @return list of aggregated statistics grouped by deck
     */
    // New optimized queries for analytics
    @QueryHints({@QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_performance")})
    @Query("SELECT s.id.deckId, " + "       SUM(s.sessions) as totalSessions, "
            + "       SUM(s.viewed) as totalViewed, "
            + "       SUM(s.correct) as totalCorrect, "
            + "       SUM(s.repeatCount) as totalRepeat, "
            + "       SUM(s.hard) as totalHard "
            + "FROM DeckDailyStatsEntity s "
            + "WHERE s.id.deckId IN :deckIds "
            + "  AND s.id.date >= :fromDate "
            + "GROUP BY s.id.deckId")
    List<Object[]> getAggregatesByDateRange(
            @Param("deckIds") List<Long> deckIds, @Param("fromDate") LocalDate fromDate);

    /**
     * Finds today's statistics for specified decks.
     *
     * <p>Uses optimized query for current day data retrieval.
     * Efficient for real-time dashboard updates.</p>
     *
     * @param deckIds list of deck identifiers
     * @param today the date to search for
     * @return list of today's statistics for the specified decks
     */
    // Query for today's stats only (optimized)
    @QueryHints({@QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_date")})
    @Query("SELECT s FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId IN :deckIds " + "  AND s.id.date = :today")
    List<DeckDailyStatsEntity> findTodayStatsByDeckIds(
            @Param("deckIds") List<Long> deckIds, @Param("today") LocalDate today);

    /**
     * Generates performance leaderboard for decks.
     *
     * <p>Calculates accuracy and session counts for ranking.
     * Uses optimized query with performance hints for analytics.</p>
     *
     * @param deckIds list of deck identifiers
     * @param fromDate start date for leaderboard calculation
     * @param minViewed minimum viewed cards threshold
     * @return list of performance rankings ordered by accuracy
     */
    // Query for performance leaderboards
    @QueryHints({@QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_performance")})
    @Query("SELECT s.id.deckId, " + "       AVG(CAST(s.correct AS double) / NULLIF(s.viewed, 0)) as accuracy, "
            + "       SUM(s.sessions) as totalSessions "
            + "FROM DeckDailyStatsEntity s "
            + "WHERE s.id.deckId IN :deckIds "
            + "  AND s.id.date >= :fromDate "
            + "  AND s.viewed > 0 "
            + "GROUP BY s.id.deckId "
            + "HAVING SUM(s.viewed) >= :minViewed "
            + "ORDER BY accuracy DESC, totalSessions DESC")
    List<Object[]> getPerformanceLeaderboard(
            @Param("deckIds") List<Long> deckIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("minViewed") int minViewed);

    /**
     * Tracks user progress over time for a specific deck.
     *
     * <p>Uses optimized query for progress visualization.
     * Groups daily statistics for trend analysis.</p>
     *
     * @param deckId the deck identifier
     * @param fromDate start date for progress tracking
     * @return list of daily progress data ordered by date
     */
    // Query for user progress over time
    @QueryHints({@QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_user_progress")})
    @Query("SELECT s.id.date, " + "       SUM(s.sessions) as dailySessions, "
            + "       SUM(s.viewed) as dailyViewed, "
            + "       SUM(s.correct) as dailyCorrect "
            + "FROM DeckDailyStatsEntity s "
            + "WHERE s.id.deckId = :deckId "
            + "  AND s.id.date >= :fromDate "
            + "GROUP BY s.id.date "
            + "ORDER BY s.id.date ASC")
    List<Object[]> getUserProgressOverTime(@Param("deckId") long deckId, @Param("fromDate") LocalDate fromDate);

    /**
     * Checks if statistics exist for a deck on a specific date.
     *
     * @param deckId the deck identifier
     * @param date the date to check
     * @return true if statistics exist, false otherwise
     */
    // Check if stats exist for a deck on a specific date
    @Query("SELECT COUNT(s) > 0 FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId = :deckId AND s.id.date = :date")
    boolean existsByDeckIdAndDate(@Param("deckId") long deckId, @Param("date") LocalDate date);

    /**
     * Gets the latest statistics for a deck.
     *
     * <p>Uses Spring Data derived query for portability.
     * Returns the most recent statistics entry.</p>
     *
     * @param deckId the deck identifier
     * @return latest statistics if available, empty otherwise
     */
    // Get latest stats for a deck using Spring Data derived query (portable, no vendor-specific LIMIT)
    Optional<DeckDailyStatsEntity> findTopById_DeckIdOrderById_DateDesc(@Param("deckId") long deckId);
}
