package org.apolenkov.application.integration;

import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryStatsRepository;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryUserSettingsRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("jpa")
public class TestStatsConfig {
    @Bean
    public StatsRepository statsRepository() {
        return new InMemoryStatsRepository();
    }

    @Bean
    public UserSettingsRepository userSettingsRepository() {
        return new InMemoryUserSettingsRepository();
    }
}
