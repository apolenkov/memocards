package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing news articles in the system.
 *
 * <p>This entity manages news content that can be displayed to users, including
 * announcements, updates, and informational articles. It provides a structured
 * way to store and retrieve news items with proper metadata and content management.</p>
 *
 * <p>The entity maintains the following characteristics:</p>
 * <ul>
 *   <li><strong>Content Management:</strong> Title, content, and author information</li>
 *   <li><strong>Temporal Tracking:</strong> Creation and modification timestamps</li>
 *   <li><strong>Content Storage:</strong> Large text content using LOB annotation</li>
 *   <strong>Audit Trail:</strong> Automatic timestamp management for changes</li>
 * </ul>
 *
 * <p><strong>Database Mapping:</strong></p>
 * <ul>
 *   <li>Table: "news"</li>
 *   <li>Primary Key: Auto-generated ID</li>
 *   <li>Content Field: Uses LOB for large text storage</li>
 *   <li>Timestamps: Automatic management via JPA lifecycle callbacks</li>
 * </ul>
 *
 * <p><strong>Lifecycle Management:</strong></p>
 * <ul>
 *   <li><strong>Creation:</strong> Automatic timestamp setting on persist</li>
 *   <li><strong>Updates:</strong> Automatic timestamp updating on modifications</li>
 *   <strong>Validation:</strong> Required fields enforced at database level</li>
 *   <strong>Content Handling:</strong> Large content supported via LOB</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong></p>
 * <ul>
 *   <li><strong>Content Requirements:</strong> Title and content are mandatory</li>
 *   <li><strong>Author Attribution:</strong> All news must have an author</li>
 *   <strong>Temporal Integrity:</strong> Timestamps automatically managed</li>
 *   <strong>Content Size:</strong> Large content supported for detailed articles</li>
 * </ul>
 *
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 * @see jakarta.persistence.Id
 * @see jakarta.persistence.GeneratedValue
 * @see jakarta.persistence.Column
 * @see jakarta.persistence.Lob
 * @see jakarta.persistence.PrePersist
 * @see jakarta.persistence.PreUpdate
 * @see java.time.LocalDateTime
 */
@Entity
@Table(name = "news")
public class NewsEntity {

    /**
     * Unique identifier for the news article.
     *
     * <p>This field serves as the primary key and is automatically generated
     * using the database's identity strategy. It provides a unique reference
     * for each news article in the system.</p>
     *
     * <p><strong>Generation Strategy:</strong> IDENTITY (auto-increment)</p>
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Primary key, non-nullable, unique</p>
     * <p><strong>Purpose:</strong> Enables efficient querying and referencing of news articles</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the news article.
     *
     * <p>This field stores the headline or title of the news article.
     * It provides a concise summary of the content and is used for
     * display purposes in news listings and navigation.</p>
     *
     * <p><strong>Database Type:</strong> VARCHAR</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Purpose:</strong> Article identification and display</p>
     * <p><strong>Business Rule:</strong> Must provide meaningful article identification</p>
     */
    @Column(nullable = false)
    private String title;

    /**
     * Main content of the news article.
     *
     * <p>This field stores the full text content of the news article.
     * It uses the LOB annotation to support large text content,
     * enabling detailed articles with rich formatting and extensive content.</p>
     *
     * <p><strong>Database Type:</strong> LOB (Large Object)</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Content Support:</strong> Large text, HTML, markdown</p>
     * <p><strong>Purpose:</strong> Stores the complete article content</p>
     * <p><strong>Business Rule:</strong> Must contain meaningful article content</p>
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * Author of the news article.
     *
     * <p>This field identifies the person or entity responsible for
     * creating the news content. It provides attribution and accountability
     * for published articles.</p>
     *
     * <p><strong>Database Type:</strong> VARCHAR</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Content:</strong> Author name or identifier</p>
     * <p><strong>Purpose:</strong> Content attribution and accountability</p>
     * <p><strong>Business Rule:</strong> Must identify the content creator</p>
     */
    @Column(nullable = false)
    private String author;

    /**
     * Timestamp when the news article was created.
     *
     * <p>This field records the exact date and time when the news article
     * was first created and persisted in the system. It is automatically
     * set during the persist operation.</p>
     *
     * <p><strong>Database Type:</strong> TIMESTAMP</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Auto-Setting:</strong> Set automatically on persist</p>
     * <p><strong>Format:</strong> ISO 8601 datetime format</p>
     * <p><strong>Purpose:</strong> Creation date tracking and ordering</p>
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the news article was last updated.
     *
     * <p>This field records the exact date and time when the news article
     * was last modified. It is automatically updated during each update
     * operation to maintain an accurate modification history.</p>
     *
     * <p><strong>Database Type:</strong> TIMESTAMP</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Auto-Updating:</strong> Updated automatically on each modification</p>
     * <p><strong>Format:</strong> ISO 8601 datetime format</p>
     * <p><strong>Purpose:</strong> Modification tracking and audit trail</p>
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
