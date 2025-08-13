package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserJpaRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("jpa")
@Repository
public class UserJpaAdapter implements UserRepository {

  private final UserJpaRepository repo;

  public UserJpaAdapter(UserJpaRepository repo) {
    this.repo = repo;
  }

  private static User toModel(UserEntity e) {
    User u = new User(e.getId(), e.getEmail(), e.getName());
    u.setPasswordHash(e.getPasswordHash());
    u.setCreatedAt(e.getCreatedAt());
    return u;
  }

  private static UserEntity toEntity(User u) {
    UserEntity e = new UserEntity();
    e.setId(u.getId());
    e.setEmail(u.getEmail());
    e.setPasswordHash(u.getPasswordHash());
    e.setName(u.getName());
    e.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt() : java.time.LocalDateTime.now());
    return e;
  }

  @Override
  public List<User> findAll() {
    return repo.findAll().stream().map(UserJpaAdapter::toModel).collect(Collectors.toList());
  }

  @Override
  public Optional<User> findById(Long id) {
    return repo.findById(id).map(UserJpaAdapter::toModel);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repo.findByEmail(email).map(UserJpaAdapter::toModel);
  }

  @Override
  public User save(User user) {
    return toModel(repo.save(toEntity(user)));
  }
}
