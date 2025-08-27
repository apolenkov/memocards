package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a flashcard deck with metadata and timestamp management.
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
     * @param idValue the unique identifier for this deck
     */
    public void setId(final Long idValue) {
        this.id = idValue;
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
     * @param userIdValue the user ID that owns this deck
     */
    public void setUserId(final Long userIdValue) {
        this.userId = userIdValue;
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
     * @param titleValue the deck title to set
     */
    public void setTitle(final String titleValue) {
        this.title = titleValue;
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
     * @param descriptionValue the deck description to set
     */
    public void setDescription(final String descriptionValue) {
        this.description = descriptionValue;
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
     * @param createdAtValue the creation timestamp to set
     */
    public void setCreatedAt(final LocalDateTime createdAtValue) {
        this.createdAt = createdAtValue;
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
     * @param updatedAtValue the last modification timestamp to set
     */
    public void setUpdatedAt(final LocalDateTime updatedAtValue) {
        this.updatedAt = updatedAtValue;
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
        if (createdAt == null) {
            createdAt = now;
        }
        // Initialize update timestamp if not set
        if (updatedAt == null) {
            updatedAt = now;
        }
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
