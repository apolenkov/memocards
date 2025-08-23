package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.News;

/**
 * Domain port for managing news and announcements.
 *
 * <p>Defines the contract for CRUD operations on news items
 * displayed on the application landing page.</p>
 */
public interface NewsRepository {

    /**
     * Retrieves all news items ordered by creation date.
     *
     * @return list of news items sorted newest first
     */
    List<News> findAllOrderByCreatedDesc();

    /**
     * Finds a news item by its identifier.
     *
     * @param id the news item identifier
     * @return news item if found, empty otherwise
     */
    Optional<News> findById(Long id);

    /**
     * Saves a news item (creates new or updates existing).
     *
     * @param item the news item to save
     * @return the saved news item with generated ID
     */
    News save(News item);

    /**
     * Deletes a news item by its identifier.
     *
     * @param id the news item identifier to delete
     */
    void deleteById(Long id);
}
