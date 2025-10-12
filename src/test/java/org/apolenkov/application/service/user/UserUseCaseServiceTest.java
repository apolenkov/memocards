package org.apolenkov.application.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCaseService Core Tests")
class UserUseCaseServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserUseCaseService userUseCaseService;

    @BeforeEach
    void setUp() {
        userUseCaseService = new UserUseCaseService(userRepository);
    }

    @Test
    @DisplayName("Should get user by id")
    void shouldGetUserById() {
        // Given
        long userId = 1L;
        User expectedUser = new User(userId, "test@example.com", "Test User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        Optional<User> result = userUseCaseService.getUserById(userId);

        // Then
        assertThat(result).isPresent().contains(expectedUser);
    }

    @Test
    @DisplayName("Should handle user not found")
    void shouldHandleUserNotFound() {
        // Given
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userUseCaseService.getUserById(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty user list")
    void shouldHandleEmptyUserList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userUseCaseService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Given
        List<User> expectedUsers =
                List.of(new User(1L, "user1@example.com", "User 1"), new User(2L, "user2@example.com", "User 2"));
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userUseCaseService.getAllUsers();

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedUsers);
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
        // Given: Existing user
        User user = new User(1L, "old@example.com", "Old Name");
        user.setEmail("new@example.com");
        user.setName("New Name");

        User updatedUser = new User(1L, "new@example.com", "New Name");

        when(userRepository.save(user)).thenReturn(updatedUser);

        // When: Update user
        User result = userUseCaseService.updateUser(user);

        // Then: User updated
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception when updating null user")
    void shouldThrowExceptionWhenUpdatingNull() {
        // When/Then
        assertThatThrownBy(() -> userUseCaseService.updateUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when updating user without ID")
    void shouldThrowExceptionWhenUpdatingUserWithoutId() {
        // Given: New user without ID
        User user = new User(null, "test@example.com", "Test User");

        // When/Then
        assertThatThrownBy(() -> userUseCaseService.updateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update user without ID");
    }

    @Test
    @DisplayName("Should update user email")
    void shouldUpdateUserEmail() {
        // Given: User with old email
        User user = new User(1L, "old@example.com", "User Name");
        user.setEmail("new@example.com"); // Email change

        User saved = new User(1L, "new@example.com", "User Name");
        when(userRepository.save(user)).thenReturn(saved);

        // When: Update
        User result = userUseCaseService.updateUser(user);

        // Then: Email updated and Caffeine cache evicted (allEntries=true at repository level)
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
    }
}
