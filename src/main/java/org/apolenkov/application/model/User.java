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
 * <p>This class encapsulates user data including authentication credentials,
 * personal information, and roles for authorization purposes.</p>
 *
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
     * Default constructor.
     * Initializes a new user with current timestamp.
     */
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new User with the specified id, email, and name.
     *
     * @param id the unique identifier for the user
     * @param email the email address of the user
     * @param name the display name of the user
     */
    public User(Long id, String email, String name) {
        this();
        this.id = id;
        setEmail(email);
        setName(name);
    }

    /**
     * Returns the unique identifier of this user.
     *
     * @return the user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this user.
     *
     * @param id the user ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the email address of this user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of this user.
     *
     * @param email the email address to set
     * @throws IllegalArgumentException if email is null or blank
     */
    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        this.email = email.trim();
    }

    /**
     * Returns the password hash of this user.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash of this user.
     *
     * @param passwordHash the password hash to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the display name of this user.
     *
     * @return the user name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this user.
     *
     * @param name the name to set
     * @throws IllegalArgumentException if name is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name.trim();
    }

    /**
     * Returns the creation timestamp of this user.
     *
     * @return the creation date and time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this user.
     *
     * @param createdAt the creation date and time to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns an unmodifiable view of the user's roles.
     *
     * @return the set of roles assigned to this user
     */
    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Sets the roles for this user.
     *
     * @param roles the set of roles to assign to this user
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    /**
     * Adds a role to this user.
     * Automatically prefixes the role with "ROLE_" if not already present.
     *
     * @param role the role to add
     */
    public void addRole(String role) {
        if (role != null && !role.isBlank()) {
            this.roles.add(role.startsWith("ROLE_") ? role : "ROLE_" + role);
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two users are considered equal if they have the same ID.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Returns a hash code value for this user.
     * The hash code is based on the user's ID.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this user.
     *
     * @return a string representation of the object
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
