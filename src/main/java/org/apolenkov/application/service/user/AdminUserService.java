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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            RoleAuditRepository roleAuditRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleAuditRepository = roleAuditRepository;
        this.passwordEncoder = passwordEncoder;
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
    public void delete(Long id) {
        var userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        boolean isAdmin = user.getRoles().contains(org.apolenkov.application.config.SecurityConstants.ROLE_ADMIN);
        if (isAdmin) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(org.apolenkov.application.config.SecurityConstants.ROLE_ADMIN))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last administrator");
            }
        }
        userRepository.deleteById(id);
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

    @Transactional
    public User updateUserWithAudit(
            String adminEmail, Long userId, String email, String name, Set<String> roles, String rawPasswordOrNull) {
        User user = userRepository.findById(userId).orElseThrow();
        java.util.Set<String> beforeRoles = new java.util.HashSet<>(user.getRoles());

        if (email != null && !email.isBlank()) user.setEmail(email);
        if (name != null && !name.isBlank()) user.setName(name);

        java.util.Set<String> allowed = java.util.Set.of(
                org.apolenkov.application.config.SecurityConstants.ROLE_USER,
                org.apolenkov.application.config.SecurityConstants.ROLE_ADMIN);
        java.util.Set<String> sanitized = new java.util.HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r == null || r.isBlank()) continue;
                String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                if (allowed.contains(role)) sanitized.add(role);
            }
        }
        if (sanitized.isEmpty()) sanitized.add(org.apolenkov.application.config.SecurityConstants.ROLE_USER);
        user.setRoles(sanitized);

        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            if (rawPasswordOrNull.length() < 8
                    || !rawPasswordOrNull.matches(".*[A-Za-z].*")
                    || !rawPasswordOrNull.matches(".*\\d.*")) {
                throw new IllegalArgumentException("Password policy violated");
            }
            user.setPasswordHash(passwordEncoder.encode(rawPasswordOrNull));
        }

        User saved = userRepository.save(user);
        if (!beforeRoles.equals(saved.getRoles())) {
            roleAuditRepository.recordChange(
                    adminEmail, userId, beforeRoles, saved.getRoles(), java.time.LocalDateTime.now());
        }
        return saved;
    }

    @Transactional
    public User createUserWithAudit(
            String adminEmail, String email, String name, String rawPassword, Set<String> roles) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("User already exists");
        });

        if (rawPassword == null
                || rawPassword.length() < 8
                || !rawPassword.matches(".*[A-Za-z].*")
                || !rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password policy violated");
        }

        java.util.Set<String> allowed = java.util.Set.of(
                org.apolenkov.application.config.SecurityConstants.ROLE_USER,
                org.apolenkov.application.config.SecurityConstants.ROLE_ADMIN);
        java.util.Set<String> sanitized = new java.util.HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r == null || r.isBlank()) continue;
                String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                if (allowed.contains(role)) sanitized.add(role);
            }
        }
        if (sanitized.isEmpty()) sanitized.add(org.apolenkov.application.config.SecurityConstants.ROLE_USER);

        org.apolenkov.application.model.User user = new org.apolenkov.application.model.User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(sanitized);
        User created = userRepository.save(user);
        roleAuditRepository.recordChange(
                adminEmail, created.getId(), java.util.Set.of(), created.getRoles(), java.time.LocalDateTime.now());
        return created;
    }
}
