package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Utility class for centralized layout creation and configuration.
 * Provides factory methods for creating consistently styled layouts.
 */
public final class LayoutHelper {

    private LayoutHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a centered vertical layout with consistent styling.
     * Creates a vertical layout that centers its content both horizontally and vertically.
     *
     * @return a configured VerticalLayout with centered content alignment
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
     * Creates a centered horizontal layout with consistent styling.
     * Creates a horizontal layout that centers its content both horizontally and vertically.
     *
     * @return a configured HorizontalLayout with centered content alignment
     */
    public static HorizontalLayout createCenteredHorizontalLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);
        return layout;
    }

    /**
     * Creates a button row layout with consistent spacing and alignment.
     *
     * @param buttons buttons to arrange in a row
     * @return configured horizontal layout for buttons
     */
    public static HorizontalLayout createButtonRow(Component... buttons) {
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        buttonRow.add(buttons);
        return buttonRow;
    }

    /**
     * Creates a search row layout with search field and optional filters.
     *
     * @param searchField the search input component
     * @param filters optional filter components
     * @return configured horizontal layout for search functionality
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
