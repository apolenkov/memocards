package org.apolenkov.application.model;

import java.time.LocalDateTime;

/**
 * Represents a news item in the application.
 *
 * <p>News items are announcements or informational content displayed to users.
 * Contains title, content, author information, and timestamps for tracking.</p>
 */
public class News {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates news item with all required fields and initializes timestamps.
     *
     * @param id unique identifier for the news item
     * @param title headline or title of the news
     * @param content main text content of the news
     * @param author name of the person who wrote the news
     * @param createdAt timestamp when the news was created
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
     * Gets unique identifier of the news item.
     *
     * @return news ID, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identifier of the news item.
     *
     * @param id news ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title or headline of the news item.
     *
     * @return news title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title or headline of the news item.
     *
     * @param title news title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets main content text of the news item.
     *
     * @return news content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets main content text of the news item.
     *
     * @param content news content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets author of the news item.
     *
     * @return author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets author of the news item.
     *
     * @param author author name to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets timestamp when the news item was created.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets creation timestamp of the news item.
     *
     * @param createdAt creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets timestamp when the news item was last modified.
     *
     * @return last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets modification timestamp of the news item.
     *
     * @param updatedAt modification timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
