package org.apolenkov.application.service.user;

import java.time.LocalDateTime;
import java.util.Set;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-based implementation of user registration service.
 *
 * <p>This service handles user registration functionality using JPA repositories
 * for data persistence. It provides secure user account creation with password
 * hashing and role assignment. The service also maintains an audit trail of
 * role changes for security and compliance purposes.</p>
 *
 * <p>The service is only active in specific profiles (dev, prod) to allow
 * for different registration strategies in different environments.</p>
 *
 */
@Service
@Profile({"dev", "prod"})
public class JpaRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleAuditRepository roleAuditRepository;

    /**
     * Creates a new JpaRegistrationService with required dependencies.
     *
     * @param userRepository the repository for user persistence operations
     * @param passwordEncoder the encoder for secure password hashing
     * @param roleAuditRepository the repository for tracking role changes
     */
    public JpaRegistrationService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, RoleAuditRepository roleAuditRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleAuditRepository = roleAuditRepository;
    }

    /**
     * Registers a new user account.
     *
     * <p>Creates a new user account with the specified email, name, and password.
     * The password is securely hashed before storage, and the user is automatically
     * assigned the default USER role. The service also records the role assignment
     * in the audit log for security tracking.</p>
     *
     * <p>This method performs validation to ensure the email is not already
     * registered in the system.</p>
     *
     * @param email the email address for the new user account
     * @param name the display name for the new user
     * @param rawPassword the plain text password to be hashed and stored
     * @throws IllegalArgumentException if a user with the specified email already exists
     */
    @Transactional
    public void register(String email, String name, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("User already exists");
        });
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.addRole(org.apolenkov.application.config.SecurityConstants.ROLE_USER);
        User created = userRepository.save(user);
        roleAuditRepository.recordChange(
                "self-register", created.getId(), Set.of(), created.getRoles(), LocalDateTime.now());
    }
}
