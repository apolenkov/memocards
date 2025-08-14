package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.RoleAuditEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.RoleAuditJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"jpa", "prod"})
public class RoleAuditJpaAdapter implements RoleAuditRepository {

    private final RoleAuditJpaRepository repo;

    public RoleAuditJpaAdapter(RoleAuditJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at) {
        RoleAuditEntity e = new RoleAuditEntity();
        e.setAdminEmail(adminEmail);
        e.setUserId(userId);
        e.setRolesBefore(String.join(",", rolesBefore));
        e.setRolesAfter(String.join(",", rolesAfter));
        e.setChangedAt(at);
        repo.save(e);
    }

    @Override
    public List<RoleAuditRecord> listAll() {
        return repo.findAll().stream()
                .map(e -> new RoleAuditRecord(
                        e.getAdminEmail(),
                        e.getUserId(),
                        Set.copyOf(java.util.Arrays.asList(e.getRolesBefore().split(","))),
                        Set.copyOf(java.util.Arrays.asList(e.getRolesAfter().split(","))),
                        e.getChangedAt()))
                .collect(Collectors.toList());
    }
}
