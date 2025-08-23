package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
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
     *
     * <p>This method is automatically called by the JPA framework before
     * a new news article is persisted to the database. It ensures that
     * both creation and update timestamps are properly initialized.</p>
     *
     * <p><strong>Execution:</strong> Automatic, before persist operation</p>
     * <p><strong>Purpose:</strong> Initialize timestamps for new entities</p>
     * <p><strong>Behavior:</strong> Sets both createdAt and updatedAt to current time</p>
     * <p><strong>Framework:</strong> Called by JPA lifecycle management</p>
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * JPA lifecycle callback method executed before updating an existing entity.
     *
     * <p>This method is automatically called by the JPA framework before
     * an existing news article is updated in the database. It ensures that
     * the update timestamp reflects the most recent modification time.</p>
     *
     * <p><strong>Execution:</strong> Automatic, before update operation</p>
     * <p><strong>Purpose:</strong> Update modification timestamp</p>
     * <p><strong>Behavior:</strong> Sets updatedAt to current time</p>
     * <p><strong>Framework:</strong> Called by JPA lifecycle management</p>
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
     * <p>This method is typically called by the JPA framework during
     * entity lifecycle management. Manual setting should be avoided
     * to prevent conflicts with the auto-generation strategy.</p>
     *
     * @param id the unique identifier to set
     */
    public void setId(Long id) {
        this.id = id;
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
     * Sets the title of the news article.
     *
     * <p>This method allows setting or updating the headline or title
     * of the news article. The title should be descriptive and provide
     * a clear indication of the article's content.</p>
     *
     * @param title the article title to set, must not be null or empty
     * @throws IllegalArgumentException if title is null or empty
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
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
     * Sets the main content of the news article.
     *
     * <p>This method allows setting or updating the full text content
     * of the news article. The content can include rich formatting,
     * HTML markup, or plain text as needed.</p>
     *
     * <p>If content is null, it will be treated as empty content.</p>
     *
     * @param content the article content to set, null will be converted to empty string
     */
    public void setContent(String content) {
        this.content = content != null ? content : "";
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
     * Sets the author of the news article.
     *
     * <p>This method allows setting or updating the author attribution
     * for the news article. The author should be clearly identified
     * to provide proper content attribution.</p>
     *
     * @param author the article author to set, must not be null or empty
     * @throws IllegalArgumentException if author is null or empty
     */
    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        this.author = author.trim();
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
     * Sets the timestamp when the news article was created.
     *
     * <p>This method allows manual setting of the creation timestamp,
     * though it is typically managed automatically by the JPA lifecycle
     * callbacks. Use with caution to avoid disrupting the audit trail.</p>
     *
     * @param createdAt the creation timestamp to set, must not be null
     * @throws IllegalArgumentException if createdAt is null
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at timestamp cannot be null");
        }
        this.createdAt = createdAt;
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
     * Sets the timestamp when the news article was last updated.
     *
     * <p>This method allows manual setting of the update timestamp,
     * though it is typically managed automatically by the JPA lifecycle
     * callbacks. Use with caution to avoid disrupting the audit trail.</p>
     *
     * <p>If updatedAt is null, it will be set to current time.</p>
     *
     * @param updatedAt the update timestamp to set, null will be converted to current time
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
}
