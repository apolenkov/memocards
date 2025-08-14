package org.apolenkov.application.infrastructure.repository.jpa.springdata;

import org.apolenkov.application.infrastructure.repository.jpa.entity.RoleAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAuditJpaRepository extends JpaRepository<RoleAuditEntity, Long> {}
