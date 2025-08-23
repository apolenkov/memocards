package org.apolenkov.application.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Domain port for auditing user role changes.
 *
 * <p>Defines contract for tracking and retrieving audit records
 * of role modifications for compliance and security monitoring.</p>
 */
public interface RoleAuditRepository {

    /**
     * Records role change operation in audit log.
     *
     * @param adminEmail email of administrator making change
     * @param userId identifier of user whose roles were changed
     * @param rolesBefore user's roles before change
     * @param rolesAfter user's roles after change
     * @param at timestamp when change occurred
     */
    void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at);

    /**
     * Record representing role change audit entry.
     *
     * @param adminEmail email of administrator making change
     * @param userId identifier of user whose roles were changed
     * @param rolesBefore user's roles before change
     * @param rolesAfter user's roles after change
     * @param at timestamp when change occurred
     */
    record RoleAuditRecord(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at) {}

    /**
     * Retrieves all role change audit records.
     *
     * @return list of all role change audit records
     */
    List<RoleAuditRecord> listAll();
}
