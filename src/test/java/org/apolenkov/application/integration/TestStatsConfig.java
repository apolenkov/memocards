package org.apolenkov.application.integration;

import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("dev")
public class TestStatsConfig {
    @Bean
    public StatsRepository statsRepository() {
        return Mockito.mock(StatsRepository.class);
    }

    @Bean
    public UserSettingsRepository userSettingsRepository() {
        return Mockito.mock(UserSettingsRepository.class);
    }
}
