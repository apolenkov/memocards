package org.apolenkov.application.service.user;

/**
 * Service interface for user registration operations.
 * Provides abstraction for user registration regardless of persistence technology.
 */
public interface RegistrationService {

    /**
     * Registers a new user with the specified credentials.
     *
     * @param email user's email address
     * @param name user's display name
     * @param rawPassword plain text password to be hashed
     * @throws IllegalArgumentException if any parameter is invalid
     */
    void register(String email, String name, String rawPassword);
}
