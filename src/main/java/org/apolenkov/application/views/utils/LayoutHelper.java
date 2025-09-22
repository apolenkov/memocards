package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Utility class for centralized layout creation and configuration.
 * Provides factory methods for creating consistently styled layouts.
 */
public final class LayoutHelper {

    private LayoutHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a search row layout with search field and optional filters.
     *
     * @param searchField the search input component
     * @param filters optional filter components
     * @return configured horizontal layout for search functionality
     */
    public static HorizontalLayout createSearchRow(final Component searchField, final Component... filters) {
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

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
}
