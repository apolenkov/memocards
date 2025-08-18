package org.apolenkov.application.model;

import java.time.LocalDateTime;

/**
 * Base entity class providing common functionality for all domain entities.
 * Eliminates duplication of timestamp management and validation logic.
 */
public abstract class BaseEntity {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update the timestamp when entity is modified
     */
    protected void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validate that a required field is not null or empty
     */
    protected void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Validate that a required field is not null
     */
    protected void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    /**
     * Trim string value safely
     */
    protected String trimSafely(String value) {
        return value != null ? value.trim() : null;
    }

    /**
     * Set trimmed value and update timestamp
     */
    protected void setTrimmedValue(String value, String fieldName) {
        String trimmed = trimSafely(value);
        validateRequired(trimmed, fieldName);
        setFieldValue(trimmed, fieldName);
        updateTimestamp();
    }

    /**
     * Set optional trimmed value and update timestamp
     */
    protected void setOptionalTrimmedValue(String value, String fieldName) {
        String trimmed = trimSafely(value);
        setFieldValue(trimmed, fieldName);
        updateTimestamp();
    }

    /**
     * Abstract method to be implemented by subclasses
     */
    protected abstract void setFieldValue(String value, String fieldName);

    // Getters and setters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
