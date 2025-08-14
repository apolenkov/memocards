package org.apolenkov.application.infrastructure.repository.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"memory", "dev"})
public class InMemoryRoleAuditRepository implements RoleAuditRepository {

    private final List<RoleAuditRecord> store = new ArrayList<>();

    @Override
    public void recordChange(
            String adminEmail, long userId, Set<String> rolesBefore, Set<String> rolesAfter, LocalDateTime at) {
        store.add(new RoleAuditRecord(adminEmail, userId, rolesBefore, rolesAfter, at));
    }

    @Override
    public List<RoleAuditRecord> listAll() {
        return List.copyOf(store);
    }
}
