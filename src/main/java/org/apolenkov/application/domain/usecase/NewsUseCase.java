package org.apolenkov.application.domain.usecase;

import java.util.List;
import org.apolenkov.application.model.News;

/**
 * Core business operations for managing news and announcements.
 */
public interface NewsUseCase {
    /**
     * Gets all news items ordered by creation date (newest first).
     *
     * @return list of all news items
     */
    List<News> getAllNews();

    /**
     * Creates new news item.
     *
     * @param title headline or title of news item
     * @param content main text content of news item
     * @param author name of person who wrote news
     * @throws IllegalArgumentException if title or content is null or empty
     */
    void createNews(String title, String content, String author);

    /**
     * Updates existing news item.
     *
     * @param id unique identifier of news item to update
     * @param title new headline or title
     * @param content new main text content
     * @param author new author name
     * @throws IllegalArgumentException if title or content is null or empty, or if news item not found
     */
    void updateNews(long id, String title, String content, String author);

    /**
     * Deletes news item by ID.
     *
     * @param id unique identifier of news item to delete
     */
    void deleteNews(long id);
}
