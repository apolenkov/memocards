package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized icon creation and management.
 * Eliminates duplication of icon creation patterns across the application.
 */
public final class IconHelper {

    private IconHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a back arrow icon
     */
    public static Icon createBackIcon() {
        return VaadinIcon.ARROW_LEFT.create();
    }

    /**
     * Create a forward arrow icon
     */
    public static Icon createForwardIcon() {
        return VaadinIcon.ARROW_RIGHT.create();
    }

    /**
     * Create a home icon
     */
    public static Icon createHomeIcon() {
        return VaadinIcon.HOME.create();
    }

    /**
     * Create a user icon
     */
    public static Icon createUserIcon() {
        return VaadinIcon.USER.create();
    }

    /**
     * Create a users icon
     */
    public static Icon createUsersIcon() {
        return VaadinIcon.USERS.create();
    }

    /**
     * Create a settings icon
     */
    public static Icon createSettingsIcon() {
        return VaadinIcon.COG.create();
    }

    /**
     * Create a search icon
     */
    public static Icon createSearchIcon() {
        return VaadinIcon.SEARCH.create();
    }

    /**
     * Create a plus icon
     */
    public static Icon createPlusIcon() {
        return VaadinIcon.PLUS.create();
    }

    /**
     * Create a minus icon
     */
    public static Icon createMinusIcon() {
        return VaadinIcon.MINUS.create();
    }

    /**
     * Create a check icon
     */
    public static Icon createCheckIcon() {
        return VaadinIcon.CHECK.create();
    }

    /**
     * Create a close icon
     */
    public static Icon createCloseIcon() {
        return VaadinIcon.CLOSE.create();
    }

    /**
     * Create a edit icon
     */
    public static Icon createEditIcon() {
        return VaadinIcon.EDIT.create();
    }

    /**
     * Create a trash icon
     */
    public static Icon createTrashIcon() {
        return VaadinIcon.TRASH.create();
    }

    /**
     * Create a eye icon
     */
    public static Icon createEyeIcon() {
        return VaadinIcon.EYE.create();
    }

    /**
     * Create a eye slash icon
     */
    public static Icon createEyeSlashIcon() {
        return VaadinIcon.EYE_SLASH.create();
    }

    /**
     * Create a play icon
     */
    public static Icon createPlayIcon() {
        return VaadinIcon.PLAY.create();
    }

    /**
     * Create a pause icon
     */
    public static Icon createPauseIcon() {
        return VaadinIcon.PAUSE.create();
    }

    /**
     * Create a stop icon
     */
    public static Icon createStopIcon() {
        return VaadinIcon.STOP.create();
    }

    /**
     * Create a refresh icon
     */
    public static Icon createRefreshIcon() {
        return VaadinIcon.ROTATE_LEFT.create();
    }

    /**
     * Create a download icon
     */
    public static Icon createDownloadIcon() {
        return VaadinIcon.DOWNLOAD.create();
    }

    /**
     * Create a upload icon
     */
    public static Icon createUploadIcon() {
        return VaadinIcon.UPLOAD.create();
    }

    /**
     * Create a file icon
     */
    public static Icon createFileIcon() {
        return VaadinIcon.FILE.create();
    }

    /**
     * Create a folder icon
     */
    public static Icon createFolderIcon() {
        return VaadinIcon.FOLDER.create();
    }

    /**
     * Create a calendar icon
     */
    public static Icon createCalendarIcon() {
        return VaadinIcon.CALENDAR.create();
    }

    /**
     * Create a clock icon
     */
    public static Icon createClockIcon() {
        return VaadinIcon.CLOCK.create();
    }

    /**
     * Create a map marker icon
     */
    public static Icon createMapMarkerIcon() {
        return VaadinIcon.MAP_MARKER.create();
    }

    /**
     * Create a phone icon
     */
    public static Icon createPhoneIcon() {
        return VaadinIcon.PHONE.create();
    }

    /**
     * Create a envelope icon
     */
    public static Icon createEnvelopeIcon() {
        return VaadinIcon.ENVELOPE.create();
    }

    /**
     * Create a link icon
     */
    public static Icon createLinkIcon() {
        return VaadinIcon.LINK.create();
    }

    /**
     * Create a external link icon
     */
    public static Icon createExternalLinkIcon() {
        return VaadinIcon.EXTERNAL_LINK.create();
    }

    /**
     * Create a star icon
     */
    public static Icon createStarIcon() {
        return VaadinIcon.STAR.create();
    }

