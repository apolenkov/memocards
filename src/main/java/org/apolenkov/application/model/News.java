package org.apolenkov.application.model;

import java.time.LocalDateTime;

/**
 * Represents a news item in the application.
 *
 * <p>News items are announcements or informational content that can be displayed
 * to users. They typically contain a title, content, author information, and
 * timestamps for creation and modification tracking.</p>
 *
 * <p>This class is used for displaying site-wide announcements, updates, and
 * other important information to users on the landing page.</p>
 *
 *
 */
public class News {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a news item with all required fields.
     *
     * <p>Initializes both creation and update timestamps to the same value
     * when the news item is first created.</p>
     *
     * @param id the unique identifier for the news item
     * @param title the headline or title of the news
     * @param content the main text content of the news
     * @param author the name of the person who wrote the news
     * @param createdAt the timestamp when the news was created
     */
    public News(Long id, String title, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Gets the unique identifier of the news item.
     *
     * @return the news ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the news item.
     *
     * @param id the news ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the title or headline of the news item.
     *
     * @return the news title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title or headline of the news item.
     *
     * @param title the news title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the main content text of the news item.
     *
     * @return the news content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the main content text of the news item.
     *
     * @param content the news content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the author of the news item.
     *
     * @return the author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the news item.
     *
     * @param author the author name to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the timestamp when the news item was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the news item.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the news item was last modified.
     *
     * @return the last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the modification timestamp of the news item.
     *
     * @param updatedAt the modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
