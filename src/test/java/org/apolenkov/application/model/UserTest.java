package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("User Model Tests")
class UserTest {

    private User user;
    private final LocalDateTime testTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with current time")
        void defaultConstructorShouldInitializeWithCurrentTime() {
            User newUser = new User();

            assertThat(newUser.getId()).isNull();
            assertThat(newUser.getEmail()).isNull();
            assertThat(newUser.getPasswordHash()).isNull();
            assertThat(newUser.getName()).isNull();
            assertThat(newUser.getRoles()).isEmpty();
            assertThat(newUser.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Parameterized constructor should set all fields")
        void parameterizedConstructorShouldSetAllFields() {
            User newUser = new User(1L, "test@example.com", "Test User");

            assertThat(newUser.getId()).isEqualTo(1L);
            assertThat(newUser.getEmail()).isEqualTo("test@example.com");
            assertThat(newUser.getName()).isEqualTo("Test User");
            assertThat(newUser.getPasswordHash()).isNull();
            assertThat(newUser.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Id getter and setter should work correctly")
        void idGetterAndSetterShouldWorkCorrectly() {
            user.setId(123L);
            assertThat(user.getId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("Email getter and setter should work correctly")
        void emailGetterAndSetterShouldWorkCorrectly() {
            user.setEmail("test@example.com");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Email setter should trim whitespace")
        void emailSetterShouldTrimWhitespace() {
            user.setEmail("  test@example.com  ");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Email setter should throw exception for null or blank email")
        void emailSetterShouldThrowExceptionForNullOrBlankEmail(final String email) {
            assertThatThrownBy(() -> user.setEmail(email))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("email is required");
        }

        @Test
        @DisplayName("PasswordHash getter and setter should work correctly")
        void passwordHashGetterAndSetterShouldWorkCorrectly() {
            user.setPasswordHash("hashedPassword123");
            assertThat(user.getPasswordHash()).isEqualTo("hashedPassword123");
        }

        @Test
        @DisplayName("Name getter and setter should work correctly")
        void nameGetterAndSetterShouldWorkCorrectly() {
            user.setName("Test User");
            assertThat(user.getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("Name setter should trim whitespace")
        void nameSetterShouldTrimWhitespace() {
            user.setName("  Test User  ");
            assertThat(user.getName()).isEqualTo("Test User");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Name setter should throw exception for null or blank name")
        void nameSetterShouldThrowExceptionForNullOrBlankName(final String name) {
            assertThatThrownBy(() -> user.setName(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is required");
        }

        @Test
        @DisplayName("CreatedAt getter and setter should work correctly")
        void createdAtGetterAndSetterShouldWorkCorrectly() {
            user.setCreatedAt(testTime);
            assertThat(user.getCreatedAt()).isEqualTo(testTime);
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("GetRoles should return unmodifiable set")
        @SuppressWarnings("DataFlowIssue") // To make test with error
        void getRolesShouldReturnUnmodifiableSet() {
            user.setRoles(Set.of("ROLE_USER"));

            Set<String> roles = user.getRoles();
            assertThatThrownBy(() -> roles.add("ROLE_ADMIN")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("SetRoles should create new set")
        void setRolesShouldCreateNewSet() {
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            user.setRoles(roles);

            assertThat(user.getRoles()).hasSize(1);
            assertThat(user.getRoles()).isNotSameAs(roles);
        }

        @Test
        @DisplayName("SetRoles should handle null input")
        void setRolesShouldHandleNullInput() {
            user.setRoles(null);
            assertThat(user.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("AddRole should add role with ROLE_ prefix")
        void addRoleShouldAddRoleWithRolePrefix() {
            user.addRole("USER");
            assertThat(user.getRoles()).contains("ROLE_USER");
        }

        @Test
        @DisplayName("AddRole should not duplicate ROLE_ prefix")
        void addRoleShouldNotDuplicateRolePrefix() {
            user.addRole("ROLE_USER");
            assertThat(user.getRoles()).contains("ROLE_USER");
            assertThat(user.getRoles()).hasSize(1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("AddRole should handle null, blank, and empty roles")
        void addRoleShouldHandleNullOrBlankRoles(final String role) {
            user.addRole(role);
            assertThat(user.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("AddRole should handle null role")
        void addRoleShouldHandleNullRole() {
            user.addRole(null);
            assertThat(user.getRoles()).isEmpty();
        }

        @Test
        @DisplayName("Multiple roles should be handled correctly")
        void multipleRolesShouldBeHandledCorrectly() {
            user.addRole("USER");
            user.addRole("ADMIN");
            user.addRole("MODERATOR");

            assertThat(user.getRoles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Equals should work correctly")
        void equalsShouldWorkCorrectly() {
            User user1 = new User();
            user1.setId(1L);

            User user2 = new User();
            user2.setId(1L);

            User user3 = new User();
            user3.setId(2L);

            assertThat(user1).isEqualTo(user2).isNotEqualTo(user3).isNotEqualTo(null);
        }

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            User user1 = new User();
            user1.setId(1L);

            User user2 = new User();
            user2.setId(1L);

            assertThat(user1).hasSameHashCodeAs(user2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("ToString should contain all relevant information")
        void toStringShouldContainAllRelevantInformation() {
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setName("Test User");
            user.setCreatedAt(testTime);

            String result = user.toString();

            assertThat(result)
                    .contains("id=1")
                    .contains("email='test@example.com'")
                    .contains("name='Test User'")
                    .contains("createdAt=" + testTime);
        }

        @Test
        @DisplayName("ToString should handle null values correctly")
        void toStringShouldHandleNullValuesCorrectly() {
            user.setId(1L);
            user.setEmail("test@example.com");
            user.setName("Test User");
            // Leave createdAt as null

            String result = user.toString();

            assertThat(result)
                    .contains("id=1")
                    .contains("email='test@example.com'")
                    .contains("name='Test User'");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long email within limits")
        void shouldHandleVeryLongEmailWithinLimits() {
            String longEmail = "a".repeat(200) + "@example.com";
            user.setEmail(longEmail);

            assertThat(user.getEmail()).isEqualTo(longEmail);
        }

        @Test
        @DisplayName("Should handle very long name within limits")
        void shouldHandleVeryLongNameWithinLimits() {
            String longName = "a".repeat(120);
            user.setName(longName);

            assertThat(user.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle very long password hash")
        void shouldHandleVeryLongPasswordHash() {
            String longHash = "a".repeat(1000);
            user.setPasswordHash(longHash);

            assertThat(user.getPasswordHash()).isEqualTo(longHash);
        }

        @Test
        @DisplayName("Should handle special characters in email")
        void shouldHandleSpecialCharactersInEmail() {
            String specialEmail = "test+tag@example-domain.co.uk";
            user.setEmail(specialEmail);

            assertThat(user.getEmail()).isEqualTo(specialEmail);
        }

        @Test
        @DisplayName("Should handle special characters in name")
        void shouldHandleSpecialCharactersInName() {
            String specialName = "José María O'Connor-Smith";
            user.setName(specialName);

            assertThat(user.getName()).isEqualTo(specialName);
        }
    }

    @Nested
    @DisplayName("Role Edge Cases Tests")
    class RoleEdgeCasesTests {

        @Test
        @DisplayName("Should handle role with special characters")
        void shouldHandleRoleWithSpecialCharacters() {
            user.addRole("USER_SPECIAL_CHARS_123");
            assertThat(user.getRoles()).contains("ROLE_USER_SPECIAL_CHARS_123");
        }

        @Test
        @DisplayName("Should handle role with underscores")
        void shouldHandleRoleWithUnderscores() {
            user.addRole("USER_ADMIN");
            assertThat(user.getRoles()).contains("ROLE_USER_ADMIN");
        }

        @Test
        @DisplayName("Should handle role with numbers")
        void shouldHandleRoleWithNumbers() {
            user.addRole("USER_123");
            assertThat(user.getRoles()).contains("ROLE_USER_123");
        }

        @Test
        @DisplayName("Should handle multiple calls to addRole with same role")
        void shouldHandleMultipleCallsToAddRoleWithSameRole() {
            user.addRole("USER");
            user.addRole("USER");
            user.addRole("USER");

            assertThat(user.getRoles()).hasSize(1);
            assertThat(user.getRoles()).contains("ROLE_USER");
        }
    }
}
