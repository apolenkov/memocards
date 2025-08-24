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
     * @param text the display text for the button
     * @param clickListener the event handler for button clicks
     * @return a configured primary button
     */
    public static Button createPrimaryButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a primary button with icon, text, and click listener.
     * Primary buttons with icons provide visual reinforcement for the main action.
     *
     * @param text the display text for the button
     * @param icon the Vaadin icon to display
     * @param clickListener the event handler for button clicks
     * @return a configured primary button with icon
     */
    public static Button createPrimaryButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Creates a success button with icon, text, and click listener.
     * Success buttons are used for positive actions like save or confirm.
     *
     * @param text the display text for the button
     * @param icon the Vaadin icon to display
     * @param clickListener the event handler for button clicks
     * @return a configured success button with icon
     */
    public static Button createSuccessButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        return button;
    }

    /**
     * Creates an error button with icon, text, and click listener.
     * Error buttons are used for destructive actions like delete or remove.
     *
     * @param text the display text for the button
     * @param icon the Vaadin icon to display
     * @param clickListener the event handler for button clicks
     * @return a configured error button with icon
     */
    public static Button createErrorButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Creates a tertiary button with the specified text and click listener.
     *
     * <p>Tertiary buttons are used for secondary actions that are less
     * prominent than primary actions. They have a subtle appearance.</p>
     *
     * @param text the display text for the button
     * @param clickListener the event handler for button clicks
     * @return a configured tertiary button
     */
    public static Button createTertiaryButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a tertiary button with icon, text, and click listener.
     * Tertiary buttons with icons provide visual context while maintaining their subtle appearance.
     *
     * @param text the display text for the button
     * @param icon the Vaadin icon to display
     * @param clickListener the event handler for button clicks
     * @return a configured tertiary button with icon
     */
    public static Button createTertiaryButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Creates a large button with the specified text and click listener.
     *
     * <p>Large buttons are used for prominent actions that should be easily
     * accessible, such as main navigation or primary form submissions.</p>
     *
     * @param text the display text for the button
     * @param clickListener the event handler for button clicks
     * @return a configured large button
     */
    public static Button createLargeButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_LARGE);
        return button;
    }

    /**
     * Creates a back navigation button with left arrow icon.
     *
     * <p>Back buttons are consistently styled tertiary buttons with a left
     * arrow icon, used for navigation to previous pages or sections.</p>
     *
     * @param clickListener the event handler for button clicks
     * @return a configured back button
     */
    public static Button createBackButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createTertiaryButton("Back", VaadinIcon.ARROW_LEFT, clickListener);
    }

    /**
     * Creates a delete button with trash icon.
     *
     * <p>Delete buttons are consistently styled error buttons with a trash
     * icon, used for destructive operations that require user confirmation.</p>
     *
     * @param clickListener the event handler for button clicks
     * @return a configured delete button
     */
    public static Button createDeleteButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createErrorButton("Delete", VaadinIcon.TRASH, clickListener);
    }

    /**
     * Creates an edit button with edit icon.
     *
     * <p>Edit buttons are consistently styled tertiary buttons with an edit
     * icon, used for modifying existing content or settings.</p>
     *
     * @param clickListener the event handler for button clicks
     * @return a configured edit button
     */
    public static Button createEditButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createTertiaryButton("Edit", VaadinIcon.EDIT, clickListener);
    }

    /**
     * Creates a play button with play icon.
     *
     * <p>Play buttons are consistently styled success buttons with a play
     * icon, used for starting processes, games, or other positive actions.</p>
     *
     * @param clickListener the event handler for button clicks
     * @return a configured play button
     */
    public static Button createPlayButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createSuccessButton("Start", VaadinIcon.PLAY, clickListener);
    }

    /**
     * Creates a plus button with plus icon.
     *
     * <p>Plus buttons are consistently styled primary buttons with a plus
     * icon, used for adding new items, creating content, or other additive actions.</p>
     *
     * @param clickListener the event handler for button clicks
     * @return a configured plus button
     */
    public static Button createPlusButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createPrimaryButton("Add", VaadinIcon.PLUS, clickListener);
    }
}
