package org.apolenkov.application.application.usecase;

import org.apolenkov.application.model.User;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User getCurrentUser();
}


