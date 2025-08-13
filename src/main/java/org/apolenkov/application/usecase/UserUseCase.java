package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;

import org.apolenkov.application.model.User;

public interface UserUseCase {
  List<User> getAllUsers();

  Optional<User> getUserById(Long id);

  User getCurrentUser();
}
