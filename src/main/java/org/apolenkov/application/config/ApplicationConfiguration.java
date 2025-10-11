package org.apolenkov.application.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-wide configuration for common beans and utilities.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Provides system UTC clock for time operations.
     *
     * @return system UTC clock instance
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
