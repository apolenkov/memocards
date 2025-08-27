package org.apolenkov.application.config.theme;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Configures Vaadin UI theme settings.
 *
 * <p>This component is responsible for applying theme configurations
 * to Vaadin UI instances.
 */
@Component
public class ThemeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeConfiguration.class);

    /**
     * Applies Lumo dark theme to the UI.
     *
     * @param ui the UI instance to apply the theme to (non-null)
     * @throws IllegalArgumentException if ui is null
     */
    public void applyTheme(final UI ui) {
        if (ui == null) {
            throw new IllegalArgumentException("UI cannot be null");
        }
        ui.getElement().getThemeList().add(Lumo.DARK);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Lumo dark theme enabled [uiId={}]", ui.getUIId());
        }
    }
}
