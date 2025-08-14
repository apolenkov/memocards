package org.apolenkov.application.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckEntity;
import org.apolenkov.application.infrastructure.repository.jpa.entity.UserEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.DeckJpaRepository;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.UserJpaRepository;
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
@org.junit.jupiter.api.Tag("integration")
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

    @Autowired
    UserJpaRepository userRepo;

    @Test
    void contextAndJpaWorks() {
        UserEntity u = new UserEntity();
        u.setEmail("u@test");
        u.setName("U");
        UserEntity savedUser = userRepo.save(u);
        DeckEntity d = new DeckEntity();
        d.setUserId(savedUser.getId());
        d.setTitle("IT");
        d.setDescription("terms");
        DeckEntity saved = deckRepo.save(d);
        assertNotNull(saved.getId());
        assertFalse(deckRepo.findByUserId(savedUser.getId()).isEmpty());
    }
}
