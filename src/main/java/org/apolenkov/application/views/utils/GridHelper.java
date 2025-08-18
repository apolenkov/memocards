package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
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
        grid.setWidth("100%");
        grid.setHeight("420px");
        grid.addClassName("data-grid");
        return grid;
    }

    /**
     * Create a grid with standard configuration
     */
    public static <T> Grid<T> createStandardGrid(Class<T> beanType, DataProvider<T, ?> dataProvider) {
        Grid<T> grid = createBasicGrid(beanType);
        grid.setDataProvider(dataProvider);
        return grid;
    }

    /**
     * Add a text column to grid
     */
    public static <T> Grid.Column<T> addTextColumn(
            Grid<T> grid, String header, ValueProvider<T, String> valueProvider) {
        return grid.addColumn(valueProvider).setHeader(header).setFlexGrow(2);
    }

    /**
     * Add a text column with custom flex grow
     */
    public static <T> Grid.Column<T> addTextColumn(
            Grid<T> grid, String header, ValueProvider<T, String> valueProvider, int flexGrow) {
        return grid.addColumn(valueProvider).setHeader(header).setFlexGrow(flexGrow);
    }

    /**
     * Add a status column with custom styling
     */
    public static <T> Grid.Column<T> addStatusColumn(
            Grid<T> grid, String header, ValueProvider<T, String> valueProvider, String className) {
        return grid.addComponentColumn(item -> {
                    Span status = new Span(valueProvider.apply(item));
                    status.addClassName(className);
                    return status;
                })
                .setHeader(header)
                .setFlexGrow(0)
                .setWidth("130px");
    }

    /**
     * Add an actions column with buttons
     */
    public static <T> Grid.Column<T> addActionsColumn(
            Grid<T> grid, String header, SerializableFunction<T, Button[]> buttonProvider) {
        return grid.addComponentColumn(item -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);
                    actions.add(buttonProvider.apply(item));
                    return actions;
                })
                .setHeader(header)
                .setWidth("220px")
                .setFlexGrow(0);
    }

    /**
     * Add a number column
     */
    public static <T> Grid.Column<T> addNumberColumn(
            Grid<T> grid, String header, ValueProvider<T, Number> valueProvider) {
        return grid.addColumn(valueProvider).setHeader(header).setFlexGrow(1);
    }

    /**
     * Add a date column
     */
    public static <T> Grid.Column<T> addDateColumn(
            Grid<T> grid, String header, ValueProvider<T, java.time.temporal.Temporal> valueProvider) {
        return grid.addColumn(valueProvider).setHeader(header).setFlexGrow(1);
    }

    /**
     * Add a boolean column with icon representation
     */
    public static <T> Grid.Column<T> addBooleanColumn(
            Grid<T> grid, String header, ValueProvider<T, Boolean> valueProvider) {
        return grid.addComponentColumn(item -> {
                    Boolean value = valueProvider.apply(item);
                    Span icon = new Span(value ? "✓" : "✗");
                    icon.addClassName(value ? "boolean-true" : "boolean-false");
                    return icon;
                })
                .setHeader(header)
                .setFlexGrow(0)
                .setWidth("80px");
    }

    /**
     * Configure grid for responsive design
     */
    public static <T> void configureResponsive(Grid<T> grid) {
        grid.addClassName("responsive-grid");
        grid.setMaxHeight("600px");
    }

    /**
     * Configure grid for compact display
     */
    public static <T> void configureCompact(Grid<T> grid) {
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("300px");
    }

    /**
     * Configure grid for full height
     */
    public static <T> void configureFullHeight(Grid<T> grid) {
        grid.setHeight("100%");
        grid.addClassName("full-height-grid");
    }

    /**
     * Add common grid features
     */
    public static <T> void addCommonFeatures(Grid<T> grid) {
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addClassName("enhanced-grid");
    }

    /**
     * Create a grid with common deck configuration
     */
    public static <T> Grid<T> createDeckGrid(Class<T> beanType, DataProvider<T, ?> dataProvider) {
        Grid<T> grid = createStandardGrid(beanType, dataProvider);
        addCommonFeatures(grid);
        configureResponsive(grid);
        return grid;
    }

    /**
     * Create a grid with common user configuration
     */
    public static <T> Grid<T> createUserGrid(Class<T> beanType, DataProvider<T, ?> dataProvider) {
        Grid<T> grid = createStandardGrid(beanType, dataProvider);
        addCommonFeatures(grid);
        configureCompact(grid);
        return grid;
    }
}
