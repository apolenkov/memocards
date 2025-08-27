package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stores temporary tokens for password reset functionality.
 * Each token is associated with a user and has an expiration time.
 * Table: "password_reset_tokens" with unique token constraint.
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
     * @param idValue the unique identifier for this token record
     */
    public void setId(final Long idValue) {
        this.id = idValue;
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
     * @param tokenValue the unique token for password reset
     */
    public void setToken(final String tokenValue) {
        this.token = tokenValue;
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
     * @param userIdValue the ID of the user requesting password reset
     */
    public void setUserId(final Long userIdValue) {
        this.userId = userIdValue;
    }

    /**
     * Gets the token expiration time.
     *
     * @return when this token expires
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the token expiration time.
     *
     * @param expiresAtValue when this token expires
     */
    public void setExpiresAt(final LocalDateTime expiresAtValue) {
        this.expiresAt = expiresAtValue;
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
     * @param usedValue true if token was used for password reset
     */
    public void setUsed(final boolean usedValue) {
        this.used = usedValue;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PasswordResetTokenEntity that = (PasswordResetTokenEntity) o;
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
