package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing audit trail for role changes in the system.
 *
 * <p>This entity maintains a comprehensive audit log of all role modifications
 * performed by administrators. It tracks who made changes, when they were made,
 * and what the roles were before and after the modification. This audit trail
 * is essential for security compliance, troubleshooting, and maintaining system
 * integrity.</p>
 *
 * <p>The entity captures the following audit information:</p>
 * <ul>
 *   <li><strong>Administrator Identity:</strong> Email of the admin who made the change</li>
 *   <li><strong>Target User:</strong> User whose roles were modified</li>
 *   <li><strong>Role Changes:</strong> Before and after role configurations</li>
 *   <li><strong>Timestamp:</strong> Exact time when the change occurred</li>
 * </ul>
 *
 * <p><strong>Database Mapping:</strong></p>
 * <ul>
 *   <li>Table: "role_audit"</li>
 *   <li>Primary Key: Auto-generated ID</li>
 *   <li>Text Fields: roles_before and roles_after use TEXT column type</li>
 *   <li>Constraints: All fields are non-nullable for complete audit trail</li>
 * </ul>
 *
 * <p><strong>Security and Compliance Features:</strong></p>
 * <ul>
 *   <li><strong>Non-Repudiation:</strong> Complete audit trail prevents denial of changes</li>
 *   <li><strong>Compliance Support:</strong> Meets regulatory requirements for access control</li>
 *   <li><strong>Forensic Analysis:</strong> Enables investigation of security incidents</li>
 *   <strong>Change Tracking:</strong> Monitors all role modifications in real-time</li>
 * </ul>
 *
 * <p><strong>Business Rules:</strong></p>
 * <ul>
 *   <li><strong>Complete Recording:</strong> All role changes must be logged</li>
 *   <li><strong>Immutable Records:</strong> Audit entries cannot be modified after creation</li>
 *   <strong>Data Retention:</strong> Audit logs maintained according to policy</li>
 *   <strong>Access Control:</strong> Audit data accessible only to authorized personnel</li>
 * </ul>
 *
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 * @see jakarta.persistence.Id
 * @see jakarta.persistence.GeneratedValue
 * @see jakarta.persistence.Column
 * @see java.time.LocalDateTime
 */
@Entity
@Table(name = "role_audit")
public class RoleAuditEntity {

    /**
     * Unique identifier for the role audit record.
     *
     * <p>This field serves as the primary key and is automatically generated
     * using the database's identity strategy. It provides a unique reference
     * for each audit entry in the system.</p>
     *
     * <p><strong>Generation Strategy:</strong> IDENTITY (auto-increment)</p>
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Primary key, non-nullable, unique</p>
     * <p><strong>Purpose:</strong> Enables efficient querying and referencing of audit records</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SuppressWarnings("unused")
    private Long id;

    /**
     * Email address of the administrator who performed the role change.
     *
     * <p>This field identifies the administrator responsible for modifying
     * user roles. It provides accountability and enables tracking of
     * administrative actions for security and compliance purposes.</p>
     *
     * <p><strong>Database Type:</strong> VARCHAR</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Format:</strong> Valid email address format</p>
     * <p><strong>Business Rule:</strong> Must be a valid administrator email</p>
     * <p><strong>Security:</strong> Used for access control and audit trail</p>
     */
    @Column(nullable = false)
    private String adminEmail;

    /**
     * Identifier of the user whose roles were modified.
     *
     * <p>This field references the target user whose role configuration
     * was changed. It enables tracking of role modifications for specific
     * users and supports user-specific audit reporting.</p>
     *
     * <p><strong>Database Type:</strong> BIGINT</p>
     * <p><strong>Constraints:</strong> Non-nullable, foreign key reference</p>
     * <p><strong>Relationship:</strong> Many-to-one with UserEntity</p>
     * <p><strong>Business Rule:</strong> Must reference an existing user</p>
     * <p><strong>Purpose:</strong> Identifies the subject of the role change</p>
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Role configuration before the modification.
     *
     * <p>This field stores the complete role configuration that existed
     * before the administrator made changes. It uses TEXT column type
     * to accommodate complex role structures and multiple roles per user.</p>
     *
     * <p><strong>Database Type:</strong> TEXT</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Content:</strong> JSON or comma-separated role list</p>
     * <p><strong>Purpose:</strong> Provides baseline for change comparison</p>
     * <p><strong>Audit Value:</strong> Essential for understanding what changed</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesBefore;

    /**
     * Role configuration after the modification.
     *
     * <p>This field stores the complete role configuration that resulted
     * from the administrator's changes. It uses TEXT column type to
     * accommodate complex role structures and multiple roles per user.</p>
     *
     * <p><strong>Database Type:</strong> TEXT</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Content:</strong> JSON or comma-separated role list</p>
     * <p><strong>Purpose:</strong> Shows the final state after changes</p>
     * <p><strong>Audit Value:</strong> Essential for understanding the outcome</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesAfter;

    /**
     * Timestamp when the role change occurred.
     *
     * <p>This field records the exact date and time when the role
     * modification was performed. It provides chronological ordering
     * of audit events and enables time-based reporting and analysis.</p>
     *
     * <p><strong>Database Type:</strong> TIMESTAMP</p>
     * <p><strong>Constraints:</strong> Non-nullable</p>
     * <p><strong>Format:</strong> ISO 8601 datetime format</p>
     * <p><strong>Precision:</strong> Includes date, time, and timezone information</p>
     * <p><strong>Purpose:</strong> Enables chronological audit trail and reporting</p>
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
     * <p>This method establishes the relationship between the audit record
     * and the target user. The user must exist in the system before
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
     * <p>This method records the baseline role configuration that existed
     * before the administrator made changes. It provides the starting point
     * for understanding what was modified.</p>
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
     * <p>This method records the final role configuration that resulted
     * from the administrator's changes. It provides the end point for
     * understanding what the modification achieved.</p>
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
     * Sets the timestamp when the role change occurred.
     *
     * <p>This method records the exact time when the role modification
     * was performed. It enables chronological ordering of audit events
     * and supports time-based reporting and analysis.</p>
     *
     * <p>If changedAt is null, it will be set to current time.</p>
     *
     * @param changedAt the timestamp of the change, null will be converted to current time
     */
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }
}
