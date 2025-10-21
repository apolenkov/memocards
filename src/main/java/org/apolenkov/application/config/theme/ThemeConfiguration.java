package org.apolenkov.application.config.theme;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Configures dynamic theme settings for Vaadin UI instances.
 *
 * <p>This component handles runtime theme switching and dynamic theme
 * configurations that cannot be set at the AppShell level.
 * The base theme is configured in VaadinApplicationShell.
 */
@Component
public class ThemeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeConfiguration.class);

    /**
     * Applies dynamic theme settings to the UI.
     *
     * <p>Currently applies Lumo dark theme as a dynamic addition
     * to the base "cards" theme configured in AppShell.
     *
     * @param ui the UI instance to apply the theme to (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    public void applyTheme(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }

        // Apply dynamic theme additions (base theme is set in AppShell)
        ui.getElement().getThemeList().add(Lumo.DARK);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "Dynamic theme settings applied [uiId={}, themes={}]",
                    ui.getUIId(),
                    ui.getElement().getThemeList());
        }
    }
}
