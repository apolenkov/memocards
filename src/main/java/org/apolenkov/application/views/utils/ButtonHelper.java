package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized button creation and styling.
 * Eliminates duplication of button creation patterns across the application.
 */
public final class ButtonHelper {

    private ButtonHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a primary button
     */
    public static Button createPrimaryButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Create a primary button with icon
     */
    public static Button createPrimaryButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Create a success button with icon
     */
    public static Button createSuccessButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        return button;
    }

    /**
     * Create an error button with icon
     */
    public static Button createErrorButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return button;
    }

    /**
     * Create a tertiary button
     */
    public static Button createTertiaryButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Create a tertiary button with icon
     */
    public static Button createTertiaryButton(
            String text, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, icon.create(), clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    /**
     * Create a large button
     */
    public static Button createLargeButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button(text, clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_LARGE);
        return button;
    }

    /**
     * Create a back button
     */
    public static Button createBackButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createTertiaryButton("Back", VaadinIcon.ARROW_LEFT, clickListener);
    }

    /**
     * Create a delete button
     */
    public static Button createDeleteButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createErrorButton("Delete", VaadinIcon.TRASH, clickListener);
    }

    /**
     * Create an edit button
     */
    public static Button createEditButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createTertiaryButton("Edit", VaadinIcon.EDIT, clickListener);
    }

    /**
     * Create a play button
     */
    public static Button createPlayButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createSuccessButton("Start", VaadinIcon.PLAY, clickListener);
    }

    /**
     * Create a plus button
     */
    public static Button createPlusButton(ComponentEventListener<ClickEvent<Button>> clickListener) {
        return createPrimaryButton("Add", VaadinIcon.PLUS, clickListener);
    }
}
