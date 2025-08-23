package org.apolenkov.application.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing a password reset token for secure password recovery.
 *
 * <p>Secure, temporary, single-use token linked to a specific user account.</p>
 */
public class PasswordResetToken {
    private Long id;
    private String token;
    private Long userId;
    private LocalDateTime expiresAt;
    private boolean used;

    /**
     * Default constructor for JPA and serialization.
     */
    public PasswordResetToken() {}

    /**
     * Creates password reset token.
     *
     * @param token secure token string
     * @param userId ID of the user requesting password reset
     * @param expiresAt expiration date and time for this token
     */
    public PasswordResetToken(String token, Long userId, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    // Getters and setters

    /**
     * Gets unique identifier.
     *
     * @return token ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets unique identifier.
     *
     * @param id token ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets secure token string.
     *
     * @return secure token string
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets secure token string.
     *
     * @param token secure token string to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets ID of the user associated with this token.
     *
     * @return user ID associated with this token
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets ID of the user associated with this token.
     *
     * @param userId user ID to associate with this token
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets expiration date and time.
     *
     * @return expiration date and time
     */
    @SuppressWarnings("unused")
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets expiration date and time.
     *
     * @param expiresAt expiration date and time to set
     */
    @SuppressWarnings("unused")
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Checks whether this token has been used.
     *
     * @return true if token has been used
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Sets whether this token has been used.
     *
     * @param used true to mark token as used
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Checks whether this token has expired.
     *
     * @return true if token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if this token is valid for use.
     *
     * <p>Valid if not expired, not used, and has a user ID.</p>
     *
     * @return true if token is valid for use
     */
    public boolean isValid() {
        return !isExpired() && !isUsed() && userId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" + "id="
                + id + ", token='"
                + (token != null ? "[HIDDEN]" : "null") + '\'' + ", userId="
                + userId + ", expiresAt="
                + expiresAt + ", used="
                + used + '}';
    }
}
