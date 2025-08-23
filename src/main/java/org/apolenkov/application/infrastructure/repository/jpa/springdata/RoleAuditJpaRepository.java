package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import org.apolenkov.application.infrastructure.repository.jpa.entity.RoleAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for role audit records.
 *
 * <p>Manages audit trail for role changes, providing CRUD operations
 * for compliance and security monitoring.</p>
 */
public interface RoleAuditJpaRepository extends JpaRepository<RoleAuditEntity, Long> {}
