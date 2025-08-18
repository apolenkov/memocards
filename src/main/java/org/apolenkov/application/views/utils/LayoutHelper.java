package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
     * Create a centered horizontal layout
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
     * Create a header layout with title and actions
     */
    public static HorizontalLayout createHeaderLayout(String title, Component... actions) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassName("view-header");

        H2 titleElement = new H2(title);
        titleElement.addClassName("view-header__title");
        header.add(titleElement);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setSpacing(true);
            actionsLayout.add(actions);
            header.add(actionsLayout);
        }

        return header;
    }

    /**
     * Create a section container
     */
    public static Div createSectionContainer(String className) {
        Div section = new Div();
        section.addClassName(className);
        section.addClassName("view-section");
        return section;
    }

    /**
     * Create a card container
     */
    public static Div createCardContainer(String className) {
        Div card = new Div();
        card.addClassName(className);
        card.addClassName("view-card");
        return card;
    }

    /**
     * Create a form layout with consistent spacing
     */
    public static VerticalLayout createFormLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidth("100%");
        layout.addClassName("form-layout");
        return layout;
    }

    /**
     * Create a button row layout
     */
    public static HorizontalLayout createButtonRow(Component... buttons) {
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonRow.addClassName("button-row");
        buttonRow.add(buttons);
        return buttonRow;
    }

    /**
     * Create a centered button row layout
     */
    public static HorizontalLayout createCenteredButtonRow(Component... buttons) {
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonRow.addClassName("button-row");
        buttonRow.add(buttons);
        return buttonRow;
    }

    /**
     * Create a search row layout
     */
    public static HorizontalLayout createSearchRow(Component searchField, Component... filters) {
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidth("100%");
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        searchRow.addClassName("search-row");

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
        statsGrid.setWidth("100%");
        statsGrid.setSpacing(true);
        statsGrid.addClassName("stats-grid");
        statsGrid.add(statCards);
        return statsGrid;
    }

    /**
     * Create a responsive grid layout
     */
    public static HorizontalLayout createResponsiveGrid(Component... items) {
        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidth("100%");
        grid.setSpacing(true);
        // FlexWrap not available in this version, using alternative approach
        grid.addClassName("responsive-grid");
        grid.add(items);
        return grid;
    }

    /**
     * Create a sidebar layout
     */
    public static HorizontalLayout createSidebarLayout(Component sidebar, Component mainContent) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setSpacing(false);
        layout.addClassName("sidebar-layout");

        sidebar.addClassName("sidebar-layout__sidebar");
        mainContent.addClassName("sidebar-layout__main");

        layout.add(sidebar, mainContent);
        return layout;
    }

    /**
     * Create a modal layout
     */
    public static VerticalLayout createModalLayout(String title, Component content, Component... actions) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidth("100%");
        layout.addClassName("modal-layout");

        H3 titleElement = new H3(title);
        titleElement.addClassName("modal-layout__title");
        layout.add(titleElement);

        layout.add(content);

        if (actions.length > 0) {
            HorizontalLayout actionsLayout = createButtonRow(actions);
            layout.add(actionsLayout);
        }

        return layout;
    }
}
