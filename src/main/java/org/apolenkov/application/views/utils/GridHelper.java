package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;

/**
 * Utility class for centralized grid creation and configuration.
 * Provides factory methods for creating consistently styled data grids.
 */
public final class GridHelper {

    private GridHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a basic grid with common configuration.
     * Creates a grid component with standard styling including row stripes
     * and full width layout.
     *
     * @param <T> the type of data items in the grid
     * @param beanType the class type of the data items
     * @return a configured Grid component with standard styling
     */
    public static <T> Grid<T> createBasicGrid(Class<T> beanType) {
        Grid<T> grid = new Grid<>(beanType, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();

        return grid;
    }

    /**
     * Adds a text column with custom flex grow to the grid.
     * Creates and configures a grid column for text display with the
     * specified header, value extraction function, and flex grow setting.
     *
     * @param <T> the type of data items in the grid
     * @param grid the grid to add the column to
     * @param header the header text to display for the column
     * @param valueProvider the function to extract text values for the column
     * @param flexGrow the flex grow value for the column width distribution
     */
    public static <T> void addTextColumn(
            Grid<T> grid, String header, ValueProvider<T, String> valueProvider, int flexGrow) {
        grid.addColumn(valueProvider).setHeader(header).setFlexGrow(flexGrow);
    }

    /**
     * Adds an actions column with buttons to the grid.
     *
     * @param <T> the type of data items in the grid
     * @param grid the grid to add the column to
     * @param header the header text to display for the column
     * @param buttonProvider function that provides buttons for each row
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
     * Adds common grid features for consistent behavior.
     *
     * @param <T> the type of data items in the grid
     * @param grid the grid to configure
     */
    public static <T> void addCommonFeatures(Grid<T> grid) {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }
}
