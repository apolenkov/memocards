package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.FlashcardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.FlashcardJpaRepository;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlashcardJpaAdapter Tests")
class FlashcardJpaAdapterTest {

    @Mock
    private FlashcardJpaRepository repo;

    private FlashcardJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FlashcardJpaAdapter(repo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<FlashcardJpaAdapter> clazz = FlashcardJpaAdapter.class;

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
            Class<FlashcardJpaAdapter> clazz = FlashcardJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Find By Deck ID Tests")
    class FindByDeckIdTests {
        @Test
        @DisplayName("FindByDeckId should return flashcards for deck")
        void findByDeckIdShouldReturnFlashcardsForDeck() {
            // Given
            long deckId = 1L;
            FlashcardEntity entity1 = createFlashcardEntity(1L, deckId, "Front 1", "Back 1");
            FlashcardEntity entity2 = createFlashcardEntity(2L, deckId, "Front 2", "Back 2");
            List<FlashcardEntity> entities = List.of(entity1, entity2);

            when(repo.findByDeckId(deckId)).thenReturn(entities);

            // When
            List<Flashcard> result = adapter.findByDeckId(deckId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getFrontText()).isEqualTo("Front 1");
            assertThat(result.getFirst().getBackText()).isEqualTo("Back 1");
            assertThat(result.get(1).getFrontText()).isEqualTo("Front 2");
            assertThat(result.get(1).getBackText()).isEqualTo("Back 2");
            verify(repo).findByDeckId(deckId);
        }

        @Test
        @DisplayName("FindByDeckId should return empty list when no flashcards exist")
        void findByDeckIdShouldReturnEmptyListWhenNoFlashcardsExist() {
            // Given
            long deckId = 1L;
            when(repo.findByDeckId(deckId)).thenReturn(List.of());

            // When
            List<Flashcard> result = adapter.findByDeckId(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findByDeckId(deckId);
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {
        @Test
        @DisplayName("FindById should return flashcard when exists")
        void findByIdShouldReturnFlashcardWhenExists() {
            // Given
            long id = 1L;
            FlashcardEntity entity = createFlashcardEntity(id, 1L, "Front", "Back");
            when(repo.findById(id)).thenReturn(Optional.of(entity));

            // When
            Optional<Flashcard> result = adapter.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getFrontText()).isEqualTo("Front");
            assertThat(result.get().getBackText()).isEqualTo("Back");
            verify(repo).findById(id);
        }

        @Test
        @DisplayName("FindById should return empty when flashcard does not exist")
        void findByIdShouldReturnEmptyWhenFlashcardDoesNotExist() {
            // Given
            long id = 1L;
            when(repo.findById(id)).thenReturn(Optional.empty());

            // When
            Optional<Flashcard> result = adapter.findById(id);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(id);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {
        @Test
        @DisplayName("Save should save and return flashcard")
        void saveShouldSaveAndReturnFlashcard() {
            // Given
            Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back");
            flashcard.setExample("Example");
            flashcard.setImageUrl("image.jpg");
            flashcard.setCreatedAt(LocalDateTime.now().minusDays(1));
            flashcard.setUpdatedAt(LocalDateTime.now());

            FlashcardEntity savedEntity = createFlashcardEntity(1L, 1L, "Front", "Back");
            savedEntity.setExample("Example");
            savedEntity.setImageUrl("image.jpg");
            savedEntity.setCreatedAt(flashcard.getCreatedAt());
            savedEntity.setUpdatedAt(flashcard.getUpdatedAt());

            when(repo.save(any(FlashcardEntity.class))).thenReturn(savedEntity);

            // When
            Flashcard result = adapter.save(flashcard);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFrontText()).isEqualTo("Front");
            assertThat(result.getBackText()).isEqualTo("Back");
            assertThat(result.getExample()).isEqualTo("Example");
            assertThat(result.getImageUrl()).isEqualTo("image.jpg");
            verify(repo).save(any(FlashcardEntity.class));
        }

        @Test
        @DisplayName("Save should handle flashcard with null timestamps")
        void saveShouldHandleFlashcardWithNullTimestamps() {
            // Given
            Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back");
            flashcard.setCreatedAt(null);
            flashcard.setUpdatedAt(null);

            FlashcardEntity savedEntity = createFlashcardEntity(1L, 1L, "Front", "Back");
            when(repo.save(any(FlashcardEntity.class))).thenReturn(savedEntity);

            // When
            Flashcard result = adapter.save(flashcard);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(FlashcardEntity.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("DeleteById should delete flashcard")
        void deleteByIdShouldDeleteFlashcard() {
            // Given
            long id = 1L;

            // When
            adapter.deleteById(id);

            // Then
            verify(repo).deleteById(id);
        }

        @Test
        @DisplayName("DeleteByDeckId should delete all flashcards for deck")
        void deleteByDeckIdShouldDeleteAllFlashcardsForDeck() {
            // Given
            long deckId = 1L;

            // When
            adapter.deleteByDeckId(deckId);

            // Then
            verify(repo).deleteByDeckId(deckId);
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {
        @Test
        @DisplayName("CountByDeckId should return correct count")
        void countByDeckIdShouldReturnCorrectCount() {
            // Given
            long deckId = 1L;
            long expectedCount = 5L;
            when(repo.countByDeckId(deckId)).thenReturn(expectedCount);

            // When
            long result = adapter.countByDeckId(deckId);

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(repo).countByDeckId(deckId);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very large IDs")
        void shouldHandleVeryLargeIDs() {
            // Given
            long largeId = Long.MAX_VALUE;
            when(repo.findById(largeId)).thenReturn(Optional.empty());

            // When
            Optional<Flashcard> result = adapter.findById(largeId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(largeId);
        }

        @Test
        @DisplayName("Should handle null example and imageUrl")
        void shouldHandleNullExampleAndImageUrl() {
            // Given
            Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back");
            flashcard.setExample(null);
            flashcard.setImageUrl(null);

            FlashcardEntity savedEntity = createFlashcardEntity(1L, 1L, "Front", "Back");
            when(repo.save(any(FlashcardEntity.class))).thenReturn(savedEntity);

            // When
            Flashcard result = adapter.save(flashcard);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(FlashcardEntity.class));
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longText = "A".repeat(1000);
            Flashcard flashcard = new Flashcard(1L, 1L, longText, longText);
            flashcard.setExample(longText);
            flashcard.setImageUrl(longText);

            FlashcardEntity savedEntity = createFlashcardEntity(1L, 1L, longText, longText);
            savedEntity.setExample(longText);
            savedEntity.setImageUrl(longText);
            when(repo.save(any(FlashcardEntity.class))).thenReturn(savedEntity);

            // When
            Flashcard result = adapter.save(flashcard);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFrontText()).isEqualTo(longText);
            verify(repo).save(any(FlashcardEntity.class));
        }
    }

    private FlashcardEntity createFlashcardEntity(
            final Long id, final Long deckId, final String frontText, final String backText) {
        FlashcardEntity entity = new FlashcardEntity();
        entity.setId(id);
        entity.setDeckId(deckId);
        entity.setFrontText(frontText);
        entity.setBackText(backText);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
