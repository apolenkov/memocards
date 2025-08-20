package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.TransactionAnnotations;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserUseCaseService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserUseCaseService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }
        return userRepository
                .findByEmail(username)
                .orElseThrow(
                        () -> new IllegalStateException("Authenticated principal has no domain user: " + username));
    }

    @TransactionAnnotations.WriteTransaction
    public void updateCurrentUserName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        User currentUser = getCurrentUser();
        currentUser.setName(newName.trim());
        userRepository.save(currentUser);
    }

    @TransactionAnnotations.WriteTransaction
    public void updateCurrentUserEmail(String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        User currentUser = getCurrentUser();
        currentUser.setEmail(newEmail.trim());
        userRepository.save(currentUser);
    }

    @TransactionAnnotations.WriteTransaction
    public void updateCurrentUserPassword(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User currentUser = getCurrentUser();

        // Check current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        String newPasswordHash = passwordEncoder.encode(newPassword);
        currentUser.setPasswordHash(newPasswordHash);
        userRepository.save(currentUser);
    }
}