    /**
     * Create a heart icon
     */
    public static Icon createHeartIcon() {
        return VaadinIcon.HEART.create();
    }

    /**
     * Create a thumbs up icon
     */
    public static Icon createThumbsUpIcon() {
        return VaadinIcon.THUMBS_UP.create();
    }

    /**
     * Create a thumbs down icon
     */
    public static Icon createThumbsDownIcon() {
        return VaadinIcon.THUMBS_DOWN.create();
    }

    /**
     * Create a warning icon
     */
    public static Icon createWarningIcon() {
        return VaadinIcon.WARNING.create();
    }

    /**
     * Create a info icon
     */
    public static Icon createInfoIcon() {
        return VaadinIcon.INFO.create();
    }

    /**
     * Create a question icon
     */
    public static Icon createQuestionIcon() {
        return VaadinIcon.QUESTION.create();
    }

    /**
     * Create a exclamation icon
     */
    public static Icon createExclamationIcon() {
        return VaadinIcon.EXCLAMATION.create();
    }

    /**
     * Create a lock icon
     */
    public static Icon createLockIcon() {
        return VaadinIcon.LOCK.create();
    }

    /**
     * Create a unlock icon
     */
    public static Icon createUnlockIcon() {
        return VaadinIcon.UNLOCK.create();
    }

    /**
     * Create a key icon
     */
    public static Icon createKeyIcon() {
        return VaadinIcon.KEY.create();
    }

    /**
     * Create a shield icon
     */
    public static Icon createShieldIcon() {
        return VaadinIcon.SHIELD.create();
    }

    /**
     * Create a chart icon
     */
    public static Icon createChartIcon() {
        return VaadinIcon.CHART.create();
    }

    /**
     * Create a pie chart icon
     */
    public static Icon createPieChartIcon() {
        return VaadinIcon.PIE_CHART.create();
    }

    /**
     * Create a bar chart icon
     */
    public static Icon createBarChartIcon() {
        return VaadinIcon.BAR_CHART.create();
    }

    /**
     * Create a line chart icon
     */
    public static Icon createLineChartIcon() {
        return VaadinIcon.LINE_CHART.create();
    }

    /**
     * Create a table icon
     */
    public static Icon createTableIcon() {
        return VaadinIcon.TABLE.create();
    }

    /**
     * Create a list icon
     */
    public static Icon createListIcon() {
        return VaadinIcon.LIST.create();
    }

    /**
     * Create a grid icon
     */
    public static Icon createGridIcon() {
        return VaadinIcon.GRID.create();
    }

    /**
     * Create a menu icon
     */
    public static Icon createMenuIcon() {
        return VaadinIcon.MENU.create();
    }

    /**
     * Create a bars icon
     */
    public static Icon createBarsIcon() {
        return VaadinIcon.MENU.create(); // Alternative to BARS
    }

    /**
     * Create a ellipsis icon
     */
    public static Icon createEllipsisIcon() {
        return VaadinIcon.ELLIPSIS_DOTS_H.create();
    }

    /**
     * Create a ellipsis vertical icon
     */
    public static Icon createEllipsisVerticalIcon() {
        return VaadinIcon.ELLIPSIS_DOTS_V.create();
    }

    /**
     * Create a status icon based on boolean value
     */
    public static Icon createStatusIcon(boolean status) {
        return status ? createCheckIcon() : createCloseIcon();
    }

    /**
     * Create a status icon with custom colors
     */
    public static Icon createStatusIcon(boolean status, String trueColor, String falseColor) {
        Icon icon = createStatusIcon(status);
        icon.getStyle().set("color", status ? trueColor : falseColor);
        return icon;
    }

    /**
     * Create a text icon (emoji-style)
     */
    public static Span createTextIcon(String text, String className) {
        Span icon = new Span(text);
        icon.addClassName(className);
        return icon;
    }

    /**
     * Create a checkmark text icon
     */
    public static Span createCheckmarkIcon() {
        return createTextIcon("✓", "text-icon-checkmark");
    }

    /**
     * Create a cross text icon
     */
    public static Span createCrossIcon() {
        return createTextIcon("✗", "text-icon-cross");
    }

    /**
     * Create a arrow text icon
     */
    public static Span createArrowIcon(String direction) {
        String arrow =
                switch (direction.toLowerCase()) {
                    case "up" -> "↑";
                    case "down" -> "↓";
                    case "left" -> "←";
                    case "right" -> "→";
                    default -> "→";
                };
        return createTextIcon(arrow, "text-icon-arrow");
    }
}
