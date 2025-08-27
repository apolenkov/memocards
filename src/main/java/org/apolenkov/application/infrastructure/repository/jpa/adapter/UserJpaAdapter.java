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
     * @param repoValue the Spring Data repository for user entities
     */
    public UserJpaAdapter(final UserJpaRepository repoValue) {
        this.repo = repoValue;
    }

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain User object
     */
    private static User toModel(final UserEntity entity) {
        final User user = new User(entity.getId(), entity.getEmail(), entity.getName());
        user.setPasswordHash(entity.getPasswordHash());
        user.setCreatedAt(entity.getCreatedAt());
        user.setRoles(new java.util.HashSet<>(entity.getRoles()));
        return user;
    }

    /**
     * Converts domain model to JPA entity with timestamp handling.
     *
     * @param user the domain User object to convert
     * @return the corresponding JPA UserEntity
     */
    private static UserEntity toEntity(final User user) {
        final UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setName(user.getName());
        entity.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : java.time.LocalDateTime.now());
        entity.setRoles(new java.util.HashSet<>(user.getRoles()));
        return entity;
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
    public Optional<User> findById(final Long id) {
        return repo.findById(id).map(UserJpaAdapter::toModel);
    }

    /**
     * Gets a user by their email address.
     *
     * @param email the email address of the user
     * @return Optional containing the user if found
     */
    @Override
    public Optional<User> findByEmail(final String email) {
        return repo.findByEmail(email).map(UserJpaAdapter::toModel);
    }

    /**
     * Saves a user to the repository.
     *
     * @param user the user object to save
     * @return the saved user with updated values
     */
    @Override
    public User save(final User user) {
        return toModel(repo.save(toEntity(user)));
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the unique identifier of the user to delete
     */
    @Override
    public void deleteById(final Long id) {
        repo.deleteById(id);
    }
}
