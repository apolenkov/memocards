package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * JPA entity representing daily statistics for deck usage and performance.
 *
 * <p>Tracks daily metrics with composite primary key (deck ID + date).</p>
 */
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

    /**
     * Composite primary key for daily statistics records with deck identifier and date.
     * Enables efficient daily aggregation and time-series analysis.
     */
    @Embeddable
    public static class Id implements Serializable {

        /**
         * Identifier of the deck these statistics belong to.
         *
         * <p>
         * This field establishes the relationship between daily statistics
         * and a specific deck. It enables aggregation and analysis of
         * performance data for individual learning collections.
         * </p>
         *
         *  Database Type: BIGINT, Constraints: Non-nullable, foreign key reference,
         * Relationship: Many-to-one with DeckEntity, Business Rule: Must reference an existing deck
         */
        @NotNull
        private Long deckId;

        /**
         * Calendar date for these daily statistics.
         *
         * <p>
         * This field represents the specific calendar day for which
         * the statistics were collected. It enables daily aggregation
         * and time-series analysis of deck performance.
         * </p>
         *
         * Database Type: DATE, Constraints: Non-nullable,
         * Format: ISO 8601 date format (YYYY-MM-DD), Purpose: Daily granularity for statistics
         */
        @NotNull
        private LocalDate date;

        /**
         * Default constructor required by JPA.
         *
         * <p>
         * This constructor is required by the JPA specification for
         * embeddable classes. It should not be used directly in application
         * code.
         * </p>
         */
        public Id() {}

        /**
         * Constructs a composite key with deck identifier and date.
         *
         * @param deckId the deck identifier, must not be null
         * @param date the calendar date, must not be null
         * @throws IllegalArgumentException if either parameter is null
         */
        public Id(Long deckId, LocalDate date) {
            if (deckId == null) {
                throw new IllegalArgumentException("Deck ID cannot be null");
            }
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }
            this.deckId = deckId;
            this.date = date;
        }

        /**
         * Gets the deck identifier for this composite key.
         *
         * @return the deck identifier, never null
         */
        public Long getDeckId() {
            return deckId;
        }

        /**
         * Sets the deck identifier for this composite key (use with caution).
         *
         * @param deckId the deck identifier to set, must not be null
         * @throws IllegalArgumentException if deckId is null
         */
        public void setDeckId(Long deckId) {
            if (deckId == null) {
                throw new IllegalArgumentException("Deck ID cannot be null");
            }
            this.deckId = deckId;
        }

        /**
         * Gets the calendar date for this composite key.
         *
         * @return the calendar date, never null
         */
        public LocalDate getDate() {
            return date;
        }

        /**
         * Sets the calendar date for this composite key (use with caution).
         *
         * @param date the calendar date to set, must not be null
         * @throws IllegalArgumentException if date is null
         */
        public void setDate(LocalDate date) {
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }
            this.date = date;
        }

        /**
         * Generates a hash code for this composite key.
         *
         * @return a hash code value for this composite key
         */
        @Override
        public int hashCode() {
            return java.util.Objects.hash(deckId, date);
        }

        /**
         * Compares this composite key with another object for equality.
         *
         * @param o the object to compare with
         * @return true if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id other)) return false;
            return java.util.Objects.equals(deckId, other.deckId) && java.util.Objects.equals(date, other.date);
        }
    }

    /**
     * Composite primary key for this daily statistics record.
     * Ensures one record per deck per calendar day.
     */
    @EmbeddedId
    private Id id;

    /**
     * Number of practice sessions for this deck on the specified date.
     * Tracks user engagement and learning frequency patterns.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int sessions;

    /**
     * Number of cards viewed during practice sessions on the specified date.
     * Counts total cards presented to user for learning activity and content coverage measurement.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int viewed;

    /**
     * Number of correct answers given during practice sessions on the specified date.
     * Key performance indicator for learning effectiveness and knowledge retention.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int correct;

    /**
     * Number of times cards were repeated during practice sessions on the specified date.
     * Tracks repetition frequency for spaced repetition algorithms and learning patterns.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int repeatCount;

    /**
     * Number of cards marked as "hard" during practice sessions on the specified date.
     * Tracks user difficulty ratings for content optimization and attention areas.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int hard;

    /**
     * Total duration of all practice sessions in milliseconds for the specified date.
     * Measures time spent practicing for engagement and learning intensity insights.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalDurationMs;

    /**
     * Total delay in milliseconds before answering cards during practice sessions.
     * Measures response time patterns for user confidence and content difficulty insights.
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalAnswerDelayMs;

    /**
     * Version number for optimistic locking.
     * Implements optimistic locking to prevent concurrent modification conflicts.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Timestamp when this statistics record was created.
     * Records when daily statistics record was first created for audit trail and data lineage.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    /**
     * Timestamp when this statistics record was last updated.
     * Records when daily statistics record was last modified for audit trail and change tracking.
     */
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback method executed before persisting a new entity.
     * Automatically called by JPA framework to ensure both creation and update timestamps are initialized.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    /**
     * JPA lifecycle callback method executed before updating an existing entity.
     * Automatically called by JPA framework to ensure update timestamp reflects modification time.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and setters
    /**
     * Gets the composite primary key for this daily statistics record.
     *
     * @return the composite primary key, never null for persisted entities
     */
    public Id getId() {
        return id;
    }

    /**
     * Sets the composite primary key for this daily statistics record.
     *
     * @param id the composite primary key to set, must not be null
     * @throws IllegalArgumentException if id is null
     */
    public void setId(Id id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }

    /**
     * Gets the number of practice sessions for this deck on the specified date.
     *
     * @return the number of practice sessions, always non-negative
     */
    public int getSessions() {
        return sessions;
    }

    /**
     * Sets the number of practice sessions for this deck on the specified date.
     *
     * @param sessions the number of practice sessions to set, must be non-negative
     * @throws IllegalArgumentException if sessions is negative
     */
    public void setSessions(int sessions) {
        if (sessions < 0) {
            throw new IllegalArgumentException("Sessions cannot be negative");
        }
        this.sessions = sessions;
    }

    /**
     * Gets the number of cards viewed during practice sessions on the specified date.
     *
     * @return the number of viewed cards, always non-negative
     */
    public int getViewed() {
        return viewed;
    }

    /**
     * Sets the number of cards viewed during practice sessions on the specified date.
     *
     * @param viewed the number of viewed cards to set, must be non-negative
     * @throws IllegalArgumentException if viewed is negative
     */
    public void setViewed(int viewed) {
        if (viewed < 0) {
            throw new IllegalArgumentException("Viewed cannot be negative");
        }
        this.viewed = viewed;
    }

    /**
     * Gets the number of correct answers given during practice sessions on the specified date.
     *
     * @return the number of correct answers, always non-negative
     */
    public int getCorrect() {
        return correct;
    }

    /**
     * Sets the number of correct answers given during practice sessions on the specified date.
     *
     * @param correct the number of correct answers to set, must be non-negative
     * @throws IllegalArgumentException if correct is negative
     */
    public void setCorrect(int correct) {
        if (correct < 0) {
            throw new IllegalArgumentException("Correct cannot be negative");
        }
        this.correct = correct;
    }

    /**
     * Gets the number of times cards were repeated during practice sessions on the specified date.
     *
     * @return the number of repeated cards, always non-negative
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Sets the number of times cards were repeated during practice sessions on the specified date.
     *
     * @param repeatCount the number of repeated cards to set, must be non-negative
     * @throws IllegalArgumentException if repeatCount is negative
     */
    public void setRepeatCount(int repeatCount) {
        if (repeatCount < 0) {
            throw new IllegalArgumentException("Repeat count cannot be negative");
        }
        this.repeatCount = repeatCount;
    }

    /**
     * Gets the number of cards marked as "hard" during practice sessions on the specified date.
     *
     * @return the number of hard cards, always non-negative
     */
    public int getHard() {
        return hard;
    }

    /**
     * Sets the number of cards marked as "hard" during practice sessions on the specified date.
     *
     * @param hard the number of hard cards to set, must be non-negative
     * @throws IllegalArgumentException if hard is negative
     */
    public void setHard(int hard) {
        if (hard < 0) {
            throw new IllegalArgumentException("Hard cannot be negative");
        }
        this.hard = hard;
    }

    /**
     * Gets the total duration of all practice sessions in milliseconds for the specified date.
     *
     * @return the total duration in milliseconds, always non-negative
     */
    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /**
     * Sets the total duration of all practice sessions in milliseconds for the specified date.
     *
     * @param totalDurationMs the total duration in milliseconds to set, must be non-negative
     * @throws IllegalArgumentException if totalDurationMs is negative
     */
    public void setTotalDurationMs(long totalDurationMs) {
        if (totalDurationMs < 0) {
            throw new IllegalArgumentException("Total duration cannot be negative");
        }
        this.totalDurationMs = totalDurationMs;
    }

    /**
     * Gets the total delay in milliseconds before answering cards during practice sessions.
     *
     * @return the total answer delay in milliseconds, always non-negative
     */
    public long getTotalAnswerDelayMs() {
        return totalAnswerDelayMs;
    }

    /**
     * Sets the total delay in milliseconds before answering cards during practice sessions.
     *
     * @param totalAnswerDelayMs the total answer delay in milliseconds to set, must be non-negative
     * @throws IllegalArgumentException if totalAnswerDelayMs is negative
     */
    public void setTotalAnswerDelayMs(long totalAnswerDelayMs) {
        if (totalAnswerDelayMs < 0) {
            throw new IllegalArgumentException("Total answer delay cannot be negative");
        }
        this.totalAnswerDelayMs = totalAnswerDelayMs;
    }

    /**
     * Gets the version number for optimistic locking.
     *
     * @return the version number, may be null for new entities
     */
    @SuppressWarnings("unused") // IDE Community problem
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version number for optimistic locking.
     *
     * @param version the version number to set
     */
    @SuppressWarnings("unused") // IDE Community problem
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Gets the timestamp when this statistics record was created.
     *
     * @return the creation timestamp, never null for persisted entities
     */
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when this statistics record was created.
     *
     * <p>This method allows manual setting of the creation timestamp,
     * though it is typically managed automatically by the JPA lifecycle
     * callbacks. Use with caution to avoid disrupting the audit trail.</p>
     *
     * @param createdAt the creation timestamp to set, must not be null
     * @throws IllegalArgumentException if createdAt is null
     */
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at timestamp cannot be null");
        }
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this statistics record was last updated.
     *
     * @return the last update timestamp, never null for persisted entities
     */
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when this statistics record was last updated.
     *
     * <p>This method allows manual setting of the update timestamp,
     * though it is typically managed automatically by the JPA lifecycle
     * callbacks. Use with caution to avoid disrupting the audit trail.</p>
     *
     * @param updatedAt the update timestamp to set, must not be null
     * @throws IllegalArgumentException if updatedAt is null
     */
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("Updated at timestamp cannot be null");
        }
        this.updatedAt = updatedAt;
    }
}
