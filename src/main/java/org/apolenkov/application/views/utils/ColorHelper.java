package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;

/**
 * Utility class for centralized color management and styling.
 * Eliminates duplication of color definitions across the application.
 */
public final class ColorHelper {

    // Primary colors
    public static final String PRIMARY_COLOR = "#1976d2";
    public static final String PRIMARY_LIGHT = "#42a5f5";
    public static final String PRIMARY_DARK = "#1565c0";

    // Secondary colors
    public static final String SECONDARY_COLOR = "#dc004e";
    public static final String SECONDARY_LIGHT = "#ff5983";
    public static final String SECONDARY_DARK = "#9a0036";

    // Success colors
    public static final String SUCCESS_COLOR = "#2e7d32";
    public static final String SUCCESS_LIGHT = "#4caf50";
    public static final String SUCCESS_DARK = "#1b5e20";

    // Warning colors
    public static final String WARNING_COLOR = "#ed6c02";
    public static final String WARNING_LIGHT = "#ff9800";
    public static final String WARNING_DARK = "#e65100";

    // Error colors
    public static final String ERROR_COLOR = "#d32f2f";
    public static final String ERROR_LIGHT = "#f44336";
    public static final String ERROR_DARK = "#c62828";

    // Info colors
    public static final String INFO_COLOR = "#0288d1";
    public static final String INFO_LIGHT = "#03a9f4";
    public static final String INFO_DARK = "#01579b";

    // Neutral colors
    public static final String WHITE = "#ffffff";
    public static final String BLACK = "#000000";
    public static final String GRAY_LIGHT = "#f5f5f5";
    public static final String GRAY = "#9e9e9e";
    public static final String GRAY_DARK = "#424242";

    // Background colors
    public static final String BACKGROUND_PRIMARY = "#fafafa";
    public static final String BACKGROUND_SECONDARY = "#f5f5f5";
    public static final String BACKGROUND_DARK = "#212121";

    // Text colors
    public static final String TEXT_PRIMARY = "#212121";
    public static final String TEXT_SECONDARY = "#757575";
    public static final String TEXT_DISABLED = "#bdbdbd";
    public static final String TEXT_WHITE = "#ffffff";

    // Border colors
    public static final String BORDER_LIGHT = "#e0e0e0";
    public static final String BORDER_MEDIUM = "#bdbdbd";
    public static final String BORDER_DARK = "#757575";

    // Shadow colors
    public static final String SHADOW_LIGHT = "rgba(0, 0, 0, 0.12)";
    public static final String SHADOW_MEDIUM = "rgba(0, 0, 0, 0.24)";
    public static final String SHADOW_DARK = "rgba(0, 0, 0, 0.48)";

    // Gradient colors
    public static final String GRADIENT_PRIMARY = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
    public static final String GRADIENT_SECONDARY = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
    public static final String GRADIENT_SUCCESS = "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)";
    public static final String GRADIENT_WARNING = "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)";
    public static final String GRADIENT_ERROR = "linear-gradient(135deg, #fa709a 0%, #fee140 100%)";

    private ColorHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Set primary color for component
     */
    public static void setPrimaryColor(Component component) {
        component.getStyle().set("color", PRIMARY_COLOR);
    }

    /**
     * Set secondary color for component
     */
    public static void setSecondaryColor(Component component) {
        component.getStyle().set("color", SECONDARY_COLOR);
    }

    /**
     * Set success color for component
     */
    public static void setSuccessColor(Component component) {
        component.getStyle().set("color", SUCCESS_COLOR);
    }

    /**
     * Set warning color for component
     */
    public static void setWarningColor(Component component) {
        component.getStyle().set("color", WARNING_COLOR);
    }

    /**
     * Set error color for component
     */
    public static void setErrorColor(Component component) {
        component.getStyle().set("color", ERROR_COLOR);
    }

    /**
     * Set info color for component
     */
    public static void setInfoColor(Component component) {
        component.getStyle().set("color", INFO_COLOR);
    }

    /**
     * Set background color for component
     */
    public static void setBackgroundColor(Component component, String color) {
        component.getStyle().set("background-color", color);
    }

    /**
     * Set primary background for component
     */
    public static void setPrimaryBackground(Component component) {
        setBackgroundColor(component, PRIMARY_COLOR);
    }

    /**
     * Set secondary background for component
     */
    public static void setSecondaryBackground(Component component) {
        setBackgroundColor(component, SECONDARY_COLOR);
    }

    /**
     * Set success background for component
     */
    public static void setSuccessBackground(Component component) {
        setBackgroundColor(component, SUCCESS_COLOR);
    }

    /**
     * Set warning background for component
     */
    public static void setWarningBackground(Component component) {
        setBackgroundColor(component, WARNING_COLOR);
    }

