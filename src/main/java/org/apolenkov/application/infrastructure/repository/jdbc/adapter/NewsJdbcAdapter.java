package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.NewsDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.NewsPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.NewsRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.NewsSqlQueries;
import org.apolenkov.application.model.News;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for news repository operations.
 *
 * <p>Implements NewsRepository using direct JDBC operations.
 * Provides CRUD operations for news articles.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod", "test"})
@Repository
public class NewsJdbcAdapter implements NewsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsJdbcAdapter.class);
    /**
     * RowMapper for NewsDto.
     */
    private static final RowMapper<NewsDto> NEWS_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        String author = rs.getString("author");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return NewsDto.forExistingNews(
                id,
                title,
                content,
                author,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null);
    };

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public NewsJdbcAdapter(final JdbcTemplate jdbcTemplateValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        this.jdbcTemplate = jdbcTemplateValue;
    }

    /**
     * Converts NewsDto to domain News model.
     *
     * @param newsDto DTO to convert
     * @return corresponding domain model
     */
    private static News toModel(final NewsDto newsDto) {
        final News news =
                new News(newsDto.id(), newsDto.title(), newsDto.content(), newsDto.author(), newsDto.createdAt());
        news.setUpdatedAt(newsDto.updatedAt());
        return news;
    }

    /**
     * Retrieves all news items ordered by creation date (newest first).
     *
     * @return list of all news items ordered by creation date
     */
    @Override
    public List<News> findAllOrderByCreatedDesc() {
        LOGGER.debug("Retrieving all news ordered by creation date");
        try {
            List<NewsDto> newsDtos =
                    jdbcTemplate.query(NewsSqlQueries.SELECT_ALL_NEWS_ORDER_BY_CREATED_DESC, NEWS_ROW_MAPPER);
            return newsDtos.stream().map(NewsJdbcAdapter::toModel).toList();
        } catch (DataAccessException e) {
            throw new NewsRetrievalException("Failed to retrieve all news", e);
        }
    }

    /**
     * Retrieves a news item by its unique identifier.
     *
     * @param id the unique identifier of the news item
     * @return Optional containing the news if found
     */
    @Override
    public Optional<News> findById(final long id) {
        LOGGER.debug("Retrieving news by ID: {}", id);
        try {
            List<NewsDto> newsList = jdbcTemplate.query(NewsSqlQueries.SELECT_NEWS_BY_ID, NEWS_ROW_MAPPER, id);
            if (newsList.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(newsList.getFirst()));
        } catch (DataAccessException e) {
            throw new NewsRetrievalException("Failed to retrieve news by ID: " + id, e);
        }
    }

    /**
     * Saves a news item to the database.
     *
     * @param news the news item to save
     * @throws IllegalArgumentException if news is null
     */
    @Override
    public void save(final News news) {
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }

        LOGGER.debug("Saving news: {}", news.getTitle());
        try {
            if (news.getId() == null) {
                createNews(news);
            } else {
                updateNews(news);
            }
        } catch (DataAccessException e) {
            throw new NewsPersistenceException("Failed to save news: " + news.getTitle(), e);
        }
    }

    /**
     * Deletes a news item by its unique identifier.
     *
     * @param id the unique identifier of the news item to delete
     */
    @Override
    public void deleteById(final long id) {
        LOGGER.debug("Deleting news by ID: {}", id);
        try {
            int deleted = jdbcTemplate.update(NewsSqlQueries.DELETE_NEWS, id);
            if (deleted == 0) {
                LOGGER.warn("No news found with ID: {}", id);
            }
        } catch (DataAccessException e) {
            throw new NewsPersistenceException("Failed to delete news by ID: " + id, e);
        }
    }

    /**
     * Creates new news article in database.
     *
     * @param news news article to create
     */
    private void createNews(final News news) {
        NewsDto newsDto = NewsDto.forNewNews(news.getTitle(), news.getContent(), news.getAuthor());

        // Insert news and get generated ID using RETURNING clause
        Long generatedId = jdbcTemplate.queryForObject(
                NewsSqlQueries.INSERT_NEWS_RETURNING_ID,
                Long.class,
                newsDto.title(),
                newsDto.content(),
                newsDto.author(),
                newsDto.createdAt(),
                newsDto.updatedAt());

        // Return created news
        NewsDto createdDto = NewsDto.forExistingNews(
                generatedId,
                newsDto.title(),
                newsDto.content(),
                newsDto.author(),
                newsDto.createdAt(),
                newsDto.updatedAt());

        toModel(createdDto);
    }

    /**
     * Updates existing news article in database.
     *
     * @param news news article to update
     */
    private void updateNews(final News news) {
        // Update news
        jdbcTemplate.update(
                NewsSqlQueries.UPDATE_NEWS,
                news.getTitle(),
                news.getContent(),
                news.getAuthor(),
                LocalDateTime.now(), // Update timestamp
                news.getId());
    }
}
