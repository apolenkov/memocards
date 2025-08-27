package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserJpaRepository;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserJpaAdapter Tests")
class UserJpaAdapterTest {

    @Mock
    private UserJpaRepository repo;

    private UserJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserJpaAdapter(repo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<UserJpaAdapter> clazz = UserJpaAdapter.class;

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
            Class<UserJpaAdapter> clazz = UserJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {
        @Test
        @DisplayName("FindAll should return all users")
        void findAllShouldReturnAllUsers() {
            // Given
            UserEntity entity1 = createUserEntity(1L, "user1@test.com", "User 1");
            UserEntity entity2 = createUserEntity(2L, "user2@test.com", "User 2");
            List<UserEntity> entities = List.of(entity1, entity2);

            when(repo.findAll()).thenReturn(entities);

            // When
            List<User> result = adapter.findAll();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getEmail()).isEqualTo("user1@test.com");
            assertThat(result.getFirst().getName()).isEqualTo("User 1");
            assertThat(result.get(1).getEmail()).isEqualTo("user2@test.com");
            assertThat(result.get(1).getName()).isEqualTo("User 2");
            verify(repo).findAll();
        }

        @Test
        @DisplayName("FindAll should return empty list when no users exist")
        void findAllShouldReturnEmptyListWhenNoUsersExist() {
            // Given
            when(repo.findAll()).thenReturn(List.of());

            // When
            List<User> result = adapter.findAll();

            // Then
            assertThat(result).isEmpty();
            verify(repo).findAll();
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {
        @Test
        @DisplayName("FindById should return user when exists")
        void findByIdShouldReturnUserWhenExists() {
            // Given
            Long id = 1L;
            UserEntity entity = createUserEntity(id, "user@test.com", "Test User");
            when(repo.findById(id)).thenReturn(Optional.of(entity));

            // When
            Optional<User> result = adapter.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("user@test.com");
            assertThat(result.get().getName()).isEqualTo("Test User");
            verify(repo).findById(id);
        }

        @Test
        @DisplayName("FindById should return empty when user does not exist")
        void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
            // Given
            Long id = 1L;
            when(repo.findById(id)).thenReturn(Optional.empty());

            // When
            Optional<User> result = adapter.findById(id);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(id);
        }
    }

    @Nested
    @DisplayName("Find By Email Tests")
    class FindByEmailTests {
        @Test
        @DisplayName("FindByEmail should return user when exists")
        void findByEmailShouldReturnUserWhenExists() {
            // Given
            String email = "user@test.com";
            UserEntity entity = createUserEntity(1L, email, "Test User");
            when(repo.findByEmail(email)).thenReturn(Optional.of(entity));

            // When
            Optional<User> result = adapter.findByEmail(email);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
            assertThat(result.get().getName()).isEqualTo("Test User");
            verify(repo).findByEmail(email);
        }

        @Test
        @DisplayName("FindByEmail should return empty when user does not exist")
        void findByEmailShouldReturnEmptyWhenUserDoesNotExist() {
            // Given
            String email = "user@test.com";
            when(repo.findByEmail(email)).thenReturn(Optional.empty());

            // When
            Optional<User> result = adapter.findByEmail(email);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {
        @Test
        @DisplayName("Save should save and return user")
        void saveShouldSaveAndReturnUser() {
            // Given
            User user = new User(1L, "user@test.com", "Test User");
            user.setPasswordHash("hashedPassword");
            user.setCreatedAt(LocalDateTime.now().minusDays(1));
            user.addRole("USER");
            user.addRole("ADMIN");

            UserEntity savedEntity = createUserEntity(1L, "user@test.com", "Test User");
            savedEntity.setPasswordHash("hashedPassword");
            savedEntity.setCreatedAt(user.getCreatedAt());
            savedEntity.setRoles(Set.of("USER", "ADMIN"));

            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("user@test.com");
            assertThat(result.getName()).isEqualTo("Test User");
            assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");
            assertThat(result.getRoles()).contains("USER", "ADMIN");
            verify(repo).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Save should handle user with null createdAt")
        void saveShouldHandleUserWithNullCreatedAt() {
            // Given
            User user = new User(1L, "user@test.com", "Test User");
            user.setCreatedAt(null);

            UserEntity savedEntity = createUserEntity(1L, "user@test.com", "Test User");
            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Save should handle user with empty roles")
        void saveShouldHandleUserWithEmptyRoles() {
            // Given
            User user = new User(1L, "user@test.com", "Test User");
            user.setRoles(Set.of());

            UserEntity savedEntity = createUserEntity(1L, "user@test.com", "Test User");
            savedEntity.setRoles(Set.of());
            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).isEmpty();
            verify(repo).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("DeleteById should delete user")
        void deleteByIdShouldDeleteUser() {
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
            Optional<User> result = adapter.findById(largeId);

            // Then
            assertThat(result).isEmpty();
            verify(repo).findById(largeId);
        }

        @Test
        @DisplayName("Should handle very long email addresses")
        void shouldHandleVeryLongEmailAddresses() {
            // Given
            String longEmail = "a".repeat(100) + "@test.com";
            User user = new User(1L, longEmail, "Test User");

            UserEntity savedEntity = createUserEntity(1L, longEmail, "Test User");
            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(longEmail);
            verify(repo).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            // Given
            String specialName = "Test User with émojis 🎉 and symbols @#$%";
            User user = new User(1L, "user@test.com", specialName);

            UserEntity savedEntity = createUserEntity(1L, "user@test.com", specialName);
            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(specialName);
            verify(repo).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should handle null password hash")
        void shouldHandleNullPasswordHash() {
            // Given
            User user = new User(1L, "user@test.com", "Test User");
            user.setPasswordHash(null);

            UserEntity savedEntity = createUserEntity(1L, "user@test.com", "Test User");
            savedEntity.setPasswordHash(null);
            when(repo.save(any(UserEntity.class))).thenReturn(savedEntity);

            // When
            User result = adapter.save(user);

            // Then
            assertThat(result).isNotNull();
            verify(repo).save(any(UserEntity.class));
        }
    }

    private UserEntity createUserEntity(final Long id, final String email, final String name) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setName(name);
        entity.setPasswordHash("hashedPassword");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRoles(Set.of("USER"));
        return entity;
    }
}
