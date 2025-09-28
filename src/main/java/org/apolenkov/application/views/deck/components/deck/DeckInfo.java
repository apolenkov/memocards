package org.apolenkov.application.views.deck.components.deck;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Component for displaying deck information including description.
 * Provides a consistent layout for deck details with proper styling
 * and follows the component pattern established in the refactoring.
 */
public final class DeckInfo extends VerticalLayout {

    // UI Components
    private final Div infoSection;
    private final Span description;

    /**
     * Creates a new DeckInfo component.
     * Initializes UI components without configuring them to avoid this-escape warnings.
     */
    public DeckInfo() {
        this.infoSection = new Div();
        this.description = new Span();
    }

    /**
     * Configures the info section layout and styling.
     * Sets up the container with proper CSS classes and structure.
     */
    private void configureLayout() {
        infoSection.addClassName("deck-view__info-section");
        infoSection.addClassName("surface-panel");

        description.addClassName("deck-view__description");
        description.setText(getTranslation("deck.description.loading"));

        infoSection.add(description);
        add(infoSection);
    }

    /**
     * Updates the deck description with the provided text.
     *
     * @param descriptionText the new description text to display
     */
    public void setDescription(final String descriptionText) {
        description.setText(descriptionText);
    }

    /**
     * Initializes the component when attached to the UI.
     * Configures layout and components to avoid this-escape warnings.
     *
     * @param attachEvent the attachment event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureLayout();
    }
}
