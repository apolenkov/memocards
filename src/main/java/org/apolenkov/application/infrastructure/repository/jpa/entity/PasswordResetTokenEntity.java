package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity representing a password reset token.
 *
 * <p>Stores temporary tokens for password reset functionality.
 * Each token is associated with a user and has an expiration time.</p>
 *
 * <p>Table: "password_reset_tokens" with unique token constraint.</p>
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    // Getters and setters
    /**
     * Gets the primary key identifier.
     *
     * @return the unique identifier for this token record
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier.
     *
     * @param id the unique identifier for this token record
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the reset token string.
     *
     * @return the unique token for password reset
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the reset token string.
     *
     * @param token the unique token for password reset
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the user identifier.
     *
     * @return the ID of the user requesting password reset
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user identifier.
     *
     * @param userId the ID of the user requesting password reset
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the token expiration time.
     *
     * @return when this token expires
     */
    @SuppressWarnings("unused")
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the token expiration time.
     *
     * @param expiresAt when this token expires
     */
    @SuppressWarnings("unused")
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Checks if the token has been used.
     *
     * @return true if token was already used for password reset
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Sets the token usage status.
     *
     * @param used true if token was used for password reset
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Compares this entity with another object for equality.
     *
     * <p>Two PasswordResetTokenEntity instances are considered equal if they have
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
        PasswordResetTokenEntity that = (PasswordResetTokenEntity) o;
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
