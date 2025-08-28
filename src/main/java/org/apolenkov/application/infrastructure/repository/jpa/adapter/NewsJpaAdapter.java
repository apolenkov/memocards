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
     * @param repoValue the Spring Data JPA repository for news operations
     * @throws IllegalArgumentException if repoValue is null
     */
    public NewsJpaAdapter(final NewsJpaRepository repoValue) {
        if (repoValue == null) {
            throw new IllegalArgumentException("NewsJpaRepository cannot be null");
        }
        this.repo = repoValue;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model
     * @throws IllegalArgumentException if entity is null
     */
    private static News toModel(final NewsEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("NewsEntity cannot be null");
        }

        final News news = new News(
                entity.getId(), entity.getTitle(), entity.getContent(), entity.getAuthor(), entity.getCreatedAt());
        news.setUpdatedAt(entity.getUpdatedAt());
        return news;
    }

    /**
     * Converts domain model to JPA entity.
     *
     * @param newsModel the domain model to convert
     * @return the corresponding JPA entity
     * @throws IllegalArgumentException if newsModel is null
     */
    private static NewsEntity toEntity(final News newsModel) {
        if (newsModel == null) {
            throw new IllegalArgumentException("News cannot be null");
        }

        final NewsEntity entity = new NewsEntity();
        entity.setId(newsModel.getId());
        entity.setTitle(newsModel.getTitle());
        entity.setContent(newsModel.getContent());
        entity.setAuthor(newsModel.getAuthor());
        entity.setCreatedAt(newsModel.getCreatedAt());
        entity.setUpdatedAt(newsModel.getUpdatedAt());
        return entity;
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
    public Optional<News> findById(final long id) {
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
    public News save(final News news) {
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
    public void deleteById(final long id) {
        repo.deleteById(id);
    }
}
