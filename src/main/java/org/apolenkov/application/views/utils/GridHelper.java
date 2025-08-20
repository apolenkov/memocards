package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;

/**
 * Utility class for centralized grid creation and configuration.
 * Eliminates duplication of grid setup patterns across the application.
 */
public final class GridHelper {

    private GridHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a basic grid with common configuration
     */
    public static <T> Grid<T> createBasicGrid(Class<T> beanType) {
        Grid<T> grid = new Grid<>(beanType, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();

        return grid;
    }

    /**
     * Add a text column with custom flex grow
     */
    public static <T> void addTextColumn(
            Grid<T> grid, String header, ValueProvider<T, String> valueProvider, int flexGrow) {
        grid.addColumn(valueProvider).setHeader(header).setFlexGrow(flexGrow);
    }

    /**
     * Add an actions column with buttons
     */
    public static <T> void addActionsColumn(
            Grid<T> grid, String header, SerializableFunction<T, Button[]> buttonProvider) {
        grid.addComponentColumn(item -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);
                    actions.add(buttonProvider.apply(item));
                    return actions;
                })
                .setHeader(header)
                .setFlexGrow(0);
    }

    /**
     * Add common grid features
     */
    public static <T> void addCommonFeatures(Grid<T> grid) {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }
}
