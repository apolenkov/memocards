package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Component for displaying deck information including description.
 * Provides a consistent layout for deck details with proper styling.
 */
public final class DeckInfo extends Composite<VerticalLayout> {

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

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout info = new VerticalLayout();

        infoSection.addClassName(DeckConstants.DECK_VIEW_INFO_SECTION_CLASS);
        infoSection.addClassName(DeckConstants.SURFACE_PANEL_CLASS);

        description.addClassName(DeckConstants.DECK_VIEW_DESCRIPTION_CLASS);
        description.setText(getTranslation(DeckConstants.DECK_DESCRIPTION_LOADING));

        infoSection.add(description);
        info.add(infoSection);
        return info;
    }

    /**
     * Updates the deck description with the provided text.
     *
     * @param descriptionText the new description text to display
     */
    public void setDescription(final String descriptionText) {
        description.setText(descriptionText);
    }
}
