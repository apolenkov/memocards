package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a flashcard with content, metadata, and timestamp management.
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

    /**
     * Gets the primary key identifier.
     *
     * @return the unique identifier for this flashcard
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier.
     *
     * @param id the unique identifier for this flashcard
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the deck identifier.
     *
     * @return the deck ID this flashcard belongs to
     */
    public Long getDeckId() {
        return deckId;
    }

    /**
     * Sets the deck identifier.
     *
     * @param deckId the deck ID this flashcard belongs to
     */
    public void setDeckId(Long deckId) {
        this.deckId = deckId;
    }

    /**
     * Gets the front side text.
     *
     * @return the question or prompt text
     */
    public String getFrontText() {
        return frontText;
    }

    /**
     * Sets the front side text.
     *
     * <p>Automatically updates the modification timestamp when the front text
     * is changed to maintain accurate change tracking.</p>
     *
     * @param frontText the question or prompt text to set
     */
    public void setFrontText(String frontText) {
        this.frontText = frontText;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the back side text.
     *
     * @return the answer or explanation text
     */
    public String getBackText() {
        return backText;
    }

    /**
     * Sets the back side text.
     *
     * <p>Automatically updates the modification timestamp when the back text
     * is changed to maintain accurate change tracking.</p>
     *
     * @param backText the answer or explanation text to set
     */
    public void setBackText(String backText) {
        this.backText = backText;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the optional example text.
     *
     * @return the example sentence or usage context
     */
    public String getExample() {
        return example;
    }

    /**
     * Sets the optional example text.
     *
     * <p>Automatically updates the modification timestamp when the example
     * is changed to maintain accurate change tracking.</p>
     *
     * @param example the example sentence or usage context to set
     */
    public void setExample(String example) {
        this.example = example;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the optional image URL.
     *
     * @return the URL to an associated image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the optional image URL.
     *
     * <p>Automatically updates the modification timestamp when the image URL
     * is changed to maintain accurate change tracking.</p>
     *
     * @param imageUrl the URL to an associated image to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        // Automatically update modification timestamp when content changes
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the creation timestamp.
     *
     * @return when this flashcard was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt when this flashcard was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return when this flashcard was last modified
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt when this flashcard was last modified
     */
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
