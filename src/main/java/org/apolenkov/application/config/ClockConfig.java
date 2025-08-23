package org.apolenkov.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for time-related beans.
 * Provides system clock bean for time-based operations throughout the application.
 * Useful for testing and consistent time access.
 */
@Configuration
public class ClockConfig {

    /**
     * Creates system clock bean for time-based operations.
     *
     * @return Clock instance using system's default timezone
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
