package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Utility class for centralized theme management and styling.
 * Eliminates duplication of theme-related patterns across the application.
 */
public final class ThemeHelper {

    // Theme constants
    public static final String LIGHT_THEME = "light";
    public static final String DARK_THEME = "dark";
    public static final String AUTO_THEME = "auto";

    // CSS custom properties
    public static final String PRIMARY_COLOR_PROP = "--lumo-primary-color";
    public static final String PRIMARY_TEXT_COLOR_PROP = "--lumo-primary-text-color";
    public static final String BACKGROUND_COLOR_PROP = "--lumo-base-color";
    public static final String SURFACE_COLOR_PROP = "--lumo-surface-color";
    public static final String TEXT_COLOR_PROP = "--lumo-text-color";
    public static final String SECONDARY_TEXT_COLOR_PROP = "--lumo-secondary-text-color";
    public static final String BORDER_COLOR_PROP = "--lumo-contrast-10pct";
    public static final String SHADOW_COLOR_PROP = "--lumo-box-shadow-xs";

    // Color schemes
    public static final String BLUE_SCHEME = "blue";
    public static final String GREEN_SCHEME = "green";
    public static final String PURPLE_SCHEME = "purple";
    public static final String ORANGE_SCHEME = "orange";
    public static final String RED_SCHEME = "red";

    private ThemeHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Set light theme for component
     */
    public static void setLightTheme(Component component) {
        component.getElement().setAttribute("theme", LIGHT_THEME);
        component.getElement().getThemeList().add(Lumo.LIGHT);
    }

    /**
     * Set dark theme for component
     */
    public static void setDarkTheme(Component component) {
        component.getElement().setAttribute("theme", DARK_THEME);
        component.getElement().getThemeList().add(Lumo.DARK);
    }

    /**
     * Set auto theme for component
     */
    public static void setAutoTheme(Component component) {
        component.getElement().setAttribute("theme", AUTO_THEME);
        component.getElement().getThemeList().remove(Lumo.LIGHT);
        component.getElement().getThemeList().remove(Lumo.DARK);
    }

    /**
     * Toggle between light and dark themes
     */
    public static void toggleTheme(Component component) {
        if (component.getElement().getThemeList().contains(Lumo.LIGHT)) {
            setDarkTheme(component);
        } else {
            setLightTheme(component);
        }
    }

    /**
     * Set primary color scheme
     */
    public static void setPrimaryColorScheme(Component component, String scheme) {
        String color = getSchemeColor(scheme);
        component.getElement().getStyle().set(PRIMARY_COLOR_PROP, color);
    }

    /**
     * Set background color scheme
     */
    public static void setBackgroundColorScheme(Component component, String scheme) {
        String color = getSchemeColor(scheme);
        component.getElement().getStyle().set(BACKGROUND_COLOR_PROP, color);
    }

    /**
     * Set text color scheme
     */
    public static void setTextColorScheme(Component component, String scheme) {
        String color = getSchemeColor(scheme);
        component.getElement().getStyle().set(TEXT_COLOR_PROP, color);
    }

    /**
     * Set custom CSS property
     */
    public static void setCustomProperty(Component component, String property, String value) {
        component.getElement().getStyle().set(property, value);
    }

    /**
     * Get custom CSS property value
     */
    public static String getCustomProperty(Component component, String property) {
        return component.getElement().getStyle().get(property);
    }

    /**
     * Set multiple custom CSS properties
     */
    public static void setCustomProperties(Component component, java.util.Map<String, String> properties) {
        properties.forEach(
                (property, value) -> component.getElement().getStyle().set(property, value));
    }

