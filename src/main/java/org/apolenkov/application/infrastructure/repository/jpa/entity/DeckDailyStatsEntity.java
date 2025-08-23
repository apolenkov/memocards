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
     * Composite primary key for daily statistics records.
     *
     * <p>This embeddable class represents the composite primary key consisting
     * of deck identifier and date. It enables efficient daily aggregation of
     * statistics while maintaining referential integrity with deck entities.</p>
     *
     * <p>The composite key design provides:</p>
     * <ul>
     *   <li><strong>Daily Granularity:</strong> One record per deck per day</li>
     *   <li><strong>Efficient Queries:</strong> Optimized for date-range operations</li>
     *   <li><strong>Data Integrity:</strong> Prevents duplicate daily records</li>
     *   <strong>Performance:</strong> Enables effective indexing strategies</strong>
     * </ul>
     *
     * <p><strong>Implementation Details:</strong></p>
     * <ul>
     *   <li><strong>Serializable:</strong> Supports JPA entity serialization</strong>
     *   <li><strong>Equals/HashCode:</strong> Proper implementation for composite keys</strong>
     *   <li><strong>Validation:</strong> Bean validation constraints on key fields</strong>
     *   <strong>Immutability:</strong> Key fields cannot be modified after creation</strong>
     * </ul>
     *
     * @see jakarta.persistence.Embeddable
     * @see jakarta.validation.constraints.NotNull
     * @see java.io.Serializable
     * @see java.time.LocalDate
     */
    @Embeddable
    public static class Id implements Serializable {

        /**
         * Identifier of the deck these statistics belong to.
         *
         * <p>This field establishes the relationship between daily statistics
         * and a specific deck. It enables aggregation and analysis of
         * performance data for individual learning collections.</p>
         *
         * <p><strong>Database Type:</strong> BIGINT</p>
         * <p><strong>Constraints:</strong> Non-nullable, foreign key reference</p>
         * <p><strong>Relationship:</strong> Many-to-one with DeckEntity</p>
         * <p><strong>Business Rule:</strong> Must reference an existing deck</p>
         */
        @NotNull
        private Long deckId;

        /**
         * Calendar date for these daily statistics.
         *
         * <p>This field represents the specific calendar day for which
         * the statistics were collected. It enables daily aggregation
         * and time-series analysis of deck performance.</p>
         *
         * <p><strong>Database Type:</strong> DATE</p>
         * <p><strong>Constraints:</strong> Non-nullable</p>
         * <p><strong>Format:</strong> ISO 8601 date format (YYYY-MM-DD)</p>
         * <p><strong>Purpose:</strong> Daily granularity for statistics</p>
         */
        @NotNull
        private LocalDate date;

        /**
         * Default constructor required by JPA.
         *
         * <p>This constructor is required by the JPA specification for
         * embeddable classes. It should not be used directly in application
         * code.</p>
         */
        public Id() {}

        /**
         * Constructs a composite key with deck identifier and date.
         *
         * <p>This constructor creates a composite key for daily statistics
         * records. It validates that both parameters are non-null to ensure
         * data integrity.</p>
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
         * Sets the deck identifier for this composite key.
         *
         * <p>This method allows modification of the deck identifier,
         * though it should typically be set only during construction
         * to maintain data integrity.</p>
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
         * Sets the calendar date for this composite key.
         *
         * <p>This method allows modification of the calendar date,
         * though it should typically be set only during construction
         * to maintain data integrity.</p>
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
         * <p>This method generates a hash code based on both the deck ID
         * and date fields. It ensures proper behavior when using this
         * class as a key in hash-based collections.</p>
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
         * <p>This method implements equality comparison based on both
         * the deck ID and date fields. Two composite keys are considered
         * equal if they have the same deck ID and date values.</p>
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
     *
     * <p>This field uses the embedded composite key to uniquely identify
     * daily statistics records. The combination of deck ID and date ensures
     * that only one statistics record exists per deck per day.</p>
     *
     * <p><strong>Key Composition:</strong> deck_id + date</p>
     * <p><strong>Uniqueness:</strong> One record per deck per calendar day</p>
     * <p><strong>Purpose:</strong> Primary key and entity identification</p>
     */
    @EmbeddedId
    private Id id;

    /**
     * Number of practice sessions for this deck on the specified date.
     *
     * <p>This field tracks how many times a user practiced with this deck
     * during the calendar day. It provides insights into user engagement
     * and learning frequency patterns.</p>
     *
     * <p><strong>Database Type:</strong> INTEGER</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Business Rule:</strong> Must be non-negative</p>
     * <p><strong>Purpose:</strong> Engagement and usage tracking</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int sessions;

    /**
     * Number of cards viewed during practice sessions on the specified date.
     *
     * <p>This field counts the total number of cards that were presented
     * to the user during practice sessions. It helps measure the scope
     * of learning activity and content coverage.</p>
     *
     * <p><strong>Database Type:</strong> INTEGER</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Business Rule:</strong> Must be non-negative</p>
     * <p><strong>Purpose:</strong> Content coverage measurement</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int viewed;

    /**
     * Number of correct answers given during practice sessions on the specified date.
     *
     * <p>This field tracks the number of correct responses from the user,
     * providing a key performance indicator for learning effectiveness
     * and knowledge retention.</p>
     *
     * <p><strong>Database Type:</strong> INTEGER</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Business Rule:</strong> Must be non-negative, cannot exceed viewed</p>
     * <p><strong>Purpose:</strong> Performance and accuracy measurement</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int correct;

    /**
     * Number of times cards were repeated during practice sessions on the specified date.
     *
     * <p>This field tracks repetition frequency, which is important for
     * spaced repetition algorithms and understanding user learning patterns.
     * Higher repeat counts may indicate challenging content or learning difficulties.</p>
     *
     * <p><strong>Database Type:</strong> INTEGER</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Business Rule:</strong> Must be non-negative</p>
     * <p><strong>Purpose:</strong> Learning difficulty and repetition tracking</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int repeatCount;

    /**
     * Number of cards marked as "hard" during practice sessions on the specified date.
     *
     * <p>This field tracks user difficulty ratings, providing insights into
     * which content areas may need additional attention or different
     * presentation strategies.</p>
     *
     * <p><strong>Database Type:</strong> INTEGER</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Business Rule:</strong> Must be non-negative, cannot exceed viewed</p>
     * <p><strong>Purpose:</strong> Difficulty assessment and content optimization</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private int hard;

    /**
     * Total duration of all practice sessions in milliseconds for the specified date.
     *
     * <p>This field measures the total time spent practicing with the deck,
     * providing insights into user engagement levels and learning intensity.
     * It helps optimize session lengths and content presentation.</p>
     *
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Unit:</strong> Milliseconds</p>
     * <p><strong>Business Rule:</strong> Must be non-negative</p>
     * <p><strong>Purpose:</strong> Engagement and time investment measurement</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalDurationMs;

    /**
     * Total delay in milliseconds before answering cards during practice sessions.
     *
     * <p>This field measures response time patterns, which can indicate
     * user confidence, content difficulty, and learning progress. It helps
     * optimize the spaced repetition algorithm and content presentation.</p>
     *
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Non-nullable, minimum value 0</p>
     * <p><strong>Unit:</strong> Milliseconds</p>
     * <p><strong>Business Rule:</strong> Must be non-negative</p>
     * <p><strong>Purpose:</strong> Response time and confidence measurement</p>
     */
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private long totalAnswerDelayMs;

    /**
     * Version number for optimistic locking.
     *
     * <p>This field implements optimistic locking to prevent concurrent
     * modification conflicts. It is automatically managed by Hibernate
     * and should not be manually modified by application code.</p>
     *
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Management:</strong> Automatic by Hibernate</p>
     * <p><strong>Purpose:</strong> Concurrent access control</p>
     * <p><strong>Behavior:</strong> Incremented on each update</p>
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Timestamp when this statistics record was created.
     *
     * <p>This field records when the daily statistics record was first
     * created in the system. It provides an audit trail for data
     * creation and supports data lineage tracking.</p>
     *
     * <p><strong>Database Type:</strong> TIMESTAMP</p>
     * <p><strong>Constraints:</strong> Non-nullable, not updatable</p>
     * <p><strong>Auto-Setting:</strong> Set automatically on persist</p>
     * <p><strong>Format:</strong> ISO 8601 datetime format</p>
     * <p><strong>Purpose:</strong> Creation audit trail</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    /**
     * Timestamp when this statistics record was last updated.
     *
     * <p>This field records when the daily statistics record was last
     * modified in the system. It provides an audit trail for data
     * modifications and supports change tracking.</p>
     *
     * <p><strong>Database Type:</strong> TIMESTAMP</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Auto-Updating:</strong> Updated automatically on each modification</p>
     * <p><strong>Format:</strong> ISO 8601 datetime format</p>
     * <p><strong>Purpose:</strong> Modification audit trail</p>
     */
    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback method executed before persisting a new entity.
     *
     * <p>This method is automatically called by the JPA framework before
     * a new daily statistics record is persisted to the database. It ensures
     * that both creation and update timestamps are properly initialized.</p>
     *
     * <p><strong>Execution:</strong> Automatic, before persist operation</p>
     * <p><strong>Purpose:</strong> Initialize audit timestamps for new entities</p>
     * <p><strong>Behavior:</strong> Sets both createdAt and updatedAt to current time</p>
     * <p><strong>Framework:</strong> Called by JPA lifecycle management</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }

    /**
     * JPA lifecycle callback method executed before updating an existing entity.
     *
     * <p>This method is automatically called by the JPA framework before
     * an existing daily statistics record is updated in the database. It ensures
     * that the update timestamp reflects the most recent modification time.</p>
     *
     * <p><strong>Execution:</strong> Automatic, before update operation</p>
     * <p><strong>Purpose:</strong> Update modification timestamp</p>
     * <p><strong>Behavior:</strong> Sets updatedAt to current time</p>
     * <p><strong>Framework:</strong> Called by JPA lifecycle management</p>
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
     * <p>This method establishes the identity of the daily statistics record.
     * The composite key should typically be set only during construction
     * to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the session count for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the viewed card count for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the correct answer count for daily
     * statistics. The value must be non-negative and cannot exceed the viewed count.</p>
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
     * <p>This method allows setting or updating the repeat count for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the hard card count for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the total duration for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method allows setting or updating the total answer delay for daily
     * statistics. The value must be non-negative to maintain data integrity.</p>
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
     * <p>This method returns the current version number used for optimistic
     * locking. The version is automatically managed by Hibernate and should
     * not be manually modified.</p>
     *
     * @return the version number, may be null for new entities
     */
    @SuppressWarnings("unused")
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version number for optimistic locking.
     *
     * <p>This method allows setting the version number, though it is typically
     * managed automatically by Hibernate. Manual modification should be avoided
     * to prevent conflicts with the optimistic locking mechanism.</p>
     *
     * @param version the version number to set
     */
    @SuppressWarnings("unused")
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
