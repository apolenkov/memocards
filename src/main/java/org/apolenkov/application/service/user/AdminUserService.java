package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleAuditRepository roleAuditRepository;

    public AdminUserService(UserRepository userRepository, RoleAuditRepository roleAuditRepository) {
        this.userRepository = userRepository;
        this.roleAuditRepository = roleAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId).orElseThrow();
        java.util.Set<String> allowed = java.util.Set.of(
                org.apolenkov.application.config.SecurityConstants.ROLE_USER,
                org.apolenkov.application.config.SecurityConstants.ROLE_ADMIN);
        java.util.Set<String> sanitized = new java.util.HashSet<>();
        for (String r : roles) {
            if (r == null || r.isBlank()) continue;
            String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
            if (allowed.contains(role)) sanitized.add(role);
        }
        if (sanitized.isEmpty()) {
            sanitized.add(org.apolenkov.application.config.SecurityConstants.ROLE_USER);
        }
        user.setRoles(sanitized);
        return userRepository.save(user);
    }

    @Transactional
    public void updateRolesWithAudit(String adminEmail, Long userId, Set<String> roles) {
        java.util.Set<String> beforeRoles = new java.util.HashSet<>(
                userRepository.findById(userId).orElseThrow().getRoles());
        User after = updateRoles(userId, roles);
        roleAuditRepository.recordChange(
                adminEmail, userId, beforeRoles, after.getRoles(), java.time.LocalDateTime.now());
    }
}