    /**
     * Apply modern theme with shadows and rounded corners
     */
    public static void applyModernTheme(Component component) {
        Style style = component.getElement().getStyle();
        style.set("border-radius", "8px");
        style.set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)");
        style.set("transition", "all 0.3s ease");
    }

    /**
     * Apply flat theme without shadows
     */
    public static void applyFlatTheme(Component component) {
        Style style = component.getElement().getStyle();
        style.set("border-radius", "0");
        style.set("box-shadow", "none");
        style.set("border", "1px solid var(--lumo-contrast-10pct)");
    }

    /**
     * Apply glass morphism effect
     */
    public static void applyGlassMorphism(Component component) {
        Style style = component.getElement().getStyle();
        style.set("background", "rgba(255, 255, 255, 0.1)");
        style.set("backdrop-filter", "blur(10px)");
        style.set("border", "1px solid rgba(255, 255, 255, 0.2)");
        style.set("border-radius", "16px");
        style.set("box-shadow", "0 4px 30px rgba(0, 0, 0, 0.1)");
    }

    /**
     * Apply neumorphism effect
     */
    public static void applyNeumorphism(Component component) {
        Style style = component.getElement().getStyle();
        style.set("background", "var(--lumo-base-color)");
        style.set("border-radius", "16px");
        style.set(
                "box-shadow",
                "inset 2px 2px 5px rgba(0, 0, 0, 0.1), " + "inset -2px -2px 5px rgba(255, 255, 255, 0.8)");
    }

    /**
     * Apply gradient background
     */
    public static void applyGradientBackground(Component component, String gradient) {
        component.getElement().getStyle().set("background", gradient);
    }

    /**
     * Apply primary gradient
     */
    public static void applyPrimaryGradient(Component component) {
        applyGradientBackground(
                component,
                "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)");
    }

    /**
     * Apply secondary gradient
     */
    public static void applySecondaryGradient(Component component) {
        applyGradientBackground(
                component,
                "linear-gradient(135deg, var(--lumo-secondary-color) 0%, var(--lumo-secondary-color-50pct) 100%)");
    }

    /**
     * Apply rainbow gradient
     */
    public static void applyRainbowGradient(Component component) {
        applyGradientBackground(
                component,
                "linear-gradient(45deg, #ff0000, #ff8000, #ffff00, #80ff00, #00ff00, #00ff80, #00ffff, #0080ff, #0000ff, #8000ff, #ff00ff, #ff0080)");
    }

    /**
     * Set hover effect with theme colors
     */
    public static void setHoverEffect(Component component) {
        component
                .getElement()
                .executeJs(
                        """
            this.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-2px)';
                this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
            });
            this.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.1)';
            });
        """);
    }

    /**
     * Set focus effect with theme colors
     */
    public static void setFocusEffect(Component component) {
        component
                .getElement()
                .executeJs(
                        """
            this.addEventListener('focus', function() {
                this.style.outline = 'none';
                this.style.boxShadow = '0 0 0 2px var(--lumo-primary-color)';
            });
            this.addEventListener('blur', function() {
                this.style.boxShadow = 'none';
            });
        """);
    }

    /**
     * Set loading theme
     */
    public static void setLoadingTheme(Component component) {
        Style style = component.getElement().getStyle();
        style.set("opacity", "0.7");
        style.set("pointer-events", "none");
        style.set("cursor", "wait");
    }

    /**
     * Set disabled theme
     */
    public static void setDisabledTheme(Component component) {
        Style style = component.getElement().getStyle();
        style.set("opacity", "0.5");
        style.set("pointer-events", "none");
        style.set("cursor", "not-allowed");
    }

    /**
     * Set success theme
     */
    public static void setSuccessTheme(Component component) {
        ColorHelper.setSuccessColor(component);
        ColorHelper.setSuccessBorder(component);
    }

    /**
     * Set error theme
     */
    public static void setErrorTheme(Component component) {
        ColorHelper.setErrorColor(component);
        ColorHelper.setErrorBorder(component);
    }

    /**
     * Set warning theme
     */
    public static void setWarningTheme(Component component) {
        ColorHelper.setWarningColor(component);
        ColorHelper.setWarningBorder(component);
    }

    /**
     * Set info theme
     */
    public static void setInfoTheme(Component component) {
        ColorHelper.setInfoColor(component);
        ColorHelper.setInfoBorder(component);
    }

    /**
     * Get color for specific scheme
     */
    private static String getSchemeColor(String scheme) {
        return switch (scheme.toLowerCase()) {
            case BLUE_SCHEME -> "#1976d2";
            case GREEN_SCHEME -> "#2e7d32";
            case PURPLE_SCHEME -> "#7b1fa2";
            case ORANGE_SCHEME -> "#f57c00";
            case RED_SCHEME -> "#d32f2f";
            default -> "#1976d2";
        };
    }

    /**
     * Check if component has light theme
     */
    public static boolean isLightTheme(Component component) {
        return component.getElement().getThemeList().contains(Lumo.LIGHT);
    }

    /**
     * Check if component has dark theme
     */
    public static boolean isDarkTheme(Component component) {
        return component.getElement().getThemeList().contains(Lumo.DARK);
    }

    /**
     * Get current theme name
     */
    public static String getCurrentTheme(Component component) {
        if (isLightTheme(component)) {
            return LIGHT_THEME;
        } else if (isDarkTheme(component)) {
            return DARK_THEME;
        } else {
            return AUTO_THEME;
        }
    }
}
