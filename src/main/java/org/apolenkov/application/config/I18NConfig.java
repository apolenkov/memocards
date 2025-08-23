package org.apolenkov.application.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Configuration for internationalization (i18n) support.
 * Sets up Spring's MessageSource infrastructure for internationalization.
 * Supports UTF-8 encoding and provides fallback mechanisms for missing translations.
 */
@Configuration
public class I18NConfig {

    /**
     * Creates MessageSource bean for internationalization support.
     * Reads message bundles from "i18n/messages" with UTF-8 encoding.
     * Uses message code as default when translation is not found.
     *
     * @return configured MessageSource for internationalization
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        return ms;
    }
}
