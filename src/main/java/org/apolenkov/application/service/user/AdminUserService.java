package org.apolenkov.application.service.user;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.domain.port.RoleAuditRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for administrative user management operations with security policies and audit logging.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleAuditRepository roleAuditRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Creates a new AdminUserService with required dependencies.
     *
     * @param userRepository the repository for user persistence operations
     * @param roleAuditRepository the repository for tracking role changes
     * @param passwordEncoder the encoder for secure password hashing
     */
    public AdminUserService(
            UserRepository userRepository,
            RoleAuditRepository roleAuditRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleAuditRepository = roleAuditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gets all users in the system.
     * Returns a complete list of all registered users. This method is typically
     * used for administrative user management interfaces.
     *
     * @return a list of all users in the system
     */
    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * Gets a specific user by their unique identifier.
     * Returns an Optional containing the user if found, or an empty Optional
     * if no user exists with the specified ID.
     *
     * @param id the unique identifier of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Saves a user to the repository.
     * Persists the specified user object to the repository. If the user
     * is new, it will be created; if it exists, it will be updated.
     *
     * @param user the user object to save
     * @return the saved user object
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their unique identifier.
     * Removes the specified user from the system. The method includes safety
     * checks to prevent deletion of the last administrator in the system.
     *
     * @param id the unique identifier of the user to delete
     * @throws IllegalStateException if attempting to delete the last administrator
     */
    @Transactional
    public void delete(Long id) {
        var userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        // Check if user being deleted is an administrator
        boolean isAdmin = user.getRoles().contains(SecurityConstants.ROLE_ADMIN);
        if (isAdmin) {
            // Safety check: prevent deletion of the last administrator in the system
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(SecurityConstants.ROLE_ADMIN))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last administrator");
            }
        }
        userRepository.deleteById(id);
    }

    /**
     * Updates the roles for a specific user with sanitization and validation.
     *
     * @param userId the ID of the user to update roles for
     * @param roles the new set of roles to assign to the user
     * @return the updated user object
     */
    private User updateRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId).orElseThrow();
        // Define allowed roles and sanitize input
        Set<String> allowed = Set.of(SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN);

        Set<String> sanitized = new HashSet<>();
        for (String r : roles) {
            if (r == null || r.isBlank()) continue;
            // Auto-prepend ROLE_ prefix if missing (Spring Security convention)
            String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
            if (allowed.contains(role)) sanitized.add(role);
        }
        // Ensure user always has at least ROLE_USER
        if (sanitized.isEmpty()) {
            sanitized.add(SecurityConstants.ROLE_USER);
        }
        user.setRoles(sanitized);
        return userRepository.save(user);
    }

    /**
     * Updates user roles with comprehensive audit logging.
     *
     * @param adminEmail the email of the administrator making the change
     * @param userId the ID of the user whose roles are being updated
     * @param roles the new set of roles to assign
     */
    @Transactional
    public void updateRolesWithAudit(String adminEmail, Long userId, Set<String> roles) {
        Set<String> beforeRoles =
                new HashSet<>(userRepository.findById(userId).orElseThrow().getRoles());
        User after = updateRoles(userId, roles);
        roleAuditRepository.recordChange(
                adminEmail, userId, beforeRoles, after.getRoles(), java.time.LocalDateTime.now());
    }

    /**
     * Updates user information with audit logging and validation.
     *
     * @param adminEmail the email of the administrator making the change
     * @param userId the ID of the user to update
     * @param email the new email address (optional, null to keep existing)
     * @param name the new display name (optional, null to keep existing)
     * @param roles the new set of roles (optional, null to keep existing)
     * @param rawPasswordOrNull the new password (optional, null to keep existing)
     * @return the updated user object
     * @throws IllegalArgumentException if the password policy is violated
     */
    @Transactional
    public User updateUserWithAudit(
            String adminEmail, Long userId, String email, String name, Set<String> roles, String rawPasswordOrNull) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<String> beforeRoles = new HashSet<>(user.getRoles());

        // Update email and name if provided (null values are ignored)
        if (email != null && !email.isBlank()) user.setEmail(email);
        if (name != null && !name.isBlank()) user.setName(name);

        // Sanitize and validate roles - ensure only allowed roles are assigned
        Set<String> allowed = Set.of(SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN);
        Set<String> sanitized = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r == null || r.isBlank()) continue;
                // Auto-prepend ROLE_ prefix if missing (Spring Security convention)
                String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                if (allowed.contains(role)) sanitized.add(role);
            }
        }
        // Ensure user always has at least ROLE_USER
        if (sanitized.isEmpty()) sanitized.add(SecurityConstants.ROLE_USER);
        user.setRoles(sanitized);

        // Validate and update password if provided
        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            // Password policy: minimum 8 chars, must contain letters and numbers
            if (rawPasswordOrNull.length() < 8
                    || !rawPasswordOrNull.matches(".*[A-Za-z].*") // Must contain at least one letter
                    || !rawPasswordOrNull.matches(".*\\d.*")) { // Must contain at least one digit
                throw new IllegalArgumentException("Password policy violated");
            }
            user.setPasswordHash(passwordEncoder.encode(rawPasswordOrNull));
        }

        User saved = userRepository.save(user);
        // Record role changes in audit log only if roles actually changed
        if (!beforeRoles.equals(saved.getRoles())) {
            roleAuditRepository.recordChange(
                    adminEmail, userId, beforeRoles, saved.getRoles(), java.time.LocalDateTime.now());
        }
        return saved;
    }

    /**
     * Creates new user with validation, role assignment, and audit logging.
     *
     * @param adminEmail the email of the administrator creating the user
     * @param email the email address for the new user account
     * @param name the display name for the new user
     * @param rawPassword the plain text password for the new user
     * @param roles the initial set of roles to assign to the user
     * @return the newly created user object
     * @throws IllegalArgumentException if the user already exists or if the password policy is violated
     */
    @Transactional
    public User createUserWithAudit(
            String adminEmail, String email, String name, String rawPassword, Set<String> roles) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("User already exists");
        });

        // Enforce password policy: minimum 8 chars, must contain letters and numbers
        if (rawPassword == null
                || rawPassword.length() < 8
                || !rawPassword.matches(".*[A-Za-z].*") // Must contain at least one letter
                || !rawPassword.matches(".*\\d.*")) { // Must contain at least one digit
            throw new IllegalArgumentException("Password policy violated");
        }

        // Sanitize and validate roles - ensure only allowed roles are assigned
        Set<String> allowed = Set.of(SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN);
        Set<String> sanitized = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r == null || r.isBlank()) continue;
                // Auto-prepend ROLE_ prefix if missing (Spring Security convention)
                String role = r.startsWith("ROLE_") ? r : "ROLE_" + r;
                if (allowed.contains(role)) sanitized.add(role);
            }
        }
        // Ensure user always has at least ROLE_USER
        if (sanitized.isEmpty()) sanitized.add(SecurityConstants.ROLE_USER);

        org.apolenkov.application.model.User user = new org.apolenkov.application.model.User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(sanitized);
        User created = userRepository.save(user);
        // Record initial role assignment in audit log (empty set for "before" roles)
        roleAuditRepository.recordChange(
                adminEmail, created.getId(), Set.of(), created.getRoles(), java.time.LocalDateTime.now());
        return created;
    }
}
