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

public interface StatsJpaRepository extends JpaRepository<DeckDailyStatsEntity, DeckDailyStatsEntity.Id> {

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

    // Optimized query with index hints
    @QueryHints({
        @QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_deck_date"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    @Query("SELECT s FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId = :deckId " + "ORDER BY s.id.date ASC")
    List<DeckDailyStatsEntity> findById_DeckIdOrderById_DateAsc(@Param("deckId") long deckId);

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

    // Query for today's stats only (optimized)
    @QueryHints({@QueryHint(name = "org.hibernate.comment", value = "Uses idx_deck_daily_stats_date")})
    @Query("SELECT s FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId IN :deckIds " + "  AND s.id.date = :today")
    List<DeckDailyStatsEntity> findTodayStatsByDeckIds(
            @Param("deckIds") List<Long> deckIds, @Param("today") LocalDate today);

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

    // Check if stats exist for a deck on a specific date
    @Query("SELECT COUNT(s) > 0 FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId = :deckId AND s.id.date = :date")
    boolean existsByDeckIdAndDate(@Param("deckId") long deckId, @Param("date") LocalDate date);

    // Get latest stats for a deck
    @Query("SELECT s FROM DeckDailyStatsEntity s " + "WHERE s.id.deckId = :deckId "
            + "ORDER BY s.id.date DESC "
            + "LIMIT 1")
    Optional<DeckDailyStatsEntity> findLatestByDeckId(@Param("deckId") long deckId);
}
