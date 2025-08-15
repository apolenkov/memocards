package org.apolenkov.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.model.News;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<News> getAllNews() {
        return newsRepository.findAllOrderByCreatedDesc();
    }

    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    public News createNews(String title, String content, String author) {
        News news = new News(null, title, content, author, LocalDateTime.now());
        return newsRepository.save(news);
    }

    public News updateNews(Long id, String title, String content, String author) {
        Optional<News> existingOpt = newsRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("News not found with id: " + id);
        }

        News existing = existingOpt.get();
        existing.setTitle(title);
        existing.setContent(content);
        existing.setAuthor(author);
        existing.setUpdatedAt(LocalDateTime.now());

        return newsRepository.save(existing);
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
}
