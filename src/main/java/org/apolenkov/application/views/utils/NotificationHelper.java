package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Utility class for centralized notification management.
 * Eliminates duplication of Notification.show patterns and provides consistent styling.
 */
public final class NotificationHelper {

    // Default durations
    private static final int DEFAULT_DURATION = 3000;
    private static final int SHORT_DURATION = 2000;
    private static final int LONG_DURATION = 5000;

    // Default positions
    private static final Notification.Position DEFAULT_POSITION = Notification.Position.TOP_START;
    private static final Notification.Position BOTTOM_POSITION = Notification.Position.BOTTOM_START;

    private NotificationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Show success notification
     */
    public static void showSuccess(String message) {
        showSuccess(message, DEFAULT_DURATION);
    }

    /**
     * Show success notification with custom duration
     */
    public static void showSuccess(String message, int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Show success notification at bottom
     */
    public static void showSuccessBottom(String message) {
        Notification notification = Notification.show(message, SHORT_DURATION, BOTTOM_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Show error notification
     */
    public static void showError(String message) {
        showError(message, DEFAULT_DURATION);
    }

    /**
     * Show error notification with custom duration
     */
    public static void showError(String message, int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Show error notification with long duration
     */
    public static void showErrorLong(String message) {
        Notification notification = Notification.show(message, LONG_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Show warning notification
     */
    public static void showWarning(String message) {
        Notification notification = Notification.show(message, DEFAULT_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    /**
     * Show info notification
     */
    public static void showInfo(String message) {
        Notification notification = Notification.show(message, DEFAULT_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    /**
     * Show notification with custom parameters
     */
    public static void show(String message, int duration, Notification.Position position) {
        Notification.show(message, duration, position);
    }

    /**
     * Show notification with custom parameters and variant
     */
    public static void show(String message, int duration, Notification.Position position, NotificationVariant variant) {
        Notification notification = Notification.show(message, duration, position);
        if (variant != null) {
            notification.addThemeVariants(variant);
        }
    }

    /**
     * Show validation error notification
     */
    public static void showValidationError() {
        showError(getTranslation("dialog.fillRequired"));
    }

    /**
     * Show save success notification
     */
    public static void showSaveSuccess() {
        showSuccessBottom(getTranslation("dialog.saved"));
    }

    /**
     * Show save failed notification
     */
    public static void showSaveFailed(String errorMessage) {
        showErrorLong(getTranslation("dialog.saveFailed", errorMessage));
    }

    /**
     * Show delete success notification
     */
    public static void showDeleteSuccess() {
        showSuccessBottom(getTranslation("dialog.deleted"));
    }

    /**
     * Show delete confirmation notification
     */
    public static void showDeleteConfirmation() {
        showWarning(getTranslation("dialog.delete.confirmText"));
    }

    /**
     * Get translation for current locale
     */
    private static String getTranslation(String key, Object... args) {
        try {
            // This would need to be implemented based on your i18n setup
            // For now, return the key as fallback
            return key;
        } catch (Exception e) {
            return key;
        }
    }
}
