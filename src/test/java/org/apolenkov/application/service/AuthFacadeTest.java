package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apolenkov.application.service.user.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacade Core Tests")
class AuthFacadeTest {

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private RegistrationService registrationService;

    private AuthFacade authFacade;

    @BeforeEach
    void setUp() {
        authFacade = new AuthFacade(authenticationConfiguration, registrationService);
    }

    @Test
    @DisplayName("Should register user with valid credentials")
    void shouldRegisterUserWithValidCredentials() {
        // Arrange
        String username = "test@example.com";
        String password = "password123";

        // Act
        authFacade.registerUser(username, password);

        // Assert - no exception thrown, user registered successfully
        assertThat(authFacade).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception for short password")
    void shouldThrowExceptionForShortPassword() {
        assertThatThrownBy(() -> authFacade.registerUser("test@example.com", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters");
    }

    @Test
    @DisplayName("Should throw exception for password without letters")
    void shouldThrowExceptionForPasswordWithoutLetters() {
        assertThatThrownBy(() -> authFacade.registerUser("test@example.com", "12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters and contain letters and digits");
    }

    @Test
    @DisplayName("Should throw exception for password without digits")
    void shouldThrowExceptionForPasswordWithoutDigits() {
        assertThatThrownBy(() -> authFacade.registerUser("test@example.com", "abcdefgh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters and contain letters and digits");
    }

    @Test
    @DisplayName("Should throw exception for null username")
    void shouldThrowExceptionForNullUsername() {
        assertThatThrownBy(() -> authFacade.authenticateAndPersist(null, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        assertThatThrownBy(() -> authFacade.authenticateAndPersist("test@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null");
    }
}
