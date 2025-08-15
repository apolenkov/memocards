package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.News;

/** Repository for site news items shown on landing page. */
public interface NewsRepository {

    List<News> findAllOrderByCreatedDesc();

    Optional<News> findById(Long id);

    News save(News item);

    void deleteById(Long id);
}
