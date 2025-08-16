package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.NewsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.NewsJpaRepository;
import org.apolenkov.application.model.News;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"dev", "jpa", "prod"})
public class NewsJpaAdapter implements NewsRepository {

    private final NewsJpaRepository repo;

    public NewsJpaAdapter(NewsJpaRepository repo) {
        this.repo = repo;
    }

    private static News toModel(NewsEntity e) {
        News news = new News(e.getId(), e.getTitle(), e.getContent(), e.getAuthor(), e.getCreatedAt());
        news.setUpdatedAt(e.getUpdatedAt());
        return news;
    }

    private static NewsEntity toEntity(News news) {
        NewsEntity e = new NewsEntity();
        e.setId(news.getId());
        e.setTitle(news.getTitle());
        e.setContent(news.getContent());
        e.setAuthor(news.getAuthor());
        e.setCreatedAt(news.getCreatedAt());
        e.setUpdatedAt(news.getUpdatedAt());
        return e;
    }

    @Override
    public List<News> findAllOrderByCreatedDesc() {
        return repo.findAllOrderByCreatedDesc().stream()
                .map(NewsJpaAdapter::toModel)
                .toList();
    }

    @Override
    public Optional<News> findById(Long id) {
        return repo.findById(id).map(NewsJpaAdapter::toModel);
    }

    @Override
    public News save(News news) {
        return toModel(repo.save(toEntity(news)));
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
