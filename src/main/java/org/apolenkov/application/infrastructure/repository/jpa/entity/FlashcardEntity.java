package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a flashcard in the database.
 *
 * <p>This entity maps to the "flashcards" table and represents an individual
 * flashcard within a deck. It contains the card content (front/back text),
 * optional example and image URL, and metadata for tracking creation and
 * modification times.</p>
 *
 * <p>The entity provides automatic timestamp management through JPA lifecycle
 * callbacks and includes proper equality and hash code implementations
 * based on the primary key.</p>
 */
@Entity
@Table(name = "flashcards")
public class FlashcardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long deckId;

    @Column(nullable = false, length = 300)
    private String frontText;

    @Column(nullable = false, length = 300)
    private String backText;

    @Column(length = 500)
    private String example;

    @Column(length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeckId() {
        return deckId;
    }

    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    public String getBackText() {
        return backText;
    }

    public void setBackText(String backText) {
        this.backText = backText;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * JPA lifecycle callback executed before entity persistence.
     *
     * <p>Automatically sets creation and update timestamps if they haven't
     * been explicitly set. This ensures all new flashcards have proper
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
     * the flashcard content is modified, ensuring accurate change tracking.</p>
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Compares this entity with another object for equality.
     *
     * <p>Two FlashcardEntity instances are considered equal if they have
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
        FlashcardEntity that = (FlashcardEntity) o;
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
