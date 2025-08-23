package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a flashcard deck in the database.
 *
 * <p>This entity maps to the "decks" table and represents a collection of
 * flashcards created by a user. It includes metadata such as title, description,
 * and timestamps for creation and modification tracking.</p>
 *
 * <p>The entity provides automatic timestamp management through JPA lifecycle
 * callbacks and includes proper equality and hash code implementations
 * based on the primary key.</p>
 */
@Entity
@Table(name = "decks")
public class DeckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Gets the primary key identifier.
     *
     * @return the unique identifier for this deck
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier.
     *
     * @param id the unique identifier for this deck
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the user who owns this deck.
     *
     * @return the user ID that owns this deck
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who owns this deck.
     *
     * @param userId the user ID that owns this deck
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the title of the deck.
     *
     * @return the deck title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the deck.
     *
     * <p>Automatically updates the modification timestamp when the title
     * is changed to maintain accurate change tracking.</p>
     *
     * @param title the deck title to set
     */
    public void setTitle(String title) {
        this.title = title;
        // Automatically update modification timestamp when title changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the description of the deck.
     *
     * @return the deck description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the deck.
     *
     * <p>Automatically updates the modification timestamp when the description
     * is changed to maintain accurate change tracking.</p>
     *
     * @param description the deck description to set
     */
    public void setDescription(String description) {
        this.description = description;
        // Automatically update modification timestamp when description changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the timestamp when this deck was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when this deck was created.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this deck was last modified.
     *
     * @return the last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when this deck was last modified.
     *
     * @param updatedAt the last modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * JPA lifecycle callback executed before entity persistence.
     *
     * <p>Automatically sets creation and update timestamps if they haven't
     * been explicitly set. This ensures all new entities have proper
     * timestamp values.</p>
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        // Initialize creation timestamp if not set
        if (createdAt == null) createdAt = now;
        // Initialize update timestamp if not set
        if (updatedAt == null) updatedAt = now;
    }

    /**
     * JPA lifecycle callback executed before entity updates.
     *
     * <p>Automatically updates the modification timestamp whenever
     * the entity is modified, ensuring accurate change tracking.</p>
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Compares this entity with another object for equality.
     *
     * <p>Two DeckEntity instances are considered equal if they have
     * the same ID. This is the standard approach for JPA entities
     * where identity is determined by the primary key.</p>
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeckEntity that = (DeckEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Generates a hash code for this entity.
     *
     * <p>The hash code is based on the entity's ID, which is consistent
     * with the equals method implementation.</p>
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
