package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * JPA entity representing news articles in the system.
 *
 * <p>Manages news content with title, content, author, and timestamps.</p>
 */
@Entity
@Table(name = "news")
public class NewsEntity {

    /**
     * Unique identifier for the news article.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the news article.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Main content of the news article.
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * Author of the news article.
     */
    @Column(nullable = false)
    private String author;

    /**
     * Timestamp when the news article was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the news article was last updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback method executed before persisting a new entity.
     * Initializes both creation and update timestamps.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * JPA lifecycle callback method executed before updating an existing entity.
     * Updates modification timestamp to current time.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the unique identifier for this news article.
     *
     * @return the unique identifier, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this news article.
     *
     * @param idValue the unique identifier to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Gets the title of the news article.
     *
     * @return the article title, never null for persisted entities
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the news article with validation.
     *
     * @param titleValue the article title to set, must not be null or empty
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(final String titleValue) {
        if (titleValue == null || titleValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = titleValue.trim();
    }

    /**
     * Gets the main content of the news article.
     *
     * @return the article content, never null for persisted entities
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the main content of the news article with null handling.
     *
     * @param contentValue the article content to set, null will be converted to empty string
     */
    public void setContent(final String contentValue) {
        this.content = contentValue != null ? contentValue : "";
    }

    /**
     * Gets the author of the news article.
     *
     * @return the article author, never null for persisted entities
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the news article with validation.
     *
     * @param authorValue the article author to set, must not be null or empty
     * @throws IllegalArgumentException if author is null or empty
     */
    public void setAuthor(final String authorValue) {
        if (authorValue == null || authorValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        this.author = authorValue.trim();
    }

    /**
     * Gets the timestamp when the news article was created.
     *
     * @return the creation timestamp, never null for persisted entities
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the news article was created (use with caution).
     *
     * @param createdAtValue the creation timestamp to set, must not be null
     * @throws IllegalArgumentException if createdAt is null
     */
    public void setCreatedAt(final LocalDateTime createdAtValue) {
        if (createdAtValue == null) {
            throw new IllegalArgumentException("Created at timestamp cannot be null");
        }
        this.createdAt = createdAtValue;
    }

    /**
     * Gets the timestamp when the news article was last updated.
     *
     * @return the last update timestamp, never null for persisted entities
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the news article was last updated (use with caution).
     *
     * @param updatedAtValue the update timestamp to set, null will be converted to current time
     */
    public void setUpdatedAt(final LocalDateTime updatedAtValue) {
        this.updatedAt = updatedAtValue != null ? updatedAtValue : LocalDateTime.now();
    }
}
