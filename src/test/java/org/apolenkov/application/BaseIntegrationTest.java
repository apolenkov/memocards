package org.apolenkov.application;

import org.apolenkov.application.config.TestContainersConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using TestContainers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    private static TestContainersConfig.CustomPostgreSQLContainer container;

    /**
     * Initializes the PostgreSQL container before all tests.
     */
    @BeforeAll
    public static void init() {
        container = TestContainersConfig.CustomPostgreSQLContainer.getInstance();
        container.start();
    }

    /**
     * Shuts down the PostgreSQL container after all tests.
     */
    @AfterAll
    public static void shutdown() {
        // Use for all tests one container
    }

    /**
     * Configures dynamic properties for the test context.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
    }
}
