package org.apolenkov.application.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Audit repository for tracking user role changes. */
public interface RoleAuditRepository {
    void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at);

    record RoleAuditRecord(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at) {}

    List<RoleAuditRecord> listAll();
}
