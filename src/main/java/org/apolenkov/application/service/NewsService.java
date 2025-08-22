package org.apolenkov.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.model.News;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing news and announcements in the application.
 *
 * <p>This service provides CRUD operations for news items, including creation,
 * updates, deletion, and retrieval. It handles validation of news content and
 * automatically manages timestamps for creation and modification tracking.</p>
 *
 * <p>The service is designed to support administrative functions for managing
 * site-wide announcements and informational content displayed to users.</p>
 *
 */
@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;

    /**
     * Constructs a new NewsService with the required repository dependency.
     *
     * @param newsRepository the repository for persisting and retrieving news items
     */
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Retrieves all news items ordered by creation date (newest first).
     *
     * <p>Returns a list of all news items in the system, sorted with the most
     * recently created items appearing first. This method is typically used
     * for displaying news on the landing page or news listing pages.</p>
     *
     * @return a list of all news items, ordered by creation date descending
     */
    public List<News> getAllNews() {
        return newsRepository.findAllOrderByCreatedDesc();
    }

    /**
     * Creates a new news item.
     *
     * <p>Creates and persists a new news item with the specified title, content,
     * and author. The creation timestamp is automatically set to the current time.
     * The method validates that both title and content are provided and non-empty.</p>
     *
     * @param title the headline or title of the news item
     * @param content the main text content of the news item
     * @param author the name of the person who wrote the news
     * @throws IllegalArgumentException if title or content is null or empty
     */
    public void createNews(String title, String content, String author) {
        validate(title, content);
        News news = new News(null, title, content, author, LocalDateTime.now());
        newsRepository.save(news);
    }

    /**
     * Updates an existing news item.
     *
     * <p>Updates the title, content, and author of an existing news item identified
     * by its ID. The modification timestamp is automatically updated to the current time.
     * The method validates that both title and content are provided and non-empty.</p>
     *
     * @param id the unique identifier of the news item to update
     * @param title the new headline or title for the news item
     * @param content the new main text content for the news item
     * @param author the new author name for the news item
     * @throws IllegalArgumentException if title or content is null or empty, or if news item is not found
     */
    public void updateNews(Long id, String title, String content, String author) {
        Optional<News> existingOpt = newsRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("News not found with id: " + id);
        }
        validate(title, content);

        News existing = existingOpt.get();
        existing.setTitle(title);
        existing.setContent(content);
        existing.setAuthor(author);
        existing.setUpdatedAt(LocalDateTime.now());

        newsRepository.save(existing);
    }

    /**
     * Deletes a news item by its ID.
     *
     * <p>Removes the specified news item from the system. If the news item
     * does not exist, the operation completes without error.</p>
     *
     * @param id the unique identifier of the news item to delete
     */
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    /**
     * Validates news content before creation or update.
     *
     * <p>Performs validation checks on the title and content fields to ensure
     * they meet the minimum requirements for a valid news item. Both fields
     * must be non-null and contain non-empty text after trimming.</p>
     *
     * @param title the title to validate
     * @param content the content to validate
     * @throws IllegalArgumentException if title or content is null or empty after trimming
     */
    private static void validate(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }
    }
}
