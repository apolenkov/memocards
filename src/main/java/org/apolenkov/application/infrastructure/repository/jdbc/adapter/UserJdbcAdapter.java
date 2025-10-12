package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.config.cache.CacheConfiguration;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.dto.UserDto;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.UserPersistenceException;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.UserRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.UserSqlQueries;
import org.apolenkov.application.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for user repository operations.
 *
 * <p>Implements UserRepository using direct JDBC operations.
 * Provides CRUD operations for users with role management.
 * Active in JDBC profiles only.</p>
 */
@Profile({"dev", "prod", "test"})
@Repository
public class UserJdbcAdapter implements UserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserJdbcAdapter.class);
    /**
     * RowMapper for UserDto.
     */
    private static final RowMapper<UserDto> USER_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String name = rs.getString("name");
        Timestamp createdAt = rs.getTimestamp("created_at");

        return UserDto.forExistingUser(
                id,
                email,
                passwordHash,
                name,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                new HashSet<>() // Roles will be loaded separately
                );
    };

    /**
     * RowMapper for UserDto with roles (optimized single query).
     * Extracts user data and roles from a single result set using PostgreSQL array.
     */
    private static final RowMapper<UserDto> USER_WITH_ROLES_ROW_MAPPER = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String name = rs.getString("name");
        Timestamp createdAt = rs.getTimestamp("created_at");

        // Extract roles from PostgreSQL array
        java.sql.Array rolesArray = rs.getArray("roles");
        Set<String> roles = new HashSet<>();
        if (rolesArray != null) {
            String[] rolesData = (String[]) rolesArray.getArray();
            if (rolesData != null) {
                roles.addAll(List.of(rolesData));
            }
        }

        return UserDto.forExistingUser(
                id, email, passwordHash, name, createdAt != null ? createdAt.toLocalDateTime() : null, roles);
    };

    /**
     * RowMapper for user roles.
     */
    private static final RowMapper<String> ROLE_ROW_MAPPER = (rs, rowNum) -> rs.getString("role");

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates adapter with JdbcTemplate dependency.
     *
     * @param jdbcTemplateValue the JdbcTemplate for database operations
     * @throws IllegalArgumentException if jdbcTemplate is null
     */
    public UserJdbcAdapter(final JdbcTemplate jdbcTemplateValue) {
        if (jdbcTemplateValue == null) {
            throw new IllegalArgumentException("JdbcTemplate cannot be null");
        }
        this.jdbcTemplate = jdbcTemplateValue;
    }

    /**
     * Converts UserDto to domain User model.
     *
     * @param userDto DTO to convert
     * @return corresponding domain model
     */
    private static User toModel(final UserDto userDto) {
        final User user = new User(userDto.id(), userDto.email(), userDto.name());
        user.setPasswordHash(userDto.passwordHash());
        user.setCreatedAt(userDto.createdAt());
        user.setRoles(userDto.roles());
        return user;
    }

    /**
     * Retrieves all users from database.
     *
     * @return list of all users
     */
    @Override
    public List<User> findAll() {
        LOGGER.debug("Retrieving all users");
        try {
            List<UserDto> userDtos = jdbcTemplate.query(UserSqlQueries.SELECT_ALL_USERS, USER_ROW_MAPPER);
            return userDtos.stream()
                    .map(this::loadUserRoles)
                    .map(UserJdbcAdapter::toModel)
                    .toList();
        } catch (DataAccessException e) {
            throw new UserRetrievalException("Failed to retrieve all users", e);
        }
    }

    /**
     * Retrieves user by unique identifier.
     * Uses optimized single query with JOIN to fetch user and roles together.
     *
     * @param id unique identifier of user
     * @return Optional containing user if found
     */
    @Override
    public Optional<User> findById(final long id) {
        LOGGER.debug("Retrieving user by ID: {}", id);
        try {
            List<UserDto> users =
                    jdbcTemplate.query(UserSqlQueries.SELECT_USER_WITH_ROLES_BY_ID, USER_WITH_ROLES_ROW_MAPPER, id);
            if (users.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(users.getFirst()));
        } catch (DataAccessException e) {
            throw new UserRetrievalException("Failed to retrieve user by ID: " + id, e);
        }
    }

    /**
     * Retrieves user by email address.
     * Uses optimized single query with JOIN to fetch user and roles together.
     * Results are cached using Caffeine cache with 30-minute TTL.
     * Caches both present and empty Optional to avoid repeated DB queries.
     *
     * @param email email address of user
     * @return Optional containing user if found
     * @throws IllegalArgumentException if email is null or empty
     */
    @Override
    @Cacheable(value = CacheConfiguration.USER_BY_EMAIL_CACHE, key = "#email")
    public Optional<User> findByEmail(final String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        LOGGER.debug("Retrieving user by email from database: {}", email);
        try {
            List<UserDto> users = jdbcTemplate.query(
                    UserSqlQueries.SELECT_USER_WITH_ROLES_BY_EMAIL, USER_WITH_ROLES_ROW_MAPPER, email);
            if (users.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(toModel(users.getFirst()));
        } catch (DataAccessException e) {
            throw new UserRetrievalException("Failed to retrieve user by email: " + email, e);
        }
    }

    /**
     * Saves user to database.
     * Evicts entire user cache to handle email changes correctly.
     * Uses allEntries eviction because user email might change (old email would remain cached).
     *
     * @param user user to save
     * @return saved user with updated fields
     * @throws IllegalArgumentException if user is null
     */
    @Override
    @CacheEvict(value = CacheConfiguration.USER_BY_EMAIL_CACHE, allEntries = true)
    public User save(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean isNew = user.getId() == null;
        LOGGER.debug("Saving user: email={}, isNew={}", user.getEmail(), isNew);

        try {
            User saved = isNew ? createUser(user) : updateUser(user);
            LOGGER.debug(
                    "User saved: id={}, email={}, isNew={}, cache evicted", saved.getId(), saved.getEmail(), isNew);
            return saved;
        } catch (DataAccessException e) {
            throw new UserPersistenceException("Failed to save user: " + user.getEmail(), e);
        }
    }

    /**
     * Deletes user by unique identifier.
     * Clears entire user cache to ensure consistency.
     *
     * @param id unique identifier of user to delete
     */
    @Override
    @CacheEvict(value = CacheConfiguration.USER_BY_EMAIL_CACHE, allEntries = true)
    public void deleteById(final long id) {
        LOGGER.debug("Deleting user by ID: {}", id);
        try {
            // Delete user roles first (foreign key constraint)
            jdbcTemplate.update(UserSqlQueries.DELETE_USER_ROLES, id);

            // Delete user
            int deleted = jdbcTemplate.update(UserSqlQueries.DELETE_USER, id);
            if (deleted == 0) {
                LOGGER.warn("No user found with ID: {}", id);
            } else {
                LOGGER.debug("User deleted from database: id={}, cache cleared", id);
            }
        } catch (DataAccessException e) {
            throw new UserPersistenceException("Failed to delete user by ID: " + id, e);
        }
    }

    /**
     * Creates new user in database.
     *
     * @param user user to create
     * @return created user with generated ID
     */
    private User createUser(final User user) {
        UserDto userDto = UserDto.forNewUser(user.getEmail(), user.getPasswordHash(), user.getName(), user.getRoles());

        // Insert user and get generated ID using RETURNING clause
        Long generatedId = jdbcTemplate.queryForObject(
                UserSqlQueries.INSERT_USER_RETURNING_ID,
                Long.class,
                userDto.email(),
                userDto.passwordHash(),
                userDto.name(),
                userDto.createdAt());

        // Insert roles
        insertUserRoles(generatedId, user.getRoles());

        // Return created user
        UserDto createdDto = UserDto.forExistingUser(
                generatedId,
                userDto.email(),
                userDto.passwordHash(),
                userDto.name(),
                userDto.createdAt(),
                user.getRoles());

        return toModel(createdDto);
    }

    /**
     * Updates existing user in database.
     *
     * @param user user to update
     * @return updated user
     */
    private User updateUser(final User user) {
        // Update user
        jdbcTemplate.update(
                UserSqlQueries.UPDATE_USER, user.getEmail(), user.getPasswordHash(), user.getName(), user.getId());

        // Update roles
        jdbcTemplate.update(UserSqlQueries.DELETE_USER_ROLES, user.getId());
        insertUserRoles(user.getId(), user.getRoles());

        return user;
    }

    /**
     * Inserts user roles into database.
     *
     * @param userId user ID
     * @param roles set of roles to insert
     */
    private void insertUserRoles(final Long userId, final Set<String> roles) {
        for (String role : roles) {
            jdbcTemplate.update(UserSqlQueries.INSERT_USER_ROLE, userId, role);
        }
    }

    /**
     * Loads user roles from database.
     *
     * @param userDto user DTO to load roles for
     * @return user DTO with loaded roles
     */
    private UserDto loadUserRoles(final UserDto userDto) {
        if (userDto.id() == null) {
            return userDto;
        }

        Set<String> roles =
                new HashSet<>(jdbcTemplate.query(UserSqlQueries.SELECT_USER_ROLES, ROLE_ROW_MAPPER, userDto.id()));

        return UserDto.forExistingUser(
                userDto.id(), userDto.email(), userDto.passwordHash(), userDto.name(), userDto.createdAt(), roles);
    }
}
