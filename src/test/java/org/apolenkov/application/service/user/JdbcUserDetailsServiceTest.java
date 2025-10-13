package org.apolenkov.application.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Unit tests for JdbcUserDetailsService.
 * Tests Spring Security UserDetailsService implementation with role mapping.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JdbcUserDetailsService Tests")
class JdbcUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private JdbcUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new JdbcUserDetailsService(userRepository);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create service successfully with valid repository")
    void shouldCreateServiceSuccessfully() {
        // When
        JdbcUserDetailsService service = new JdbcUserDetailsService(userRepository);

        // Then
        assertThat(service).isNotNull();
    }

    // ==================== LoadUserByUsername Tests ====================

    @Test
    @DisplayName("Should load user details successfully")
    void shouldLoadUserDetailsSuccessfully() {
        // Given
        String email = "test@example.com";
        User user = new User(1L, email, "Test User");
        user.setPasswordHash("hashedPassword123");
        user.setRoles(Set.of(SecurityConstants.ROLE_USER));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo("hashedPassword123");
        assertThat(result.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly(SecurityConstants.ROLE_USER);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should load user with admin role")
    void shouldLoadUserWithAdminRole() {
        // Given
        String email = "admin@example.com";
        User user = new User(1L, email, "Admin");
        user.setPasswordHash("hashedPassword123");
        user.setRoles(Set.of(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_USER));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(result.getAuthorities())
                .hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_USER);
    }

    @Test
    @DisplayName("Should assign default USER role when user has no roles")
    void shouldAssignDefaultUserRoleWhenUserHasNoRoles() {
        // Given
        String email = "test@example.com";
        User user = new User(1L, email, "Test User");
        user.setPasswordHash("hashedPassword123");
        user.setRoles(Set.of()); // Empty roles

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(result.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly(SecurityConstants.ROLE_USER);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when username is null")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when username is empty")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("   "))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user has no password hash")
    void shouldThrowExceptionWhenUserHasNoPasswordHash() {
        // Given
        String email = "test@example.com";
        User user = new User(1L, email, "Test User");
        user.setPasswordHash(null); // No password hash

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when password hash is blank")
    void shouldThrowExceptionWhenPasswordHashIsBlank() {
        // Given
        String email = "test@example.com";
        User user = new User(1L, email, "Test User");
        user.setPasswordHash("   "); // Blank password hash

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When / Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Bad credentials");
    }

    @Test
    @DisplayName("Should trim username before lookup")
    void shouldTrimUsernameBeforeLookup() {
        // Given
        String emailWithSpaces = "  test@example.com  ";
        String trimmedEmail = "test@example.com";
        User user = new User(1L, trimmedEmail, "Test User");
        user.setPasswordHash("hashedPassword123");
        user.setRoles(Set.of(SecurityConstants.ROLE_USER));

        when(userRepository.findByEmail(trimmedEmail)).thenReturn(Optional.of(user));

        // When
        UserDetails result = userDetailsService.loadUserByUsername(emailWithSpaces);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findByEmail(trimmedEmail);
    }
}
