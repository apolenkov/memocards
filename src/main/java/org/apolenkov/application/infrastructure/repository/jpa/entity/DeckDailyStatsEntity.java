package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "deck_daily_stats")
public class DeckDailyStatsEntity {

    @Embeddable
    public static class Id implements Serializable {
        private Long deckId;
        private LocalDate date;

        public Id() {}

        public Id(Long deckId, LocalDate date) {
            this.deckId = deckId;
            this.date = date;
        }

        public Long getDeckId() {
            return deckId;
        }

        public void setDeckId(Long deckId) {
            this.deckId = deckId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(deckId, date);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id other)) return false;
            return java.util.Objects.equals(deckId, other.deckId) && java.util.Objects.equals(date, other.date);
        }
    }

    @EmbeddedId
    private Id id;

    @Column(nullable = false)
    private int sessions;

    @Column(nullable = false)
    private int viewed;

    @Column(nullable = false)
    private int correct;

    @Column(nullable = false)
    private int repeatCount;

    @Column(nullable = false)
    private int hard;

    @Column(nullable = false)
    private long totalDurationMs;

    @Column(nullable = false)
    private long totalAnswerDelayMs;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public int getSessions() {
        return sessions;
    }

    public void setSessions(int sessions) {
        this.sessions = sessions;
    }

    public int getViewed() {
        return viewed;
    }

    public void setViewed(int viewed) {
        this.viewed = viewed;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getHard() {
        return hard;
    }

    public void setHard(int hard) {
        this.hard = hard;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public long getTotalAnswerDelayMs() {
        return totalAnswerDelayMs;
    }

    public void setTotalAnswerDelayMs(long totalAnswerDelayMs) {
        this.totalAnswerDelayMs = totalAnswerDelayMs;
    }
}
