package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserJpaRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for user repository operations.
 *
 * <p>Bridges domain layer and JPA persistence for user CRUD operations.
 * Active in dev/prod profiles only.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository repo;

    /**
     * Creates adapter with JPA repository dependency.
     *
     * @param repo the Spring Data repository for user entities
     */
    public UserJpaAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param e the JPA entity to convert
     * @return the corresponding domain User object
     */
    private static User toModel(UserEntity e) {
        User u = new User(e.getId(), e.getEmail(), e.getName());
        u.setPasswordHash(e.getPasswordHash());
        u.setCreatedAt(e.getCreatedAt());
        u.setRoles(new java.util.HashSet<>(e.getRoles()));
        return u;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param u the domain User object to convert
     * @return the corresponding JPA UserEntity
     */
    private static UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setName(u.getName());
        e.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt() : java.time.LocalDateTime.now());
        e.setRoles(new java.util.HashSet<>(u.getRoles()));
        return e;
    }

    /**
     * Gets all users from the repository.
     *
     * @return list of all users
     */
    @Override
    public List<User> findAll() {
        return repo.findAll().stream().map(UserJpaAdapter::toModel).toList();
    }

    /**
     * Gets a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return Optional containing the user if found
     */
    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(UserJpaAdapter::toModel);
    }

    /**
     * Gets a user by their email address.
     *
     * @param email the email address of the user
     * @return Optional containing the user if found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(UserJpaAdapter::toModel);
    }

    /**
     * Saves a user to the repository.
     *
     * @param user the user object to save
     * @return the saved user with updated values
     */
    @Override
    public User save(User user) {
        return toModel(repo.save(toEntity(user)));
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the unique identifier of the user to delete
     */
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
