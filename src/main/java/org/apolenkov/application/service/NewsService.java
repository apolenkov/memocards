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
 * <p>Provides CRUD operations for news items including creation, updates, deletion, and retrieval.
 * Handles validation of news content and automatically manages timestamps for tracking.</p>
 */
@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;

    /**
     * Creates NewsService with required repository dependency.
     *
     * @param newsRepository repository for persisting and retrieving news items
     */
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Retrieves all news items ordered by creation date (newest first).
     *
     * <p>Returns list of all news items in system, sorted with most recently
     * created items appearing first. Used for displaying news on landing page.</p>
     *
     * @return list of all news items, ordered by creation date descending
     */
    public List<News> getAllNews() {
        return newsRepository.findAllOrderByCreatedDesc();
    }

    /**
     * Creates new news item.
     *
     * <p>Creates and persists new news item with specified title, content, and author.
     * Creation timestamp is automatically set to current time.</p>
     *
     * @param title headline or title of the news item
     * @param content main text content of the news item
     * @param author name of the person who wrote the news
     * @throws IllegalArgumentException if title or content is null or empty
     */
    public void createNews(String title, String content, String author) {
        validate(title, content);
        News news = new News(null, title, content, author, LocalDateTime.now());
        newsRepository.save(news);
    }

    /**
     * Updates existing news item.
     *
     * <p>Updates title, content, and author of existing news item identified by ID.
     * Modification timestamp is automatically updated to current time.</p>
     *
     * @param id unique identifier of news item to update
     * @param title new headline or title for news item
     * @param content new main text content for news item
     * @param author new author name for news item
     * @throws IllegalArgumentException if title or content is null or empty, or if news item not found
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
     * Deletes news item by ID.
     *
     * <p>Removes specified news item from system. If news item does not exist,
     * operation completes without error.</p>
     *
     * @param id unique identifier of news item to delete
     */
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    /**
     * Validates news content before creation or update.
     *
     * <p>Performs validation checks on title and content fields to ensure they meet
     * minimum requirements. Both fields must be non-null and contain non-empty text after trimming.</p>
     *
     * @param title title to validate
     * @param content content to validate
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
