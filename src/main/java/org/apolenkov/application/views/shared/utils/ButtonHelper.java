package org.apolenkov.application.views.shared.utils;

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
     * Creates a button with text and click listener.
     * Generic method for creating buttons with custom styling.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @param variants the button variants to apply
     * @return a configured button with specified variants
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createButton(
            final String text,
            final ComponentEventListener<ClickEvent<Button>> clickListener,
            final ButtonVariant... variants) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, clickListener);
        if (variants.length > 0) {
            button.addThemeVariants(variants);
        }
        return button;
    }

    /**
     * Creates a button with icon, text, and click listener.
     * Generic method for creating buttons with custom styling.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @param variants the button variants to apply
     * @return a configured button with icon and specified variants
     * @throws IllegalArgumentException if any parameter is null or text is empty
     */
    public static Button createButton(
            final String text,
            final VaadinIcon icon,
            final ComponentEventListener<ClickEvent<Button>> clickListener,
            final ButtonVariant... variants) {
        if (text == null) {
            throw new IllegalArgumentException("Button text cannot be null");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, icon.create(), clickListener);
        if (variants.length > 0) {
            button.addThemeVariants(variants);
        }
        return button;
    }

    /**
     * Creates an icon-only button for grid actions and toolbars.
     * Perfect for compact UI elements where space is limited.
     *
     * @param icon the Vaadin icon to display (non-null)
     * @param clickListener the event handler for button clicks (non-null)
     * @param variants the button variants to apply
     * @return a configured icon-only button
     * @throws IllegalArgumentException if icon or clickListener is null
     */
    public static Button createIconButton(
            final VaadinIcon icon,
            final ComponentEventListener<ClickEvent<Button>> clickListener,
            final ButtonVariant... variants) {
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(icon.create(), clickListener);
        if (variants.length > 0) {
            button.addThemeVariants(variants);
        }
        return button;
    }

    /**
     * Creates an icon-only button without click listener.
     * Useful when click listener needs to be added later.
     *
     * @param icon the Vaadin icon to display (non-null)
     * @param variants the button variants to apply
     * @return a configured icon-only button without click listener
     * @throws IllegalArgumentException if icon is null
     */
    public static Button createIconButton(final VaadinIcon icon, final ButtonVariant... variants) {
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        Button button = new Button(icon.create());
        if (variants.length > 0) {
            button.addThemeVariants(variants);
        }
        return button;
    }

    /**
     * Creates a confirm button with consistent styling for dialogs.
     * Commonly used pattern for confirmation dialogs.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured confirm button with check icon and success styling
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createConfirmButton(
            final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Button text cannot be null or empty");
        }
        if (clickListener == null) {
            throw new IllegalArgumentException("Click listener cannot be null");
        }
        Button button = new Button(text, VaadinIcon.CHECK.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        return button;
    }

    /**
     * Creates a cancel button with consistent styling for dialogs.
     * Commonly used pattern for cancellation actions.
     *
     * @param text the display text for the button (non-null, non-empty)
     * @param clickListener the event handler for button clicks (non-null)
     * @return a configured cancel button with tertiary styling
     * @throws IllegalArgumentException if text is null or empty, or clickListener is null
     */
    public static Button createCancelButton(
            final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createTertiaryButton(text, clickListener);
    }
}
