package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Utility class for centralized layout creation and styling.
 * Eliminates duplication of layout creation patterns across the application.
 */
public final class LayoutHelper {

    private LayoutHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a centered vertical layout
     */
    public static VerticalLayout createCenteredVerticalLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);
        return layout;
    }

    /**
     * Create a button row layout
     */
    public static HorizontalLayout createButtonRow(Component... buttons) {
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        buttonRow.add(buttons);
        return buttonRow;
    }

    /**
     * Create a search row layout
     */
    public static HorizontalLayout createSearchRow(Component searchField, Component... filters) {
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        searchRow.add(searchField);

        if (filters.length > 0) {
            HorizontalLayout filtersLayout = new HorizontalLayout();
            filtersLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            filtersLayout.setSpacing(true);
            filtersLayout.add(filters);
            searchRow.add(filtersLayout);
        }

        return searchRow;
    }

    /**
     * Create a stats grid layout
     */
    public static HorizontalLayout createStatsGrid(Component... statCards) {
        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);

        statsGrid.add(statCards);
        return statsGrid;
    }
}
