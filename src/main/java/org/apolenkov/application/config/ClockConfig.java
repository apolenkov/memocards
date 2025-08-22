package org.apolenkov.application.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for time-related beans.
 *
 * <p>This configuration class provides a centralized location for defining
 * time-related beans used throughout the application. It currently provides
 * a system clock bean that can be injected into components that need
 * time-based functionality.</p>
 *
 * <p>The system clock bean is useful for testing and provides a consistent
 * way to access current time across the application.</p>
 *
 */
@Configuration
public class ClockConfig {

    /**
     * Creates a system clock bean for time-based operations.
     *
     * <p>This bean provides access to the system's default timezone clock,
     * which can be used for obtaining current time, measuring durations,
     * and other time-related operations throughout the application.</p>
     *
     * @return a Clock instance using the system's default timezone
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
