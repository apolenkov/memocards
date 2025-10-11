package org.apolenkov.application.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.News;

/**
 * Domain port for managing news and announcements.
 *
 * <p>Defines contract for CRUD operations on news items
 * displayed on application landing page.</p>
 */
public interface NewsRepository {

    /**
     * Retrieves all news items ordered by creation date.
     *
     * @return list of news items sorted newest first
     */
    List<News> findAllOrderByCreatedDesc();

    /**
     * Finds news item by identifier.
     *
     * @param id news item identifier
     * @return news item if found, empty otherwise
     */
    Optional<News> findById(long id);

    /**
     * Saves news item (creates new or updates existing).
     *
     * @param item news item to save
     */
    void save(News item);

    /**
     * Saves multiple news items in batch operation.
     * More efficient than calling save() multiple times.
     *
     * @param items collection of news items to save
     */
    void saveAll(Collection<News> items);

    /**
     * Deletes news item by identifier.
     *
     * @param id news item identifier to delete
     */
    void deleteById(long id);
}
