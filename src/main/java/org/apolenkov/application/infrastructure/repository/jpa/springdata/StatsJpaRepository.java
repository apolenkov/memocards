package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.time.LocalDate;
import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatsJpaRepository extends JpaRepository<DeckDailyStatsEntity, DeckDailyStatsEntity.Id> {

    @Modifying
    @Query(
            "update DeckDailyStatsEntity s set s.sessions=s.sessions+1, s.viewed=s.viewed+:viewed, s.correct=s.correct+:correct, s.repeatCount=s.repeatCount+:repeat, s.hard=s.hard+:hard, s.totalDurationMs=s.totalDurationMs+:dur, s.totalAnswerDelayMs=s.totalAnswerDelayMs+:ans where s.id.deckId=:deckId and s.id.date=:date")
    int accumulate(
            @Param("deckId") long deckId,
            @Param("date") LocalDate date,
            @Param("viewed") int viewed,
            @Param("correct") int correct,
            @Param("repeat") int repeat,
            @Param("hard") int hard,
            @Param("dur") long duration,
            @Param("ans") long totalAnswerDelayMs);

    List<DeckDailyStatsEntity> findById_DeckIdOrderById_DateAsc(long deckId);
}
