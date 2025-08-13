package org.apolenkov.application.service.user;

import org.apolenkov.application.application.usecase.UserUseCase;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@org.springframework.context.annotation.Primary
public class UserUseCaseService implements UserUseCase {

    private final UserRepository userRepository;

    public UserUseCaseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getCurrentUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No users initialized"));
    }
}


