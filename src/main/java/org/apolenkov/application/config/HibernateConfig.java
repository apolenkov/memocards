package org.apolenkov.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Hibernate configuration for performance optimization.
 *
 * <p>This configuration class provides Hibernate-specific settings and optimizations
 * for the application. It configures Hibernate properties through application.yml
 * configuration files and leverages Spring Boot's auto-configuration capabilities
 * for entity manager factory and transaction manager beans.</p>
 *
 * <p>The configuration is active in development, JPA, and production profiles
 * to ensure consistent Hibernate behavior across different environments.</p>
 *
 */
@Configuration
@Profile({"dev", "jpa", "prod"})
public class HibernateConfig {
    // Hibernate properties are configured in application.yml
    // Spring Boot AutoConfiguration handles entityManagerFactory and transactionManager
}
