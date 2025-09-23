package org.apolenkov.application.views.shared.base;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;

/**
 * Base view class providing common functionality for all views.
 * Contains shared methods for creating consistent layouts and components.
 */
public abstract class BaseView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Creates a centered vertical layout with consistent styling.
     * This is a commonly used pattern across authentication views.
     *
     * @return a configured VerticalLayout with centered content alignment
     */
    protected VerticalLayout createCenteredVerticalLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setPadding(true);
        return layout;
    }
}
