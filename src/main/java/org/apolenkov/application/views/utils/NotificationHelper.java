package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Utility class for centralized notification management.
 *
 * <p>This utility class provides factory methods for creating consistently
 * styled notifications throughout the application. It eliminates duplication
 * of notification creation patterns and ensures uniform appearance and behavior.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Success, error, warning, and info notification variants</li>
 *   <li>Customizable duration and positioning options</li>
 *   <li>Specialized methods for common notification types</li>
 *   <li>Standardized styling using Lumo design system</li>
 * </ul>
 *
 * <p>All notifications created through this utility automatically include
 * appropriate theme variants and positioning for consistent user experience.</p>
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
     *
     * <p>Displays a green success notification with default duration
     * and positioning. Success notifications are used to confirm
     * successful operations and provide positive feedback to users.</p>
     *
     * @param message the success message to display
     */
    public static void showSuccess(String message) {
        showSuccess(message, DEFAULT_DURATION);
    }

    /**
     * Shows a success notification with custom duration.
     *
     * <p>Displays a success notification with the specified duration
     * while maintaining the default positioning and styling.</p>
     *
     * @param message the success message to display
     * @param duration the duration in milliseconds to show the notification
     */
    public static void showSuccess(String message, int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows a success notification at the bottom position.
     *
     * <p>Displays a success notification with short duration at the
     * bottom position for quick user feedback.</p>
     *
     * @param message the success message to display
     */
    public static void showSuccessBottom(String message) {
        Notification notification = Notification.show(message, SHORT_DURATION, BOTTOM_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification with the specified message.
     *
     * <p>Displays a red error notification with default duration
     * and positioning. Error notifications are used to inform
     * users about failed operations or system errors.</p>
     *
     * @param message the error message to display
     */
    public static void showError(String message) {
        showError(message, DEFAULT_DURATION);
    }

    /**
     * Shows an error notification with custom duration.
     *
     * <p>Displays an error notification with the specified duration
     * while maintaining the default positioning and styling.</p>
     *
     * @param message the error message to display
     * @param duration the duration in milliseconds to show the notification
     */
    public static void showError(String message, int duration) {
        Notification notification = Notification.show(message, duration, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Shows an error notification with long duration.
     *
     * <p>Displays an error notification with extended duration to ensure
     * users have sufficient time to read important error messages.</p>
     *
     * @param message the error message to display
     */
    public static void showErrorLong(String message) {
        Notification notification = Notification.show(message, LONG_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Shows an informational notification with the specified message.
     *
     * <p>Displays a blue informational notification with default duration
     * and positioning. Info notifications are used to provide
     * general information or status updates to users.</p>
     *
     * @param message the informational message to display
     */
    public static void showInfo(String message) {
        Notification notification = Notification.show(message, DEFAULT_DURATION, DEFAULT_POSITION);
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    /**
     * Shows a notification with custom parameters.
     *
     * <p>Creates a basic notification with custom duration and position
     * without any specific theme variants applied.</p>
     *
     * @param message the message to display in the notification
     * @param duration the duration in milliseconds to show the notification
     * @param position the position where the notification should appear
     */
    public static void show(String message, int duration, Notification.Position position) {
        Notification.show(message, duration, position);
    }

    /**
     * Shows a notification with custom parameters and variant.
     *
     * <p>Creates a notification with custom duration, position, and
     * theme variant for specialized notification requirements.</p>
     *
     * @param message the message to display in the notification
     * @param duration the duration in milliseconds to show the notification
     * @param position the position where the notification should appear
     * @param variant the visual variant to apply to the notification
     */
    public static void show(String message, int duration, Notification.Position position, NotificationVariant variant) {
        Notification notification = Notification.show(message, duration, position);
        if (variant != null) {
            notification.addThemeVariants(variant);
        }
    }

    /**
     * Shows a validation error notification.
     *
     * <p>Displays a standardized error notification for form validation
     * failures using the localized error message.</p>
     */
    public static void showValidationError() {
        showError(I18nHelper.tr("dialog.fillRequired"));
    }

    /**
     * Shows a delete success notification.
     *
     * <p>Displays a success notification confirming successful deletion
     * operations with bottom positioning for quick acknowledgment.</p>
     */
    public static void showDeleteSuccess() {
        showSuccessBottom(I18nHelper.tr("dialog.deleted"));
    }
}
