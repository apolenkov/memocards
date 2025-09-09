package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Utility class for centralized notification management.
 * Provides factory methods for creating consistently styled notifications.
 */
public final class NotificationHelper {

    // Default durations
    private static final int DEFAULT_DURATION = 3000;
    private static final int SHORT_DURATION = 2000;
    private static final int LONG_DURATION = 5000;

    // Default positions
    private static final Notification.Position DEFAULT_POSITION = Notification.Position.BOTTOM_START;
    private static final Notification.Position BOTTOM_POSITION = Notification.Position.BOTTOM_START;

    private NotificationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Shows a success notification with the specified message.
     * Displays a green success notification with default duration and positioning.
     *
     * @param message the success message to display
     */
    public static void showSuccess(final String message) {
        showSuccess(message, DEFAULT_DURATION);
    }

    /**
     * Shows a success notification with custom duration.
     * Displays a success notification with the specified duration.
     *
     * @param message the success message to display
     * @param duration the duration in milliseconds to show the notification
     */
    public static void showSuccess(final String message, final int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows a success notification at the bottom position.
     * Displays a success notification with short duration at the bottom position.
     *
     * @param message the success message to display
     */
    public static void showSuccessBottom(final String message) {
        Notification notification = Notification.show(message, SHORT_DURATION, BOTTOM_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification with the specified message.
     * Displays a red error notification with default duration and positioning.
     *
     * @param message the error message to display
     */
    public static void showError(final String message) {
        showError(message, DEFAULT_DURATION);
    }

    /**
     * Shows an error notification with custom duration.
     * Displays an error notification with the specified duration.
     *
     * @param message the error message to display
     * @param duration the duration in milliseconds to show the notification
     */
    public static void showError(final String message, final int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Shows an error notification with long duration.
     * Displays an error notification with extended duration to ensure
     * users have sufficient time to read important error messages.
     *
     * @param message the error message to display
     */
    public static void showErrorLong(final String message) {
        Notification notification = Notification.show(message, LONG_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Shows an informational notification with the specified message.
     * Displays a blue informational notification with default duration and positioning.
     *
     * @param message the informational message to display
     */
    public static void showInfo(final String message) {
        Notification notification = Notification.show(message, DEFAULT_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    /**
     * Shows a notification with custom parameters.
     * Creates a basic notification with custom duration and position
     * without any specific theme variants applied.
     *
     * @param message the message to display in the notification
     * @param duration the duration in milliseconds to show the notification
     * @param position the position where the notification should appear
     */
    public static void show(final String message, final int duration, final Notification.Position position) {
        Notification.show(message, duration, position);
    }

    /**
     * Shows a notification with custom parameters and variant.
     * Creates a notification with custom duration, position, and
     * theme variant for specialized notification requirements.
     *
     * @param message the message to display in the notification
     * @param duration the duration in milliseconds to show the notification
     * @param position the position where the notification should appear
     * @param variant the visual variant to apply to the notification
     */
    public static void show(
            final String message,
            final int duration,
            final Notification.Position position,
            final NotificationVariant variant) {
        Notification notification = Notification.show(message, duration, position);
        if (variant != null) {
            notification.addThemeVariants(variant);
        }
    }
}
