package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.RoleAuditEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.RoleAuditJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for role audit operations.
 *
 * <p>Records and retrieves role change audit logs for security compliance.
 * Active in dev/prod profiles only.</p>
 */
@Repository
@Profile({"dev", "prod"})
public class RoleAuditJpaAdapter implements RoleAuditRepository {

    private final RoleAuditJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repo the Spring Data JPA repository for role audit operations
     * @throws IllegalArgumentException if repo is null
     */
    public RoleAuditJpaAdapter(RoleAuditJpaRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("RoleAuditJpaRepository cannot be null");
        }
        this.repo = repo;
    }

    /**
     * Records a role change in the audit log.
     *
     * @param adminEmail the email of the administrator who made the change
     * @param userId the ID of the user whose roles were changed
     * @param rolesBefore the set of roles before the change
     * @param rolesAfter the set of roles after the change
     * @param at the timestamp when the change occurred, null will use current time
     * @throws IllegalArgumentException if adminEmail is null/empty
     */
    @Override
    public void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at) {
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin email cannot be null or empty");
        }
        // If timestamp is null, use current time
        LocalDateTime timestamp = (at != null) ? at : LocalDateTime.now();
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        RoleAuditEntity e = new RoleAuditEntity();
        e.setAdminEmail(adminEmail.trim());
        e.setUserId(userId);
        e.setRolesBefore(String.join(",", rolesBefore != null ? rolesBefore : Set.of()));
        e.setRolesAfter(String.join(",", rolesAfter != null ? rolesAfter : Set.of()));
        e.setChangedAt(timestamp);
        repo.save(e);
    }

    /**
     * Retrieves all role audit records from the database.
     *
     * @return list of all role audit records
     */
    @Override
    public List<RoleAuditRecord> listAll() {
        return repo.findAll().stream()
                .map(e -> new RoleAuditRecord(
                        e.getAdminEmail(),
                        e.getUserId(),
                        parseRoles(e.getRolesBefore()),
                        parseRoles(e.getRolesAfter()),
                        e.getChangedAt()))
                .toList();
    }

    /**
     * Parses a comma-separated string of roles into a set.
     *
     * @param rolesString the comma-separated string of roles to parse
     * @return set of individual role strings, or empty set if input is null/empty
     */
    private static Set<String> parseRoles(String rolesString) {
        if (rolesString == null || rolesString.trim().isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(java.util.Arrays.asList(rolesString.split(",")));
    }
}
