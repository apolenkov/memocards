package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing audit trail for role changes in the system.
 * Maintains audit log of role modifications with before/after states.
 */
@Entity
@Table(name = "role_audit")
public class RoleAuditEntity {

    /**
     * Unique identifier for the role audit record.
     * This field serves as the primary key and is automatically generated
     * using the database's identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SuppressWarnings("unused") // IDE Community problem
    private Long id;

    /**
     * Email address of the administrator who performed the role change.
     * This field identifies the administrator responsible for modifying
     * user roles for accountability and tracking.
     */
    @Column(nullable = false)
    private String adminEmail;

    /**
     * Identifier of the user whose roles were modified.
     * This field references the target user whose role configuration
     * was changed for tracking and reporting purposes.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Role configuration before the modification.
     * This field stores the complete role configuration that existed
     * before the administrator made changes.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesBefore;

    /**
     * Role configuration after the modification.
     * This field stores the complete role configuration that resulted
     * from the administrator's changes.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesAfter;

    /**
     * Timestamp when the role change occurred.
     * This field records the exact date and time when the role
     * modification was performed for chronological ordering.
     */
    @Column(nullable = false)
    private LocalDateTime changedAt;

    /**
     * Gets the unique identifier for this role audit record.
     *
     * @return the unique identifier, or null if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the email address of the administrator who performed the role change.
     *
     * @return the administrator's email address, never null for persisted entities
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * Sets the email address of the administrator who performed the role change.
     *
     * <p>This method records the identity of the administrator responsible
     * for the role modification. The email must be valid and belong to
     * an authorized administrator.</p>
     *
     * @param adminEmail the administrator's email address, must not be null or empty
     * @throws IllegalArgumentException if adminEmail is null or empty
     */
    public void setAdminEmail(String adminEmail) {
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin email cannot be null or empty");
        }
        this.adminEmail = adminEmail.trim();
    }

    /**
     * Gets the identifier of the user whose roles were modified.
     *
     * @return the user identifier, never null for persisted entities
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the identifier of the user whose roles were modified.
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
     * Gets the role configuration before the modification.
     *
     * @return the roles before the change, never null for persisted entities
     */
    public String getRolesBefore() {
        return rolesBefore;
    }

    /**
     * Sets the role configuration before the modification.
     *
     * @param rolesBefore the roles before the change, must not be null
     * @throws IllegalArgumentException if rolesBefore is null
     */
    public void setRolesBefore(String rolesBefore) {
        if (rolesBefore == null) {
            throw new IllegalArgumentException("Roles before cannot be null");
        }
        this.rolesBefore = rolesBefore;
    }

    /**
     * Gets the role configuration after the modification.
     *
     * @return the roles after the change, never null for persisted entities
     */
    public String getRolesAfter() {
        return rolesAfter;
    }

    /**
     * Sets the role configuration after the modification.
     *
     * @param rolesAfter the roles after the change, must not be null
     * @throws IllegalArgumentException if rolesAfter is null
     */
    public void setRolesAfter(String rolesAfter) {
        if (rolesAfter == null) {
            throw new IllegalArgumentException("Roles after cannot be null");
        }
        this.rolesAfter = rolesAfter;
    }

    /**
     * Gets the timestamp when the role change occurred.
     *
     * @return the timestamp of the change, never null for persisted entities
     */
    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    /**
     * Sets the timestamp when the role change occurred (null converts to current time).
     *
     * @param changedAt the timestamp of the change, null will be converted to current time
     */
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }
}
