package org.apolenkov.application.config;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile({"dev"})
class DevDomainBootstrap {

    private final MessageSource messageSource;

    public DevDomainBootstrap(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    @Order(10)
    CommandLineRunner ensureDomainUsers(
            UserRepository users, RoleAuditRepository audit, PasswordEncoder passwordEncoder) {
        return args -> {
            // Use localized names
            String userName = messageSource.getMessage(
                    "dev.user.user.name", null, "Ivan Petrov", LocaleContextHolder.getLocale());
            String adminName = messageSource.getMessage(
                    "dev.user.admin.name", null, "Administrator", LocaleContextHolder.getLocale());

            syncUser(
                    users,
                    audit,
                    passwordEncoder,
                    "user@example.com",
                    "Password1",
                    userName,
                    Set.of(SecurityConstants.ROLE_USER));
            syncUser(
                    users,
                    audit,
                    passwordEncoder,
                    "admin@example.com",
                    "admin",
                    adminName,
                    Set.of(SecurityConstants.ROLE_ADMIN));
        };
    }

    private void syncUser(
            UserRepository users,
            RoleAuditRepository audit,
            PasswordEncoder passwordEncoder,
            String email,
            String rawPassword,
            String fullName,
            Set<String> desiredRoles) {
        Optional<User> existingOpt = users.findByEmail(email);
        if (existingOpt.isEmpty()) {
            User created = new User(null, email, fullName);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            desiredRoles.forEach(created::addRole);
            created = users.save(created);
            audit.recordChange("system", created.getId(), Set.of(), created.getRoles(), LocalDateTime.now());
            return;
        }

        User existing = existingOpt.get();
        // In dev profile always enforce known password and name for convenience
        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
        existing.setName(fullName);
        if (!existing.getRoles().equals(desiredRoles)) {
            Set<String> before = new HashSet<>(existing.getRoles());
            existing.setRoles(new HashSet<>(desiredRoles));
            users.save(existing);
            audit.recordChange("system", existing.getId(), before, existing.getRoles(), LocalDateTime.now());
        }
        users.save(existing);
    }
}
