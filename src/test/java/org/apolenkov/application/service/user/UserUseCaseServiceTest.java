package org.apolenkov.application.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.cache.RequestScopedUserCache;
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

    @Mock
    private RequestScopedUserCache userCache;

    private UserUseCaseService userUseCaseService;

    @BeforeEach
    void setUp() {
        userUseCaseService = new UserUseCaseService(userRepository, userCache);
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
}
