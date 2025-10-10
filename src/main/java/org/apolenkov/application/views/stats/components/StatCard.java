package org.apolenkov.application.views.stats.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import org.apolenkov.application.views.stats.constants.StatsConstants;

/**
 * UI component for displaying a single statistic card.
 * Shows a numeric value with a translated label.
 */
public final class StatCard extends Composite<Div> {

    private final String labelKey;
    private final int value;

    /**
     * Creates a new statistics card.
     *
     * @param labelKeyParam translation key for the label
     * @param valueParam numeric value to display
     */
    public StatCard(final String labelKeyParam, final int valueParam) {
        this.labelKey = labelKeyParam;
        this.value = valueParam;
    }

    @Override
    protected Div initContent() {
        Div card = new Div();
        card.addClassName(StatsConstants.STATS_CARD_CLASS);
        card.addClassName(StatsConstants.SURFACE_CARD_CLASS);

        Div valueDiv = new Div();
        valueDiv.addClassName(StatsConstants.STATS_CARD_VALUE_CLASS);
        valueDiv.setText(String.valueOf(value));

        Div labelDiv = new Div();
        labelDiv.addClassName(StatsConstants.STATS_CARD_LABEL_CLASS);
        labelDiv.setText(getTranslation(labelKey));

        card.add(valueDiv, labelDiv);
        return card;
    }
}
