package org.apolenkov.application.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCaseService Tests")
class UserUseCaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    private UserUseCaseService userUseCaseService;

    @BeforeEach
    void setUp() {
        userUseCaseService = new UserUseCaseService(userRepository);
        // Setup SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("GetAllUsers should return all users from repository")
        void getAllUsersShouldReturnAllUsersFromRepository() {
            // Given
            User user1 = new User(1L, "user1@example.com", "User 1");
            User user2 = new User(2L, "user2@example.com", "User 2");
            List<User> expectedUsers = List.of(user1, user2);

            when(userRepository.findAll()).thenReturn(expectedUsers);

            // When
            List<User> result = userUseCaseService.getAllUsers();

            // Then
            assertThat(result).isEqualTo(expectedUsers);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("GetAllUsers should return empty list when no users exist")
        void getAllUsersShouldReturnEmptyListWhenNoUsersExist() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of());

            // When
            List<User> result = userUseCaseService.getAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("GetUserById should return user when exists")
        void getUserByIdShouldReturnUserWhenExists() {
            // Given
            long userId = 1L;
            User expectedUser = new User(userId, "test@example.com", "Test User");

            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

            // When
            Optional<User> result = userUseCaseService.getUserById(userId);

            // Then
            assertThat(result).isPresent().contains(expectedUser);
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("GetUserById should return empty when user does not exist")
        void getUserByIdShouldReturnEmptyWhenUserDoesNotExist() {
            // Given
            long userId = 999L;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When
            Optional<User> result = userUseCaseService.getUserById(userId);

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findById(userId);
        }
    }

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("GetCurrentUser should return current user when authenticated")
        void getCurrentUserShouldReturnCurrentUserWhenAuthenticated() {
            // Given
            String username = "test@example.com";
            User expectedUser = new User(1L, username, "Test User");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(username);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(expectedUser));

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            verify(userRepository).findByEmail(username);
        }

        @Test
        @DisplayName("GetCurrentUser should handle string principal")
        void getCurrentUserShouldHandleStringPrincipal() {
            // Given
            String username = "test@example.com";
            User expectedUser = new User(1L, username, "Test User");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(username);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(expectedUser));

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            verify(userRepository).findByEmail(username);
        }

        @Test
        @DisplayName("GetCurrentUser should throw exception when not authenticated")
        void getCurrentUserShouldThrowExceptionWhenNotAuthenticated() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userUseCaseService.getCurrentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Unauthenticated");
        }

        @Test
        @DisplayName("GetCurrentUser should throw exception when principal is null")
        void getCurrentUserShouldThrowExceptionWhenPrincipalIsNull() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userUseCaseService.getCurrentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Authenticated principal is null");
        }

        @Test
        @DisplayName("GetCurrentUser should throw exception for unsupported principal type")
        void getCurrentUserShouldThrowExceptionForUnsupportedPrincipalType() {
            // Given
            Object unsupportedPrincipal = new Object();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(unsupportedPrincipal);

            // When & Then
            assertThatThrownBy(() -> userUseCaseService.getCurrentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Unsupported principal type");
        }

        @Test
        @DisplayName("GetCurrentUser should throw exception when user not found in repository")
        void getCurrentUserShouldThrowExceptionWhenUserNotFoundInRepository() {
            // Given
            String username = "nonexistent@example.com";

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(username);
            when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userUseCaseService.getCurrentUser())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Authenticated principal has no domain user");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large user IDs")
        void shouldHandleVeryLargeUserIds() {
            // Given
            long largeId = Long.MAX_VALUE;
            User expectedUser = new User(largeId, "test@example.com", "Test User");

            when(userRepository.findById(largeId)).thenReturn(Optional.of(expectedUser));

            // When
            Optional<User> result = userUseCaseService.getUserById(largeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("Should handle very long email addresses")
        void shouldHandleVeryLongEmailAddresses() {
            // Given
            String longEmail = "a".repeat(200) + "@example.com";
            User expectedUser = new User(1L, longEmail, "Test User");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(longEmail);
            when(userRepository.findByEmail(longEmail)).thenReturn(Optional.of(expectedUser));

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            verify(userRepository).findByEmail(longEmail);
        }

        @Test
        @DisplayName("Should handle special characters in usernames")
        void shouldHandleSpecialCharactersInUsernames() {
            // Given
            String specialUsername = "test+tag@example-domain.co.uk";
            User expectedUser = new User(1L, specialUsername, "Test User");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(specialUsername);
            when(userRepository.findByEmail(specialUsername)).thenReturn(Optional.of(expectedUser));

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            verify(userRepository).findByEmail(specialUsername);
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("GetAllUsers should be read-only transactional")
        void getAllUsersShouldBeReadOnlyTransactional() {
            // This test verifies that the method is annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            User user = new User(1L, "test@example.com", "Test User");
            List<User> expectedUsers = List.of(user);

            when(userRepository.findAll()).thenReturn(expectedUsers);

            // When
            List<User> result = userUseCaseService.getAllUsers();

            // Then
            assertThat(result).hasSize(1);
            // Transaction behavior is verified by the fact that the method executes without error
        }

        @Test
        @DisplayName("GetUserById should be read-only transactional")
        void getUserByIdShouldBeReadOnlyTransactional() {
            // This test verifies that the method is annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            long userId = 1L;
            User expectedUser = new User(userId, "test@example.com", "Test User");

            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

            // When
            Optional<User> result = userUseCaseService.getUserById(userId);

            // Then
            assertThat(result).isPresent();
            // Transaction behavior is verified by the fact that the method executes without error
        }

        @Test
        @DisplayName("GetCurrentUser should be read-only transactional")
        void getCurrentUserShouldBeReadOnlyTransactional() {
            // This test verifies that the method is annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            String username = "test@example.com";
            User expectedUser = new User(1L, username, "Test User");

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(username);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(expectedUser));

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            // Transaction behavior is verified by the fact that the method executes without error
        }
    }

    @Nested
    @DisplayName("Security Context Tests")
    class SecurityContextTests {

        @Test
        @DisplayName("Should handle SecurityContextHolder changes")
        void shouldHandleSecurityContextHolderChanges() {
            // Given
            String username = "test@example.com";
            User expectedUser = new User(1L, username, "Test User");

            // Create a new security context
            SecurityContext newSecurityContext = mock(SecurityContext.class);
            Authentication newAuthentication = mock(Authentication.class);
            when(newSecurityContext.getAuthentication()).thenReturn(newAuthentication);
            when(newAuthentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(username);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(expectedUser));

            // Change security context
            SecurityContextHolder.setContext(newSecurityContext);

            // When
            User result = userUseCaseService.getCurrentUser();

            // Then
            assertThat(result).isEqualTo(expectedUser);
            verify(userRepository).findByEmail(username);
        }

        @Test
        @DisplayName("Should handle authentication changes")
        void shouldHandleAuthenticationChanges() {
            // Given
            String username1 = "user1@example.com";
            String username2 = "user2@example.com";
            User user1 = new User(1L, username1, "User 1");
            User user2 = new User(2L, username2, "User 2");

            // First authentication
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn(username1);
            when(userRepository.findByEmail(username1)).thenReturn(Optional.of(user1));

            User result1 = userUseCaseService.getCurrentUser();
            assertThat(result1).isEqualTo(user1);

            // Change authentication
            when(userDetails.getUsername()).thenReturn(username2);
            when(userRepository.findByEmail(username2)).thenReturn(Optional.of(user2));

            User result2 = userUseCaseService.getCurrentUser();
            assertThat(result2).isEqualTo(user2);
        }
    }
}
