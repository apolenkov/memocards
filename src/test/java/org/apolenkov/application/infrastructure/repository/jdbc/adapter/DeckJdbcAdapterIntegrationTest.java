package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.apolenkov.application.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for DeckJdbcAdapter using TestContainers.
 * This test demonstrates that TestContainers are working correctly.
 */
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=true"})
@DisplayName("DeckJdbcAdapter Integration Tests")
class DeckJdbcAdapterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should connect to PostgreSQL container successfully")
    void shouldConnectToPostgreSQLContainer() {
        // This test verifies that TestContainers are working,
        // and we can connect to the PostgreSQL database
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    @DisplayName("Should have database connection available")
    void shouldHaveDatabaseConnectionAvailable() {
        // This test verifies that the database context is available
        // The BaseIntegrationTest should have started the container
        String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
        assertThat(result).isEqualTo("1");
    }
}
