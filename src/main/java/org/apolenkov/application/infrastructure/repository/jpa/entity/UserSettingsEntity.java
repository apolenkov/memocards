package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "user_settings",
        indexes = {@Index(name = "idx_user_settings_user", columnList = "user_id", unique = true)})
public class UserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "preferred_locale_code", nullable = false)
    private String preferredLocaleCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPreferredLocaleCode() {
        return preferredLocaleCode;
    }

    public void setPreferredLocaleCode(String preferredLocaleCode) {
        this.preferredLocaleCode = preferredLocaleCode;
    }
}
