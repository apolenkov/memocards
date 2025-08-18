package org.apolenkov.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Hibernate configuration for performance optimization
 * Configures Hibernate properties via application.yml
 * Uses Spring Boot AutoConfiguration for beans
 */
@Configuration
@Profile({"dev", "jpa", "prod"})
public class HibernateConfig {
    // Hibernate properties are configured in application.yml
    // Spring Boot AutoConfiguration handles entityManagerFactory and transactionManager
}
