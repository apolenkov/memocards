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
 * JPA adapter implementation for user repository operations.
 *
 * <p>This adapter provides the bridge between the domain layer and JPA-based
 * data persistence. It implements the UserRepository interface and handles
 * the conversion between domain User objects and JPA UserEntity objects.</p>
 *
 * <p>The adapter is responsible for:</p>
 * <ul>
 *   <li>Converting between domain models and JPA entities</li>
 *   <li>Delegating CRUD operations to Spring Data repositories</li>
 *   <li>Ensuring proper data transformation and validation</li>
 *   <li>Maintaining consistency between domain and persistence layers</li>
 * </ul>
 *
 * <p>This implementation is only active in development and production
 * profiles, allowing for different repository implementations in testing.</p>
 */
@Profile({"dev", "prod"})
@Repository
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository repo;

    /**
     * Constructs a new UserJpaAdapter with the required repository dependency.
     *
     * @param repo the Spring Data repository for user entities
     */
    public UserJpaAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    /**
     * Converts a JPA UserEntity to a domain User object.
     *
     * <p>Maps all entity fields to the corresponding domain model properties,
     * ensuring proper data transformation and null safety.</p>
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
     * Converts a domain User object to a JPA UserEntity.
     *
     * <p>Maps all domain model properties to the corresponding entity fields,
     * with proper handling of optional values and default assignments.</p>
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
     * Retrieves all users from the repository.
     *
     * <p>Fetches all user entities and converts them to domain objects.
     * This method should be used with caution in production environments
     * due to potential memory and performance implications.</p>
     *
     * @return a list of all users in the system
     */
    @Override
    public List<User> findAll() {
        return repo.findAll().stream().map(UserJpaAdapter::toModel).toList();
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * <p>Looks up a user entity by ID and converts it to a domain object.
     * Returns an empty Optional if no user is found with the specified ID.</p>
     *
     * @param id the unique identifier of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(UserJpaAdapter::toModel);
    }

    /**
     * Retrieves a user by their email address.
     *
     * <p>Looks up a user entity by email and converts it to a domain object.
     * Returns an empty Optional if no user is found with the specified email.</p>
     *
     * @param email the email address of the user to retrieve
     * @return an Optional containing the user if found, empty otherwise
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(UserJpaAdapter::toModel);
    }

    /**
     * Saves a user to the repository.
     *
     * <p>Converts the domain User object to an entity, persists it using
     * the JPA repository, and returns the updated domain object with
     * any generated values (such as ID or timestamps).</p>
     *
     * @param user the user object to save
     * @return the saved user with updated values
     */
    @Override
    public User save(User user) {
        return toModel(repo.save(toEntity(user)));
    }

    /**
     * Deletes a user from the repository by their ID.
     *
     * <p>Removes the user entity with the specified ID from the database.
     * If no user exists with the given ID, the operation completes silently.</p>
     *
     * @param id the unique identifier of the user to delete
     */
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