    /**
     * Set error background for component
     */
    public static void setErrorBackground(Component component) {
        setBackgroundColor(component, ERROR_COLOR);
    }

    /**
     * Set info background for component
     */
    public static void setInfoBackground(Component component) {
        setBackgroundColor(component, INFO_COLOR);
    }

    /**
     * Set border color for component
     */
    public static void setBorderColor(Component component, String color) {
        component.getStyle().set("border-color", color);
    }

    /**
     * Set primary border for component
     */
    public static void setPrimaryBorder(Component component) {
        setBorderColor(component, PRIMARY_COLOR);
    }

    /**
     * Set secondary border for component
     */
    public static void setSecondaryBorder(Component component) {
        setBorderColor(component, SECONDARY_COLOR);
    }

    /**
     * Set success border for component
     */
    public static void setSuccessBorder(Component component) {
        setBorderColor(component, SUCCESS_COLOR);
    }

    /**
     * Set warning border for component
     */
    public static void setWarningBorder(Component component) {
        setBorderColor(component, WARNING_COLOR);
    }

    /**
     * Set error border for component
     */
    public static void setErrorBorder(Component component) {
        setBorderColor(component, ERROR_COLOR);
    }

    /**
     * Set info border for component
     */
    public static void setInfoBorder(Component component) {
        setBorderColor(component, INFO_COLOR);
    }

    /**
     * Set gradient background for component
     */
    public static void setGradientBackground(Component component, String gradient) {
        component.getStyle().set("background", gradient);
    }

    /**
     * Set primary gradient for component
     */
    public static void setPrimaryGradient(Component component) {
        setGradientBackground(component, GRADIENT_PRIMARY);
    }

    /**
     * Set secondary gradient for component
     */
    public static void setSecondaryGradient(Component component) {
        setGradientBackground(component, GRADIENT_SECONDARY);
    }

    /**
     * Set success gradient for component
     */
    public static void setSuccessGradient(Component component) {
        setGradientBackground(component, GRADIENT_SUCCESS);
    }

    /**
     * Set warning gradient for component
     */
    public static void setWarningGradient(Component component) {
        setGradientBackground(component, GRADIENT_WARNING);
    }

    /**
     * Set error gradient for component
     */
    public static void setErrorGradient(Component component) {
        setGradientBackground(component, GRADIENT_ERROR);
    }

    /**
     * Set shadow for component
     */
    public static void setShadow(Component component, String shadow) {
        component.getStyle().set("box-shadow", shadow);
    }

    /**
     * Set light shadow for component
     */
    public static void setLightShadow(Component component) {
        setShadow(component, "0 2px 4px " + SHADOW_LIGHT);
    }

    /**
     * Set medium shadow for component
     */
    public static void setMediumShadow(Component component) {
        setShadow(component, "0 4px 8px " + SHADOW_MEDIUM);
    }

    /**
     * Set dark shadow for component
     */
    public static void setDarkShadow(Component component) {
        setShadow(component, "0 8px 16px " + SHADOW_DARK);
    }

    /**
     * Set hover effect for component
     */
    public static void setHoverEffect(Component component) {
        component.getStyle().set("transition", "all 0.3s ease");
        component.getStyle().set("cursor", "pointer");
    }

    /**
     * Set disabled style for component
     */
    public static void setDisabledStyle(Component component) {
        component.getStyle().set("opacity", "0.6");
        component.getStyle().set("cursor", "not-allowed");
    }

    /**
     * Set focus style for component
     */
    public static void setFocusStyle(Component component) {
        component.getStyle().set("outline", "none");
        component.getStyle().set("border-color", PRIMARY_COLOR);
        component.getStyle().set("box-shadow", "0 0 0 2px " + PRIMARY_LIGHT);
    }

    /**
     * Get color with opacity
     */
    public static String getColorWithOpacity(String color, double opacity) {
        if (color.startsWith("#")) {
            // Convert hex to rgba
            String hex = color.substring(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, opacity);
        }
        return color;
    }

    /**
     * Get lighter version of color
     */
    public static String getLighterColor(String color, double factor) {
        if (color.startsWith("#")) {
            String hex = color.substring(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            r = Math.min(255, (int) (r + (255 - r) * factor));
            g = Math.min(255, (int) (g + (255 - g) * factor));
            b = Math.min(255, (int) (b + (255 - b) * factor));

            return String.format("#%02x%02x%02x", r, g, b);
        }
        return color;
    }

    /**
     * Get darker version of color
     */
    public static String getDarkerColor(String color, double factor) {
        if (color.startsWith("#")) {
            String hex = color.substring(1);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            r = Math.max(0, (int) (r * (1 - factor)));
            g = Math.max(0, (int) (g * (1 - factor)));
            b = Math.max(0, (int) (b * (1 - factor)));

            return String.format("#%02x%02x%02x", r, g, b);
        }
        return color;
    }
}
