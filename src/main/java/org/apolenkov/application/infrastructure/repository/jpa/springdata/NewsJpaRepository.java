package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for news articles.
 *
 * <p>Provides CRUD operations and queries for news content management.</p>
 */
public interface NewsJpaRepository extends JpaRepository<NewsEntity, Long> {

    /**
     * Finds all news articles ordered by creation date (newest first).
     *
     * @return list of news articles in descending chronological order
     */
    @Query("select n from NewsEntity n order by n.createdAt desc")
    List<NewsEntity> findAllOrderByCreatedDesc();
}
