package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import java.util.List;
import org.apolenkov.application.infrastructure.repository.jpa.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsJpaRepository extends JpaRepository<NewsEntity, Long> {
    @Query("select n from NewsEntity n order by n.createdAt desc")
    List<NewsEntity> findAllOrderByCreatedDesc();
}
