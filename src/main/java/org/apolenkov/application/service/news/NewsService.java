package org.apolenkov.application.service.news;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.domain.usecase.NewsUseCase;
import org.apolenkov.application.model.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for news use cases and business operations.
 */
@Service
@Transactional
public class NewsService implements NewsUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    private final NewsRepository newsRepository;

    /**
     * Creates NewsService with required repository dependency.
     *
     * @param newsRepositoryValue repository for persisting and retrieving news items
     */
    public NewsService(final NewsRepository newsRepositoryValue) {
        this.newsRepository = newsRepositoryValue;
    }

    /**
     * Gets all news items ordered by creation date (newest first).
     *
     * @return list of all news items, ordered by creation date descending
     */
    @Override
    public List<News> getAllNews() {
        return newsRepository.findAllOrderByCreatedDesc();
    }

    /**
     * Creates new news item with automatic timestamp and validation.
     *
     * @param title headline or title of the news item
     * @param content main text content of the news item
     * @param author name of the person who wrote the news
     * @throws IllegalArgumentException if title or content is null or empty
     */
    @Override
    public void createNews(final String title, final String content, final String author) {
        LOGGER.debug("Creating news: title='{}', author={}", title, author);

        validate(title, content);

        News news = new News(null, title, content, author, LocalDateTime.now());
        newsRepository.save(news);

        AUDIT_LOGGER.info("News created: title='{}', author={}, contentLength={}", title, author, content.length());
    }

    /**
     * Updates existing news item with automatic timestamp update and validation.
     *
     * @param id unique identifier of news item to update
     * @param title new headline or title for news item
     * @param content new main text content for news item
     * @param author new author name for news item
     * @throws IllegalArgumentException if title or content is null or empty, or if news item not found
     */
    @Override
    public void updateNews(final long id, final String title, final String content, final String author) {
        LOGGER.debug("Updating news: id={}, title='{}'", id, title);

        Optional<News> existingOpt = newsRepository.findById(id);
        if (existingOpt.isEmpty()) {
            LOGGER.warn("Attempted to update non-existent news: id={}", id);
            throw new IllegalArgumentException("News not found with id: " + id);
        }

        validate(title, content);

        News existing = existingOpt.get();
        String oldTitle = existing.getTitle();

        existing.setTitle(title);
        existing.setContent(content);
        existing.setAuthor(author);
        existing.setUpdatedAt(LocalDateTime.now());

        newsRepository.save(existing);

        AUDIT_LOGGER.info("News updated: id={}, titleChanged='{}' -> '{}', author={}", id, oldTitle, title, author);
    }

    /**
     * Deletes news item by ID (no error if item doesn't exist).
     *
     * @param id unique identifier of news item to delete
     */
    @Override
    public void deleteNews(final long id) {
        LOGGER.debug("Deleting news: id={}", id);

        Optional<News> newsOpt = newsRepository.findById(id);
        if (newsOpt.isEmpty()) {
            LOGGER.warn("Attempted to delete non-existent news: id={}", id);
            return;
        }

        News news = newsOpt.get();
        newsRepository.deleteById(id);

        AUDIT_LOGGER.warn("News deleted: id={}, title='{}', author={}", id, news.getTitle(), news.getAuthor());
    }

    /**
     * Validates news content before creation or update.
     *
     * @param title title to validate
     * @param content content to validate
     * @throws IllegalArgumentException if title or content is null or empty after trimming
     */
    private static void validate(final String title, final String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }
    }
}
