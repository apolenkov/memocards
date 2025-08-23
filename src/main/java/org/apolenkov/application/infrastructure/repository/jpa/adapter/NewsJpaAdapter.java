package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.NewsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.NewsJpaRepository;
import org.apolenkov.application.model.News;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for news repository operations.
 *
 * <p>Bridges domain layer and JPA persistence for news CRUD operations.
 * Active in dev/prod profiles only.</p>
 */
@Repository
@Profile({"dev", "prod"})
public class NewsJpaAdapter implements NewsRepository {

    private final NewsJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repo the Spring Data JPA repository for news operations
     * @throws IllegalArgumentException if repo is null
     */
    public NewsJpaAdapter(NewsJpaRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("NewsJpaRepository cannot be null");
        }
        this.repo = repo;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param e the JPA entity to convert
     * @return the corresponding domain model
     * @throws IllegalArgumentException if e is null
     */
    private static News toModel(NewsEntity e) {
        if (e == null) {
            throw new IllegalArgumentException("NewsEntity cannot be null");
        }

        News news = new News(e.getId(), e.getTitle(), e.getContent(), e.getAuthor(), e.getCreatedAt());
        news.setUpdatedAt(e.getUpdatedAt());
        return news;
    }

    /**
     * Converts domain model to JPA entity.
     *
     * @param news the domain model to convert
     * @return the corresponding JPA entity
     * @throws IllegalArgumentException if news is null
     */
    private static NewsEntity toEntity(News news) {
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }

        NewsEntity e = new NewsEntity();
        e.setId(news.getId());
        e.setTitle(news.getTitle());
        e.setContent(news.getContent());
        e.setAuthor(news.getAuthor());
        e.setCreatedAt(news.getCreatedAt());
        e.setUpdatedAt(news.getUpdatedAt());
        return e;
    }

    /**
     * Retrieves all news items ordered by creation date (newest first).
     *
     * @return list of all news items ordered by creation date
     */
    @Override
    public List<News> findAllOrderByCreatedDesc() {
        return repo.findAllOrderByCreatedDesc().stream()
                .map(NewsJpaAdapter::toModel)
                .toList();
    }

    /**
     * Retrieves a news item by its unique identifier.
     *
     * @param id the unique identifier of the news item
     * @return Optional containing the news if found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Optional<News> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("News ID cannot be null");
        }
        return repo.findById(id).map(NewsJpaAdapter::toModel);
    }

    /**
     * Saves a news item to the database.
     *
     * @param news the news item to save
     * @return the saved news item with updated fields
     * @throws IllegalArgumentException if news is null
     */
    @Override
    public News save(News news) {
        if (news == null) {
            throw new IllegalArgumentException("News cannot be null");
        }
        return toModel(repo.save(toEntity(news)));
    }

    /**
     * Deletes a news item by its unique identifier.
     *
     * @param id the unique identifier of the news item to delete
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("News ID cannot be null");
        }
        repo.deleteById(id);
    }
}
