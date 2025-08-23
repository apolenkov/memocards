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
     * Unique identifier for the user settings record.
     *
     * <p>This field serves as the primary key and is automatically generated
     * using the database's identity strategy. It provides a unique reference
     * for each user settings record in the system.</p>
     *
     * <p><strong>Generation Strategy:</strong> IDENTITY (auto-increment)</p>
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Primary key, non-nullable, unique</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the user these settings belong to.
     *
     * <p>This field establishes a one-to-one relationship between user settings
     * and a specific user. The unique index ensures that each user can have
     * only one settings record, preventing duplicate configurations.</p>
     *
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Non-nullable, foreign key reference, unique</p>
     * <p><strong>Relationship:</strong> One-to-one with UserEntity</p>
     * <p><strong>Index:</strong> idx_user_settings_user (unique)</p>
     * <p><strong>Business Rule:</strong> Must reference an existing user</p>
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Preferred locale code for the user's interface language.
     *
     * <p>This field stores the user's preferred language and regional settings
     * in ISO format (e.g., "en-US", "ru-RU", "de-DE"). It determines the
     * language of the user interface, date formats, number formats, and other
     * locale-specific content.</p>
     *
     * <p><strong>Database Type:</strong> VARCHAR</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Format:</strong> ISO 639-1 language code + ISO 3166-1 country code</p>
     * <p><strong>Examples:</strong> "en-US", "ru-RU", "de-DE", "fr-FR"</p>
     * <p><strong>Business Rule:</strong> Must be a valid locale code supported by the system</p>
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
     * Sets the unique identifier for this user settings record.
     *
     * <p>This method is typically called by the JPA framework during
     * entity lifecycle management. Manual setting should be avoided
     * to prevent conflicts with the auto-generation strategy.</p>
     *
     * @param id the unique identifier to set
     */
    public void setId(Long id) {
        this.id = id;
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
     * <p>This method establishes the relationship between the settings
     * and a specific user. The user must exist in the system before
     * this relationship can be established.</p>
     *
     * @param userId the user identifier to set, must not be null
     * @throws IllegalArgumentException if userId is null
     */
    public void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        this.userId = userId;
    }

    /**
     * Gets the preferred locale code for the user's interface language.
     *
     * <p>This method returns the user's preferred language and regional
     * settings that determine the localization of the user interface.</p>
     *
     * @return the preferred locale code, never null for persisted entities
     */
    public String getPreferredLocaleCode() {
        return preferredLocaleCode;
    }

    /**
     * Sets the preferred locale code for the user's interface language.
     *
     * <p>This method allows users to customize their interface language
     * and regional preferences. The locale code should be in valid ISO format
     * and supported by the system.</p>
     *
     * <p>If preferredLocaleCode is null or empty, it will be set to empty string
     * to indicate no preference (system default will be used).</p>
     *
     * @param preferredLocaleCode the preferred locale code to set, null/empty for no preference
     */
    public void setPreferredLocaleCode(String preferredLocaleCode) {
        this.preferredLocaleCode = (preferredLocaleCode != null) ? preferredLocaleCode.trim() : "";
    }
}
