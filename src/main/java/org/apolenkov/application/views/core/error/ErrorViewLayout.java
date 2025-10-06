package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.core.constants.CoreConstants;

/**
 * Component responsible for creating and managing the main error display layout.
 * Handles the error title, description, and container setup.
 */
public final class ErrorViewLayout extends Composite<VerticalLayout> {

    private final H2 title;
    private final Span description;

    /**
     * Creates a new ErrorViewLayout component.
     */
    public ErrorViewLayout() {
        // Create and configure title
        this.title = new H2();
        title.addClassName(CoreConstants.ERROR_VIEW_TITLE_CLASS);

        // Create and configure description
        this.description = new Span();
        description.addClassName(CoreConstants.ERROR_VIEW_DESCRIPTION_CLASS);
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout errorContainer = new VerticalLayout();
        errorContainer.addClassName(CoreConstants.ERROR_CONTAINER_CLASS);
        errorContainer.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        errorContainer.setSpacing(true);
        errorContainer.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        errorContainer.add(title, description);
        return errorContainer;
    }

    /**
     * Updates the error display with error information.
     */
    public void updateWithGenericError() {
        title.setText(getTranslation(CoreConstants.ERROR_500_KEY));
        description.setText(getTranslation(CoreConstants.ERROR_500_DESCRIPTION_KEY));
    }

    /**
     * Adds a component to the error container.
     *
     * @param component the component to add
     */
    public void addComponent(final Component component) {
        getContent().add(component);
    }
}
