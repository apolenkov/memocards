package org.apolenkov.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the flashcards application.
 *
 * <p>Encapsulates user data including authentication credentials,
 * personal information, and roles for authorization.</p>
 */
public class User {
    private Long id;

    @Email
    @NotBlank
    private String email;

    @Size(max = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 120)
    private String name;

    private LocalDateTime createdAt;

    private Set<String> roles = new HashSet<>();

    /**
     * Creates new user with current timestamp.
     */
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new user with specified ID, email, and name.
     *
     * @param id unique identifier for the user
     * @param email email address of the user
     * @param name display name of the user
     */
    public User(Long id, String email, String name) {
        this();
        this.id = id;
        setEmail(email);
        setName(name);
    }

    /**
     * Returns user's unique identifier.
     *
     * @return user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets user's unique identifier.
     *
     * @param id user ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns user's email address.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets user's email address.
     *
     * @param email email address to set
     * @throws IllegalArgumentException if email is null or blank
     */
    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        this.email = email.trim();
    }

    /**
     * Returns user's password hash.
     *
     * @return password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets user's password hash.
     *
     * @param passwordHash password hash to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns user's display name.
     *
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets user's display name.
     *
     * @param name name to set
     * @throws IllegalArgumentException if name is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name.trim();
    }

    /**
     * Returns user's creation timestamp.
     *
     * @return creation date and time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets user's creation timestamp.
     *
     * @param createdAt creation date and time to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns unmodifiable view of user's roles.
     *
     * @return set of roles assigned to user
     */
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Sets roles for user.
     *
     * @param roles set of roles to assign to user
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    /**
     * Adds role to user.
     * Automatically prefixes role with "ROLE_" if not already present.
     *
     * @param role role to add
     */
    public void addRole(String role) {
        if (role != null && !role.isBlank()) {
            this.roles.add(role.startsWith("ROLE_") ? role : "ROLE_" + role);
        }
    }

    /**
     * Checks if this user equals another object.
     * Two users are considered equal if they have the same ID.
     *
     * @param o reference object to compare with
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Returns hash code based on user's ID.
     *
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns string representation of user.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "User{"
                + "id="
                + id
                + ", email='"
                + email
                + '\''
                + ", name='"
                + name
                + '\''
                + ", createdAt="
                + createdAt
                + '}';
    }
}
