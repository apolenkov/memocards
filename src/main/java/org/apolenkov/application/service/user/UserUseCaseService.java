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
import org.springframework.stereotype.Service;

@Service
public class UserUseCaseService implements UserUseCase {

    private final UserRepository userRepository;

    public UserUseCaseService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
