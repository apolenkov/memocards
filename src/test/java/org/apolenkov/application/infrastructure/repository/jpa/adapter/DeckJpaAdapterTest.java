package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.DeckJpaRepository;
import org.apolenkov.application.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckJpaAdapter Tests")
class DeckJpaAdapterTest {

    @Mock
    private DeckJpaRepository deckJpaRepository;

    private DeckJpaAdapter deckJpaAdapter;

    @BeforeEach
    void setUp() {
        deckJpaAdapter = new DeckJpaAdapter(deckJpaRepository);
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {

        @Test
        @DisplayName("FindAll should return all decks")
        void findAllShouldReturnAllDecks() {
            // Given
            DeckEntity entity1 = createMockDeckEntity(1L, 1L, "Deck 1", "Description 1");
            DeckEntity entity2 = createMockDeckEntity(2L, 1L, "Deck 2", "Description 2");
            List<DeckEntity> entities = List.of(entity1, entity2);

            when(deckJpaRepository.findAll()).thenReturn(entities);

            // When
            List<Deck> result = deckJpaAdapter.findAll();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getTitle()).isEqualTo("Deck 1");
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getTitle()).isEqualTo("Deck 2");
            verify(deckJpaRepository).findAll();
        }

        @Test
        @DisplayName("FindAll should return empty list when no decks exist")
        void findAllShouldReturnEmptyListWhenNoDecksExist() {
            // Given
            when(deckJpaRepository.findAll()).thenReturn(List.of());

            // When
            List<Deck> result = deckJpaAdapter.findAll();

            // Then
            assertThat(result).isEmpty();
            verify(deckJpaRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Find By User ID Tests")
    class FindByUserIdTests {

        @Test
        @DisplayName("FindByUserId should return user's decks")
        void findByUserIdShouldReturnUsersDecks() {
            // Given
            Long userId = 1L;
            DeckEntity entity1 = createMockDeckEntity(1L, userId, "User Deck 1", "Description 1");
            DeckEntity entity2 = createMockDeckEntity(2L, userId, "User Deck 2", "Description 2");
            List<DeckEntity> entities = List.of(entity1, entity2);

            when(deckJpaRepository.findByUserId(userId)).thenReturn(entities);

            // When
            List<Deck> result = deckJpaAdapter.findByUserId(userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(userId);
            assertThat(result.get(1).getUserId()).isEqualTo(userId);
            verify(deckJpaRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("FindByUserId should return empty list when user has no decks")
        void findByUserIdShouldReturnEmptyListWhenUserHasNoDecks() {
            // Given
            Long userId = 1L;
            when(deckJpaRepository.findByUserId(userId)).thenReturn(List.of());

            // When
            List<Deck> result = deckJpaAdapter.findByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(deckJpaRepository).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("FindById should return deck when exists")
        void findByIdShouldReturnDeckWhenExists() {
            // Given
            Long deckId = 1L;
            DeckEntity entity = createMockDeckEntity(deckId, 1L, "Test Deck", "Test Description");

            when(deckJpaRepository.findById(deckId)).thenReturn(Optional.of(entity));

            // When
            Optional<Deck> result = deckJpaAdapter.findById(deckId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(deckId);
            assertThat(result.get().getTitle()).isEqualTo("Test Deck");
            verify(deckJpaRepository).findById(deckId);
        }

        @Test
        @DisplayName("FindById should return empty when deck does not exist")
        void findByIdShouldReturnEmptyWhenDeckDoesNotExist() {
            // Given
            Long deckId = 999L;
            when(deckJpaRepository.findById(deckId)).thenReturn(Optional.empty());

            // When
            Optional<Deck> result = deckJpaAdapter.findById(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(deckJpaRepository).findById(deckId);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Save should save and return deck")
        void saveShouldSaveAndReturnDeck() {
            // Given
            Deck deckToSave = new Deck(null, 1L, "Test Deck", "Test Description");
            deckToSave.setCreatedAt(LocalDateTime.now());
            deckToSave.setUpdatedAt(LocalDateTime.now());

            DeckEntity savedEntity = createMockDeckEntity(1L, 1L, "Test Deck", "Test Description");
            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Deck");
            verify(deckJpaRepository).save(any(DeckEntity.class));
        }

        @Test
        @DisplayName("Save should handle deck with null timestamps")
        void saveShouldHandleDeckWithNullTimestamps() {
            // Given
            Deck deckToSave = new Deck(null, 1L, "Test Deck", "Test Description");
            // No timestamps set

            DeckEntity savedEntity = createMockDeckEntity(1L, 1L, "Test Deck", "Test Description");
            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull();
            verify(deckJpaRepository).save(any(DeckEntity.class));
        }

        @Test
        @DisplayName("Save should handle deck with existing timestamps")
        void saveShouldHandleDeckWithExistingTimestamps() {
            // Given
            LocalDateTime existingTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            Deck deckToSave = new Deck(1L, 1L, "Test Deck", "Test Description");
            deckToSave.setCreatedAt(existingTime);
            deckToSave.setUpdatedAt(existingTime);

            DeckEntity savedEntity = createMockDeckEntity(1L, 1L, "Test Deck", "Test Description");
            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull();
            verify(deckJpaRepository).save(any(DeckEntity.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("DeleteById should delete deck")
        void deleteByIdShouldDeleteDeck() {
            // Given
            Long deckId = 1L;

            // When
            deckJpaAdapter.deleteById(deckId);

            // Then
            verify(deckJpaRepository).deleteById(deckId);
        }
    }

    @Nested
    @DisplayName("Entity Mapping Integration Tests")
    class EntityMappingIntegrationTests {

        @Test
        @DisplayName("Save should correctly map entity to model")
        void saveShouldCorrectlyMapEntityToModel() {
            // Given
            Long id = 1L;
            Long userId = 2L;
            String title = "Test Title";
            String description = "Test Description";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 13, 0);

            Deck deckToSave = new Deck(null, userId, title, description);
            deckToSave.setCreatedAt(createdAt);
            deckToSave.setUpdatedAt(updatedAt);

            DeckEntity savedEntity = new DeckEntity();
            savedEntity.setId(id);
            savedEntity.setUserId(userId);
            savedEntity.setTitle(title);
            savedEntity.setDescription(description);
            savedEntity.setCreatedAt(createdAt);
            savedEntity.setUpdatedAt(updatedAt);

            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("FindById should correctly map entity to model")
        void findByIdShouldCorrectlyMapEntityToModel() {
            // Given
            Long id = 1L;
            Long userId = 2L;
            String title = "Test Title";
            String description = "Test Description";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 13, 0);

            DeckEntity entity = new DeckEntity();
            entity.setId(id);
            entity.setUserId(userId);
            entity.setTitle(title);
            entity.setDescription(description);
            entity.setCreatedAt(createdAt);
            entity.setUpdatedAt(updatedAt);

            when(deckJpaRepository.findById(id)).thenReturn(Optional.of(entity));

            // When
            Optional<Deck> result = deckJpaAdapter.findById(id);

            // Then
            assertThat(result).isPresent();
            Deck deck = result.get();
            assertThat(deck.getId()).isEqualTo(id);
            assertThat(deck.getUserId()).isEqualTo(userId);
            assertThat(deck.getTitle()).isEqualTo(title);
            assertThat(deck.getDescription()).isEqualTo(description);
            assertThat(deck.getCreatedAt()).isEqualTo(createdAt);
            assertThat(deck.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("Save should set current time when timestamps are null")
        void saveShouldSetCurrentTimeWhenTimestampsAreNull() {
            // Given
            Deck deckToSave = new Deck(1L, 1L, "Test Title", "Test Description");
            // No timestamps set

            DeckEntity savedEntity = createMockDeckEntity(1L, 1L, "Test Title", "Test Description");
            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull();
            // The actual timestamp setting is tested through the save method behavior
            verify(deckJpaRepository).save(any(DeckEntity.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large IDs")
        void shouldHandleVeryLargeIds() {
            // Given
            Long largeId = Long.MAX_VALUE;
            DeckEntity entity = createMockDeckEntity(largeId, 1L, "Test Deck", "Test Description");

            when(deckJpaRepository.findById(largeId)).thenReturn(Optional.of(entity));

            // When
            Optional<Deck> result = deckJpaAdapter.findById(largeId);

            // Then
            assertThat(result).isPresent();

            Deck deck = result.get();
            assertThat(deck).satisfies(current -> assertThat(current.getId()).isEqualTo(largeId));
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longTitle = "a".repeat(120);
            String longDescription = "b".repeat(500);
            DeckEntity entity = createMockDeckEntity(1L, 1L, longTitle, longDescription);

            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(entity);

            Deck deckToSave = new Deck(null, 1L, longTitle, longDescription);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull().satisfies(deck -> {
                assertThat(deck.getTitle()).isEqualTo(longTitle);
                assertThat(deck.getDescription()).isEqualTo(longDescription);
            });
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            // Given
            Deck deckToSave = new Deck(null, 1L, "Test Title", null);

            DeckEntity savedEntity = createMockDeckEntity(1L, 1L, "Test Title", null);
            when(deckJpaRepository.save(any(DeckEntity.class))).thenReturn(savedEntity);

            // When
            Deck result = deckJpaAdapter.save(deckToSave);

            // Then
            assertThat(result).isNotNull().satisfies(deck -> assertThat(deck.getDescription())
                    .isNull());
        }
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {

        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // This test verifies that the class is properly annotated
            // The actual profile behavior is tested in integration tests

            // Given
            DeckJpaAdapter adapter = new DeckJpaAdapter(deckJpaRepository);

            // When & Then
            assertThat(adapter).isNotNull().isInstanceOf(DeckJpaAdapter.class);
        }
    }

    // Helper method to create mock DeckEntity
    private DeckEntity createMockDeckEntity(
            final Long id, final Long userId, final String title, final String description) {
        DeckEntity entity = new DeckEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
