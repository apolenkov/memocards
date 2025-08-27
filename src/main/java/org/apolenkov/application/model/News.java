package org.apolenkov.application.model;

import java.time.LocalDateTime;

/**
 * Represents a news item in the application.
 *
 * <p>News items are announcements or informational content displayed to users.
 * Contains title, content, author information, and timestamps for tracking.</p>
 */
public final class News {
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
    public News(
            final Long id,
            final String title,
            final String content,
            final String author,
            final LocalDateTime createdAt) {
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
     * @param idValue news ID to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
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
     * @param titleValue news title to set
     */
    public void setTitle(final String titleValue) {
        this.title = titleValue;
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
     * @param contentValue news content to set
     */
    public void setContent(final String contentValue) {
        this.content = contentValue;
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
     * @param authorValue author name to set
     */
    public void setAuthor(final String authorValue) {
        this.author = authorValue;
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
     * @param createdAtValue creation timestamp to set
     */
    public void setCreatedAt(final LocalDateTime createdAtValue) {
        this.createdAt = createdAtValue;
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
     * @param updatedAtValue modification timestamp to set
     */
    public void setUpdatedAt(final LocalDateTime updatedAtValue) {
        this.updatedAt = updatedAtValue;
    }
}
