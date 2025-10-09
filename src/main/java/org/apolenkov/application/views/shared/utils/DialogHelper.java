package org.apolenkov.application.views.shared.utils;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Utility class for centralized dialog layout creation.
 * Provides factory methods for creating consistently styled dialog layouts.
 */
public final class DialogHelper {

    private DialogHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a standard button layout for dialogs.
     * Creates a horizontal layout with consistent spacing, alignment, and justification
     * commonly used for dialog action buttons.
     *
     * @return configured HorizontalLayout for dialog buttons
     */
    public static HorizontalLayout createButtonLayout() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();
        return buttons;
    }
}
