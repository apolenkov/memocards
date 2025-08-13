package org.apolenkov.application.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.DeckJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        classes = {org.apolenkov.application.Application.class, TestStatsConfig.class},
        properties = {
            "spring.datasource.url=jdbc:tc:postgresql:16-alpine:///testdb",
            "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=none"
        })
@ActiveProfiles({"jpa"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class JpaWithPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    DeckJpaRepository deckRepo;

    @Test
    void contextAndJpaWorks() {
        DeckEntity d = new DeckEntity();
        d.setUserId(1L);
        d.setTitle("IT");
        d.setDescription("terms");
        DeckEntity saved = deckRepo.save(d);
        assertNotNull(saved.getId());
        assertFalse(deckRepo.findByUserId(1L).isEmpty());
    }
}
