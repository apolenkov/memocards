package org.apolenkov.application.service.user;

import java.util.List;
import java.util.Optional;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.stereotype.Service;

@Service
public class UserUseCaseService implements UserUseCase {

  private final UserRepository userRepository;

  public UserUseCaseService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public User getCurrentUser() {
    return userRepository.findAll().stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No users initialized"));
  }
}
