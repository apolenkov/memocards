package org.apolenkov.application.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Domain port for auditing user role changes.
 *
 * <p>Defines the contract for tracking and retrieving audit records
 * of role modifications for compliance and security monitoring.</p>
 */
public interface RoleAuditRepository {
    
    /**
     * Records a role change operation in the audit log.
     *
     * @param adminEmail the email of the administrator making the change
     * @param userId the identifier of the user whose roles were changed
     * @param rolesBefore the user's roles before the change
     * @param rolesAfter the user's roles after the change
     * @param at the timestamp when the change occurred
     */
    void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at);

    /**
     * Record representing a role change audit entry.
     *
     * @param adminEmail the email of the administrator making the change
     * @param userId the identifier of the user whose roles were changed
     * @param rolesBefore the user's roles before the change
     * @param rolesAfter the user's roles after the change
     * @param at the timestamp when the change occurred
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
