package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(
        name = "deck_daily_stats",
        indexes = {
            @Index(name = "idx_deck_daily_stats_deck_date", columnList = "deck_id, date DESC"),
            @Index(name = "idx_deck_daily_stats_date", columnList = "date DESC"),
            @Index(name = "idx_deck_daily_stats_performance", columnList = "deck_id, correct DESC, viewed DESC"),
            @Index(name = "idx_deck_daily_stats_user_progress", columnList = "deck_id, date DESC, sessions DESC")
        })
public class DeckDailyStatsEntity {

    @Embeddable
    public static class Id implements Serializable {
        @NotNull
        private Long deckId;

        @NotNull
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

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int sessions;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int viewed;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int correct;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int repeatCount;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int hard;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalDurationMs;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalAnswerDelayMs;

    // Hibernate optimization: add @Version for optimistic locking
    @Version
    @Column(name = "version")
    private Long version;

    // Hibernate optimization: add @CreatedDate and @LastModifiedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    // Pre-persist and pre-update hooks for audit fields
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and setters
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
