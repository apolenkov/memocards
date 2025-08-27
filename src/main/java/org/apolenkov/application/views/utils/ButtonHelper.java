package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized button creation and styling.
 * Provides factory methods for creating consistently styled buttons.
 */
public final class ButtonHelper {

    private ButtonHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a primary button with the specified text and click listener.
     * Primary buttons are used for the main action in forms and dialogs.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured primary button with LUMO_PRIMARY variant
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createPrimaryButton(
            final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a primary button with icon, text, and click listener.
     * Primary buttons with icons provide visual reinforcement for the main action.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured primary button with icon and LUMO_PRIMARY variant
     * @throws IllegalArgumentException if any parameter is null or text is empty
     */
    public static Button createPrimaryButton(
            final String text, final VaadinIcon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a success button with icon, text, and click listener.
     * Success buttons are used for positive actions like save or confirm.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured success button with icon and LUMO_SUCCESS variant
     * @throws IllegalArgumentException if any parameter is null or text is empty
     */
    public static Button createSuccessButton(
            final String text, final VaadinIcon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        return button;
    }

    /**
     * Creates an error button with icon, text, and click listener.
     * Error buttons are used for destructive actions like delete or remove.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured error button with icon and LUMO_ERROR variant
     * @throws IllegalArgumentException if any parameter is null or text is empty
     */
    public static Button createErrorButton(
            final String text, final VaadinIcon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Creates a tertiary button with the specified text and click listener.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured tertiary button with LUMO_TERTIARY variant
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createTertiaryButton(
            final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a tertiary button with icon, text, and click listener.
     * Tertiary buttons with icons provide visual context while maintaining their subtle appearance.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured tertiary button with icon and LUMO_TERTIARY variant
     * @throws IllegalArgumentException if any parameter is null or text is empty
     */
    public static Button createTertiaryButton(
            final String text, final VaadinIcon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a large button with the specified text and click listener.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured large button with LUMO_LARGE variant
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createLargeButton(
            final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_LARGE);
        return button;
    }

    /**
     * Creates a back navigation button with left arrow icon.
     *
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured back button with left arrow icon
     * @throws IllegalArgumentException if clickListener is null
     */
    public static Button createBackButton(final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        return createTertiaryButton("Back", VaadinIcon.ARROW_LEFT, clickListener);
    }

    /**
     * Creates a delete button with trash icon.
     *
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured delete button with trash icon
     * @throws IllegalArgumentException if clickListener is null
     */
    public static Button createDeleteButton(final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        return createErrorButton("Delete", VaadinIcon.TRASH, clickListener);
    }

    /**
     * Creates an edit button with edit icon.
     *
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured edit button with edit icon
     * @throws IllegalArgumentException if clickListener is null
     */
    public static Button createEditButton(final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        return createTertiaryButton("Edit", VaadinIcon.EDIT, clickListener);
    }

    /**
     * Creates a play button with play icon.
     *
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured play button with play icon
     * @throws IllegalArgumentException if clickListener is null
     */
    public static Button createPlayButton(final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        return createSuccessButton("Start", VaadinIcon.PLAY, clickListener);
    }

    /**
     * Creates a plus button with plus icon.
     *
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured plus button with plus icon
     * @throws IllegalArgumentException if clickListener is null
     */
    public static Button createPlusButton(final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        return createPrimaryButton("Add", VaadinIcon.PLUS, clickListener);
    }
}
