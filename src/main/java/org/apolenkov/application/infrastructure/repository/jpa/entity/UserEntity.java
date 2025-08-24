package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA entity representing a user with authentication, personal details, and role-based access control.
 */
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 50)
    private Set<String> roles = new HashSet<>();

    /**
     * Gets the primary key identifier.
     *
     * @return the unique identifier for this user
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier.
     *
     * @param id the unique identifier for this user
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email address, unique across all users
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address, must be unique
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the hashed password.
     *
     * @return the password hash for authentication
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the hashed password.
     *
     * @param passwordHash the password hash for authentication
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the user's display name.
     *
     * @return the user's full name or display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     *
     * @param name the user's full name or display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the account creation timestamp.
     *
     * @return when this user account was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt when this user account was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * JPA lifecycle callback executed before entity persistence.
     *
     * <p>Automatically sets creation timestamp if it hasn't been explicitly set.
     * This ensures all new users have proper timestamp values.</p>
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /**
     * Gets the user's assigned roles.
     *
     * @return the set of role strings defining user permissions
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Sets the user's roles with null safety.
     *
     * <p>Ensures that the roles collection is never null by creating an empty
     * set if null is passed. This prevents NullPointerException in role-based
     * operations.</p>
     *
     * @param roles the set of roles to assign, or null for empty set
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    /**
     * Compares this entity with another object for equality.
     *
     * <p>Two UserEntity instances are considered equal if they have
     * the same ID. This is the standard approach for JPA entities
     * where identity is determined by the primary key.</p>
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Generates a hash code for this entity.
     *
     * <p>The hash code is based on the entity's ID, which is consistent
     * with the equals method implementation.</p>
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
