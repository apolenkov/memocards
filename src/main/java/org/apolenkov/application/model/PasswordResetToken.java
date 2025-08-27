package org.apolenkov.application.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing a password reset token for secure password recovery.
 * Secure, temporary, single-use token linked to a specific user account.
 */
public final class PasswordResetToken {
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
     * @param tokenValue secure token string
     * @param userIdValue ID of the user requesting password reset
     * @param expiresAtValue expiration date and time for this token
     */
    public PasswordResetToken(final String tokenValue, final Long userIdValue, final LocalDateTime expiresAtValue) {
        this.token = tokenValue;
        this.userId = userIdValue;
        this.expiresAt = expiresAtValue;
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
     * @param idValue token ID to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
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
     * @param tokenValue secure token string to set
     */
    public void setToken(final String tokenValue) {
        this.token = tokenValue;
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
     * @param userIdValue user ID to associate with this token
     */
    public void setUserId(final Long userIdValue) {
        this.userId = userIdValue;
    }

    /**
     * Gets expiration date and time.
     *
     * @return expiration date and time
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets expiration date and time.
     *
     * @param expiresAtValue expiration date and time to set
     */
    public void setExpiresAt(final LocalDateTime expiresAtValue) {
        this.expiresAt = expiresAtValue;
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
     * @param usedValue true to mark token as used
     */
    public void setUsed(final boolean usedValue) {
        this.used = usedValue;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
