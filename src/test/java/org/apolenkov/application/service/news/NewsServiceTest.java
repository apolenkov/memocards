package org.apolenkov.application.service.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.NewsRepository;
import org.apolenkov.application.model.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for NewsService business operations.
 * Tests cover CRUD operations, validation, and business rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsService Tests")
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    private NewsService newsService;

    @BeforeEach
    void setUp() {
        newsService = new NewsService(newsRepository);
    }

    @Test
    @DisplayName("Should get all news ordered by creation date")
    void shouldGetAllNewsOrderedByCreationDate() {
        // Given
        News news1 = new News(
                1L, "Title 1", "Content 1", "Author 1", LocalDateTime.now().minusDays(1));
        News news2 = new News(2L, "Title 2", "Content 2", "Author 2", LocalDateTime.now());
        List<News> expectedNews = List.of(news2, news1);

        when(newsRepository.findAllOrderByCreatedDesc()).thenReturn(expectedNews);

        // When
        List<News> result = newsService.getAllNews();

        // Then
        assertThat(result).hasSize(2).containsExactly(news2, news1);
        verify(newsRepository).findAllOrderByCreatedDesc();
    }

    @Test
    @DisplayName("Should return empty list when no news exists")
    void shouldReturnEmptyListWhenNoNewsExists() {
        // Given
        when(newsRepository.findAllOrderByCreatedDesc()).thenReturn(Collections.emptyList());

        // When
        List<News> result = newsService.getAllNews();

        // Then
        assertThat(result).isEmpty();
        verify(newsRepository).findAllOrderByCreatedDesc();
    }

    @Test
    @DisplayName("Should create news with valid data")
    void shouldCreateNewsWithValidData() {
        // Given
        String title = "Breaking News";
        String content = "Important announcement about new features";
        String author = "admin";

        ArgumentCaptor<News> newsCaptor = ArgumentCaptor.forClass(News.class);

        // When
        newsService.createNews(title, content, author);

        // Then
        verify(newsRepository).save(newsCaptor.capture());

        News savedNews = newsCaptor.getValue();
        assertThat(savedNews.getTitle()).isEqualTo(title);
        assertThat(savedNews.getContent()).isEqualTo(content);
        assertThat(savedNews.getAuthor()).isEqualTo(author);
        assertThat(savedNews.getCreatedAt()).isNotNull();
        assertThat(savedNews.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should reject null title")
    void shouldRejectNullTitle() {
        assertThatThrownBy(() -> newsService.createNews(null, "content", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");
    }

    @Test
    @DisplayName("Should reject empty title")
    void shouldRejectEmptyTitle() {
        assertThatThrownBy(() -> newsService.createNews("   ", "content", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");
    }

    @Test
    @DisplayName("Should reject null content")
    void shouldRejectNullContent() {
        assertThatThrownBy(() -> newsService.createNews("title", null, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content is required");
    }

    @Test
    @DisplayName("Should reject empty content")
    void shouldRejectEmptyContent() {
        assertThatThrownBy(() -> newsService.createNews("title", "   ", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content is required");
    }

    @Test
    @DisplayName("Should update existing news")
    void shouldUpdateExistingNews() {
        // Given
        Long newsId = 1L;
        String newTitle = "Updated Title";
        String newContent = "Updated Content";
        String newAuthor = "Updated Author";

        News existingNews = new News(newsId, "Old Title", "Old Content", "Old Author", LocalDateTime.now());
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));

        ArgumentCaptor<News> newsCaptor = ArgumentCaptor.forClass(News.class);

        // When
        newsService.updateNews(newsId, newTitle, newContent, newAuthor);

        // Then
        verify(newsRepository).save(newsCaptor.capture());

        News updatedNews = newsCaptor.getValue();
        assertThat(updatedNews.getId()).isEqualTo(newsId);
        assertThat(updatedNews.getTitle()).isEqualTo(newTitle);
        assertThat(updatedNews.getContent()).isEqualTo(newContent);
        assertThat(updatedNews.getAuthor()).isEqualTo(newAuthor);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent news")
    void shouldThrowExceptionWhenUpdatingNonExistentNews() {
        // Given
        long newsId = 999L;
        when(newsRepository.findById(newsId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> newsService.updateNews(newsId, "Title", "Content", "Author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("News not found with id:");
    }

    @Test
    @DisplayName("Should delete news by id")
    void shouldDeleteNewsById() {
        // Given
        long newsId = 1L;
        News existingNews = new News(newsId, "Title", "Content", "Author", LocalDateTime.now());
        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));

        // When
        newsService.deleteNews(newsId);

        // Then
        verify(newsRepository).deleteById(newsId);
    }

    @Test
    @DisplayName("Should handle delete of non-existent news without error")
    void shouldHandleDeleteOfNonExistentNewsWithoutError() {
        // Given
        long newsId = 999L;
        when(newsRepository.findById(newsId)).thenReturn(Optional.empty());

        // When / Then - should not throw exception
        newsService.deleteNews(newsId);

        // Verify deleteById was never called
        verify(newsRepository, never()).deleteById(newsId);
    }
}
