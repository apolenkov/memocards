package org.apolenkov.application.infrastructure.repository.jdbc.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * JDBC DTO for user data transfer operations.
 *
 * <p>Immutable record representing user data for JDBC operations.
 * Used for mapping between database rows and domain models.</p>
 *
 * @param id unique user identifier
 * @param email user's email address
 * @param passwordHash hashed password for authentication
 * @param name user's display name
 * @param createdAt account creation timestamp
 * @param roles set of user roles
 */
public record UserDto(
        Long id, String email, String passwordHash, String name, LocalDateTime createdAt, Set<String> roles) {

    /**
     * Creates UserDto with validation.
     *
     * @param id unique user identifier
     * @param email user's email address
     * @param passwordHash hashed password for authentication
     * @param name user's display name
     * @param createdAt account creation timestamp
     * @param roles set of user roles
     * @throws IllegalArgumentException if email or name is null/empty
     */
    public UserDto {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (roles == null) {
            throw new IllegalArgumentException("Roles cannot be null");
        }
    }

    /**
     * Creates UserDto for new user (without ID).
     *
     * @param email user's email address
     * @param passwordHash hashed password for authentication
     * @param name user's display name
     * @param roles set of user roles
     * @return UserDto for new user
     */
    public static UserDto forNewUser(
            final String email, final String passwordHash, final String name, final Set<String> roles) {
        return new UserDto(null, email, passwordHash, name, LocalDateTime.now(), roles);
    }

    /**
     * Creates UserDto for existing user.
     *
     * @param id unique user identifier
     * @param email user's email address
     * @param passwordHash hashed password for authentication
     * @param name user's display name
     * @param createdAt account creation timestamp
     * @param roles set of user roles
     * @return UserDto for existing user
     */
    public static UserDto forExistingUser(
            final Long id,
            final String email,
            final String passwordHash,
            final String name,
            final LocalDateTime createdAt,
            final Set<String> roles) {
        return new UserDto(id, email, passwordHash, name, createdAt, roles);
    }
}
