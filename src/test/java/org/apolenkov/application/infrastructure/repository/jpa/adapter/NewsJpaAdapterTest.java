package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.NewsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.NewsJpaRepository;
import org.apolenkov.application.model.News;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsJpaAdapter Tests")
class NewsJpaAdapterTest {

    @Mock
    private NewsJpaRepository repo;

    private NewsJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NewsJpaAdapter(repo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<NewsJpaAdapter> clazz = NewsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Profile.class))
                    .isTrue();
            org.springframework.context.annotation.Profile profile =
                    clazz.getAnnotation(org.springframework.context.annotation.Profile.class);
            assertThat(profile.value()).contains("dev", "prod");
        }

        @Test
        @DisplayName("Should be annotated with Repository")
        void shouldBeAnnotatedWithRepository() {
            // Given
            Class<NewsJpaAdapter> clazz = NewsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Find All Order By Created Desc Tests")
    class FindAllOrderByCreatedDescTests {
        @Test
        @DisplayName("FindAllOrderByCreatedDesc should return news ordered by creation date")
        void findAllOrderByCreatedDescShouldReturnNewsOrderedByCreationDate() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            LocalDateTime twoDaysAgo = now.minusDays(2);

            NewsEntity entity1 = createNewsEntity(1L, "News 1", "Content 1", "Author 1", now);
            NewsEntity entity2 = createNewsEntity(2L, "News 2", "Content 2", "Author 2", yesterday);
            NewsEntity entity3 = createNewsEntity(3L, "News 3", "Content 3", "Author 3", twoDaysAgo);
            List<NewsEntity> entities = List.of(entity1, entity2, entity3);

            when(repo.findAllOrderByCreatedDesc()).thenReturn(entities);

            // When
            List<News> result = adapter.findAllOrderByCreatedDesc();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTitle()).isEqualTo("News 1");
            assertThat(result.get(0).getCreatedAt()).isEqualTo(now);
            assertThat(result.get(1).getTitle()).isEqualTo("News 2");
            assertThat(result.get(1).getCreatedAt()).isEqualTo(yesterday);
            assertThat(result.get(2).getTitle()).isEqualTo("News 3");
            assertThat(result.get(2).getCreatedAt()).isEqualTo(twoDaysAgo);
            verify(repo).findAllOrderByCreatedDesc();
        }

        @Test
        @DisplayName("FindAllOrderByCreatedDesc should return empty list when no news exist")
        void findAllOrderByCreatedDescShouldReturnEmptyListWhenNoNewsExist() {
            // Given
            when(repo.findAllOrderByCreatedDesc()).thenReturn(List.of());

            // When
            List<News> result = adapter.findAllOrderByCreatedDesc();

            // Then
            assertThat(result).isEmpty();
            verify(repo).findAllOrderByCreatedDesc();
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {
        @Test
        @DisplayName("FindById should return news when exists")
        void findByIdShouldReturnNewsWhenExists() {
            // Given
            Long id = 1L;
            NewsEntity entity = createNewsEntity(id, "Test News", "Test Content", "Test Author", LocalDateTime.now());
            when(repo.findById(id)).thenReturn(Optional.of(entity));

            // When
            Optional<News> result = adapter.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test News");
            assertThat(result.get().getContent()).isEqualTo("Test Content");
            assertThat(result.get().getAuthor()).isEqualTo("Test Author");
            verify(repo).findById(id);
        }

        @Test
        @DisplayName("FindById should return empty when news does not exist")
        void findByIdShouldReturnEmptyWhenNewsDoesNotExist() {
            // Given
            Long id = 1L;
            when(repo.findById(id)).thenReturn(Optional.empty());

            // When
            Optional<News> result = adapter.findById(id);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(id);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {
        @Test
        @DisplayName("Save should save and return news")
        void saveShouldSaveAndReturnNews() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            News news = new News(1L, "Test News", "Test Content", "Test Author", createdAt);
            news.setUpdatedAt(updatedAt);

            NewsEntity savedEntity = createNewsEntity(1L, "Test News", "Test Content", "Test Author", createdAt);
            savedEntity.setUpdatedAt(updatedAt);

            when(repo.save(any(NewsEntity.class))).thenReturn(savedEntity);

            // When
            News result = adapter.save(news);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test News");
            assertThat(result.getContent()).isEqualTo("Test Content");
            assertThat(result.getAuthor()).isEqualTo("Test Author");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
            verify(repo).save(any(NewsEntity.class));
        }

        @Test
        @DisplayName("Save should handle news with null updatedAt")
        void saveShouldHandleNewsWithNullUpdatedAt() {
            // Given
            News news = new News(1L, "Test News", "Test Content", "Test Author", LocalDateTime.now());
            news.setUpdatedAt(null);

            NewsEntity savedEntity =
                    createNewsEntity(1L, "Test News", "Test Content", "Test Author", LocalDateTime.now());
            when(repo.save(any(NewsEntity.class))).thenReturn(savedEntity);

            // When
            News result = adapter.save(news);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(NewsEntity.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("DeleteById should delete news")
        void deleteByIdShouldDeleteNews() {
            // Given
            Long id = 1L;

            // When
            adapter.deleteById(id);

            // Then
            verify(repo).deleteById(id);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very large IDs")
        void shouldHandleVeryLargeIDs() {
            // Given
            Long largeId = Long.MAX_VALUE;
            when(repo.findById(largeId)).thenReturn(Optional.empty());

            // When
            Optional<News> result = adapter.findById(largeId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(largeId);
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longText = "A".repeat(1000);
            News news = new News(1L, longText, longText, longText, LocalDateTime.now());

            NewsEntity savedEntity = createNewsEntity(1L, longText, longText, longText, LocalDateTime.now());
            when(repo.save(any(NewsEntity.class))).thenReturn(savedEntity);

            // When
            News result = adapter.save(news);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(longText);
            assertThat(result.getContent()).isEqualTo(longText);
            assertThat(result.getAuthor()).isEqualTo(longText);
            verify(repo).save(any(NewsEntity.class));
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicodeText = "Новости с émojis 🎉 и символами @#$%";
            News news = new News(1L, unicodeText, unicodeText, unicodeText, LocalDateTime.now());

            NewsEntity savedEntity = createNewsEntity(1L, unicodeText, unicodeText, unicodeText, LocalDateTime.now());
            when(repo.save(any(NewsEntity.class))).thenReturn(savedEntity);

            // When
            News result = adapter.save(news);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(unicodeText);
            verify(repo).save(any(NewsEntity.class));
        }

        @Test
        @DisplayName("Should handle null content")
        void shouldHandleNullContent() {
            // Given
            News news = new News(1L, "Test News", null, "Test Author", LocalDateTime.now());

            NewsEntity savedEntity = createNewsEntity(1L, "Test News", null, "Test Author", LocalDateTime.now());
            when(repo.save(any(NewsEntity.class))).thenReturn(savedEntity);

            // When
            News result = adapter.save(news);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(NewsEntity.class));
        }
    }

    private NewsEntity createNewsEntity(
            final Long id,
            final String title,
            final String content,
            final String author,
            final LocalDateTime createdAt) {
        NewsEntity entity = new NewsEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setAuthor(author);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }
}
