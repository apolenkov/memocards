package org.apolenkov.application.infrastructure.repository.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_audit")
public class RoleAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SuppressWarnings("unused")
    private Long id;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesBefore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rolesAfter;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public Long getId() {
        return id;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRolesBefore() {
        return rolesBefore;
    }

    public void setRolesBefore(String rolesBefore) {
        this.rolesBefore = rolesBefore;
    }

    public String getRolesAfter() {
        return rolesAfter;
    }

    public void setRolesAfter(String rolesAfter) {
        this.rolesAfter = rolesAfter;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
