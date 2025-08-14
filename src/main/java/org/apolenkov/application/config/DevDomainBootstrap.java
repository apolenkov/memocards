package org.apolenkov.application.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"dev"})
class DevDomainBootstrap {

    @Bean
    @Order(10)
    CommandLineRunner ensureDomainUsers(
            UserRepository users, RoleAuditRepository audit, PasswordEncoder passwordEncoder) {
        return args -> {
            syncUser(users, audit, passwordEncoder, "user", "Password1", Set.of(SecurityConstants.ROLE_USER));
            syncUser(users, audit, passwordEncoder, "admin", "admin", Set.of(SecurityConstants.ROLE_ADMIN));
        };
    }

    private void syncUser(
            UserRepository users,
            RoleAuditRepository audit,
            PasswordEncoder passwordEncoder,
            String email,
            String rawPassword,
            Set<String> desiredRoles) {
        Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            User created = new User(null, email, capitalize(email));
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            created = users.save(created);
            audit.recordChange("system", created.getId(), Set.of(), created.getRoles(), LocalDateTime.now());
            return;
        }

        User existing = existingOpt.get();
        // In dev profile always enforce known password for convenience
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        if (!existing.getRoles().equals(desiredRoles)) {
            Set<String> before = new HashSet<>(existing.getRoles());
            existing.setRoles(new HashSet<>(desiredRoles));
            users.save(existing);
            audit.recordChange("system", existing.getId(), before, existing.getRoles(), LocalDateTime.now());
        }
        users.save(existing);
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
