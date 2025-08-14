package org.apolenkov.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** User model for flashcards application */
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

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(Long id, String email, String name) {
        this();
        this.id = id;
        setEmail(email);
        setName(name);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        this.email = email.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        this.name = name.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public void addRole(String role) {
        if (role != null && !role.isBlank()) {
            this.roles.add(role.startsWith("ROLE_") ? role : "ROLE_" + role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
