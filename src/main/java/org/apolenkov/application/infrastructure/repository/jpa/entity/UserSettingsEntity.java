package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity for user-specific settings and preferences.
 *
 * <p>Stores locale preferences and other user configuration.
 * One-to-one relationship with users via unique index on user_id.</p>
 *
 * <p>Table: "user_settings"</p>
 */
@Entity
@Table(
        name = "user_settings",
        indexes = {@Index(name = "idx_user_settings_user", columnList = "user_id", unique = true)})
public class UserSettingsEntity {

    /**
     * Unique identifier for the user settings record (auto-generated primary key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the user these settings belong to (one-to-one relationship with unique constraint).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Preferred locale code for user's interface language in ISO format (e.g., "en-US", "ru-RU").
     */
    @Column(name = "preferred_locale_code", nullable = false)
    private String preferredLocaleCode;

    /**
     * Gets the unique identifier for this user settings record.
     *
     * @return the unique identifier, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this user settings record (use with caution).
     *
     * @param idValue the unique identifier to set
     */
    public void setId(final Long idValue) {
        this.id = idValue;
    }

    /**
     * Gets the identifier of the user these settings belong to.
     *
     * @return the user identifier, never null for persisted entities
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the identifier of the user these settings belong to.
     *
     * @param userIdValue the user identifier to set, must not be null
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(final Long userIdValue) {
        if (userIdValue == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.userId = userIdValue;
    }

    /**
     * Gets the preferred locale code for the user's interface language.
     *
     * @return the preferred locale code, never null for persisted entities
     */
    public String getPreferredLocaleCode() {
        return preferredLocaleCode;
    }

    /**
     * Sets the preferred locale code for the user's interface language.
     *
     * @param preferredLocaleCodeValue the preferred locale code to set, null/empty for no preference
     */
    public void setPreferredLocaleCode(final String preferredLocaleCodeValue) {
        this.preferredLocaleCode = (preferredLocaleCodeValue != null) ? preferredLocaleCodeValue.trim() : "";
    }
}
