package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Domain Model Tests")
class UserTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@example.com", "Test User");
    }

    @Test
    @DisplayName("Should create user with valid parameters")
    void shouldCreateUserWithValidParameters() {
        User user = new User(1L, "user@example.com", "Full Name");

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getName()).isEqualTo("Full Name");
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create user with minimal parameters")
    void shouldCreateUserWithMinimalParameters() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setName("User Name");

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getName()).isEqualTo("User Name");
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        Long newId = 999L;
        testUser.setId(newId);

        assertThat(testUser.getId()).isEqualTo(newId);
    }

    @Test
    @DisplayName("Should set and get email")
    void shouldSetAndGetEmail() {
        String newEmail = "newemail@example.com";
        testUser.setEmail(newEmail);

        assertThat(testUser.getEmail()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("Should set and get name")
    void shouldSetAndGetName() {
        String newName = "New User Name";
        testUser.setName(newName);

        assertThat(testUser.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        assertThatThrownBy(() -> testUser.setName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    @DisplayName("Should throw exception for empty name")
    void shouldThrowExceptionForEmptyName() {
        assertThatThrownBy(() -> testUser.setName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");

        assertThatThrownBy(() -> testUser.setName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        assertThatThrownBy(() -> testUser.setEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is required");
    }

    @Test
    @DisplayName("Should throw exception for empty email")
    void shouldThrowExceptionForEmptyEmail() {
        assertThatThrownBy(() -> testUser.setEmail(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is required");

        assertThatThrownBy(() -> testUser.setEmail("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is required");
    }

    @Test
    @DisplayName("Should trim email")
    void shouldTrimEmail() {
        testUser.setEmail("  email@example.com  ");

        assertThat(testUser.getEmail()).isEqualTo("email@example.com");
    }

    @Test
    @DisplayName("Should set and get password hash")
    void shouldSetAndGetPasswordHash() {
        String passwordHash = "hashedPassword123";
        testUser.setPasswordHash(passwordHash);

        assertThat(testUser.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    @DisplayName("Should handle null password hash")
    void shouldHandleNullPasswordHash() {
        testUser.setPasswordHash(null);

        assertThat(testUser.getPasswordHash()).isNull();
    }

    @Test
    @DisplayName("Should handle empty password hash")
    void shouldHandleEmptyPasswordHash() {
        testUser.setPasswordHash("");

        assertThat(testUser.getPasswordHash()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get roles")
    void shouldSetAndGetRoles() {
        Set<String> roles = Set.of("USER", "ADMIN");
        testUser.setRoles(roles);

        assertThat(testUser.getRoles()).hasSize(2);
        assertThat(testUser.getRoles()).contains("USER", "ADMIN");
    }

    @Test
    @DisplayName("Should handle empty roles")
    void shouldHandleEmptyRoles() {
        testUser.setRoles(Set.of());

        assertThat(testUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null roles")
    void shouldHandleNullRoles() {
        testUser.setRoles(null);

        assertThat(testUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should add role")
    void shouldAddRole() {
        testUser.addRole("USER");
        testUser.addRole("ADMIN");

        assertThat(testUser.getRoles()).hasSize(2);
        assertThat(testUser.getRoles()).contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should handle adding null role")
    void shouldHandleAddingNullRole() {
        testUser.addRole(null);
        assertThat(testUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should handle adding empty role")
    void shouldHandleAddingEmptyRole() {
        testUser.addRole("");
        testUser.addRole("   ");
        assertThat(testUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should automatically prefix role with ROLE_")
    void shouldAutomaticallyPrefixRoleWithRole() {
        testUser.addRole("USER");
        testUser.addRole("ROLE_ADMIN");

        assertThat(testUser.getRoles()).hasSize(2);
        assertThat(testUser.getRoles()).contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should handle user with special characters")
    void shouldHandleUserWithSpecialCharacters() {
        String specialName = "User @#$%^&*()_+-=[]{}|;':\",./<>?";
        testUser.setName(specialName);

        assertThat(testUser.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("Should handle user with unicode characters")
    void shouldHandleUserWithUnicodeCharacters() {
        String unicodeName = "User with unicode characters: 你好世界";
        testUser.setName(unicodeName);

        assertThat(testUser.getName()).isEqualTo(unicodeName);
    }

    @Test
    @DisplayName("Should handle user constraints")
    void shouldHandleUserConstraints() {
        // Test email length constraint - @Size(max = 255) is JPA validation, not runtime
        String longEmail = "a".repeat(256) + "@example.com";
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testUser.setEmail(longEmail);
        assertThat(testUser.getEmail()).isEqualTo(longEmail);

        // Test name length constraint - @Size(max = 120) is JPA validation, not runtime
        String longName = "a".repeat(121);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testUser.setName(longName);
        assertThat(testUser.getName()).isEqualTo(longName);
    }

    @Test
    @DisplayName("Should handle user equality and hash code")
    void shouldHandleUserEqualityAndHashCode() {
        User user1 = new User(1L, "user@example.com", "User Name");
        User user2 = new User(1L, "user@example.com", "User Name");
        User user3 = new User(2L, "other@example.com", "Other Name");

        assertThat(user1).isEqualTo(user2).isNotEqualTo(user3).hasSameHashCodeAs(user2);
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }

    @Test
    @DisplayName("Should handle user string representation")
    void shouldHandleUserStringRepresentation() {
        String userString = testUser.toString();

        assertThat(userString).contains("test@example.com").contains("Test User");
    }

    @Test
    @DisplayName("Should handle user with maximum allowed values")
    void shouldHandleUserWithMaximumAllowedValues() {
        String maxName = "A".repeat(120); // Maximum allowed name length

        testUser.setName(maxName);
        assertThat(testUser.getName()).isEqualTo(maxName);
    }

    @Test
    @DisplayName("Should handle user with minimum allowed values")
    void shouldHandleUserWithMinimumAllowedValues() {
        String minName = "A"; // Minimum valid name length
        testUser.setName(minName);

        assertThat(testUser.getName()).isEqualTo(minName);
    }

    @Test
    @DisplayName("Should handle user creation with null values")
    void shouldHandleUserCreationWithNullValues() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        // Cannot set null name as it throws exception
        user.setName("Valid Name");

        assertThat(user).satisfies(u -> {
            assertThat(u.getId()).isEqualTo(1L);
            assertThat(u.getEmail()).isEqualTo("user@example.com");
            assertThat(u.getName()).isEqualTo("Valid Name");
        });
    }

    @Test
    @DisplayName("Should handle user with complex email formats")
    void shouldHandleUserWithComplexEmailFormats() {
        // Valid email formats
        testUser.setEmail("user.name@domain.co.uk");
        assertThat(testUser.getEmail()).isEqualTo("user.name@domain.co.uk");

        testUser.setEmail("user+tag@example.com");
        assertThat(testUser.getEmail()).isEqualTo("user+tag@example.com");

        testUser.setEmail("user123@test-domain.org");
        assertThat(testUser.getEmail()).isEqualTo("user123@test-domain.org");
    }

    @Test
    @DisplayName("Should handle user roles operations")
    void shouldHandleUserRolesOperations() {
        // Test initial state
        assertThat(testUser.getRoles()).isEmpty();

        // Add roles
        testUser.addRole("USER");
        testUser.addRole("ADMIN");
        testUser.addRole("MODERATOR");

        assertThat(testUser.getRoles()).hasSize(3);
        assertThat(testUser.getRoles()).contains("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");

        // Clear all roles
        testUser.setRoles(Set.of());
        assertThat(testUser.getRoles()).isEmpty();
    }
}
