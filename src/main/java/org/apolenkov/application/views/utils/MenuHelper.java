package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;

/**
 * Utility class for centralized menu and navigation element creation.
 * Eliminates duplication of menu creation patterns across the application.
 */
public final class MenuHelper {

    private MenuHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a main navigation menu
     */
    public static HorizontalLayout createMainMenu(List<MenuItem> items) {
        HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);
        menu.setAlignItems(FlexComponent.Alignment.CENTER);
        menu.addClassName("main-menu");

        for (MenuItem item : items) {
            if (item.isVisible()) {
                menu.add(createMenuButton(item));
            }
        }

        return menu;
    }

    /**
     * Create a dropdown menu
     */
    public static VerticalLayout createDropdownMenu(String label, List<MenuItem> items) {
        VerticalLayout dropdown = new VerticalLayout();
        dropdown.addClassName("dropdown-menu");
        dropdown.setSpacing(false);
        dropdown.setPadding(false);

        Button trigger = ButtonHelper.createTertiaryButton(label, e -> {
            dropdown.setVisible(!dropdown.isVisible());
        });
        trigger.addClassName("dropdown-trigger");

        dropdown.add(trigger);

        VerticalLayout itemsContainer = new VerticalLayout();
        itemsContainer.addClassName("dropdown-items");
        itemsContainer.setSpacing(false);
        itemsContainer.setPadding(false);

        for (MenuItem item : items) {
            if (item.isVisible()) {
                itemsContainer.add(createDropdownItem(item));
            }
        }

        dropdown.add(itemsContainer);
        return dropdown;
    }

    /**
     * Create breadcrumbs navigation
     */
    public static HorizontalLayout createBreadcrumbs(List<BreadcrumbItem> items) {
        HorizontalLayout breadcrumbs = new HorizontalLayout();
        breadcrumbs.setSpacing(true);
        breadcrumbs.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumbs.addClassName("breadcrumbs");

        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);

            if (i > 0) {
                Span separator = new Span(">");
                separator.addClassName("breadcrumb-separator");
                breadcrumbs.add(separator);
            }

            if (i == items.size() - 1) {
                // Last item - not clickable
                Span current = new Span(item.getText());
                current.addClassName("breadcrumb-current");
                breadcrumbs.add(current);
            } else {
                // Clickable item
                Anchor link = new Anchor(item.getRoute(), item.getText());
                link.addClassName("breadcrumb-link");
                breadcrumbs.add(link);
            }
        }

        return breadcrumbs;
    }

    /**
     * Create a tab navigation menu
     */
    public static HorizontalLayout createTabMenu(List<TabItem> items) {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setSpacing(false);
        tabs.addClassName("tab-menu");

        for (TabItem item : items) {
            final Button tab = ButtonHelper.createTertiaryButton(item.getText(), e -> {
                // Remove active class from all tabs
                tabs.getChildren().forEach(tabBtn -> tabBtn.removeClassName("tab-active"));
                // Add active class to clicked tab using the event source
                Button source = e.getSource();
                source.addClassName("tab-active");
                // Execute tab action
                item.getAction().run();
            });

            if (item.isActive()) {
                tab.addClassName("tab-active");
            }

            tab.addClassName("tab-button");
            tabs.add(tab);
        }

        return tabs;
    }

    /**
     * Create a sidebar menu
     */
    public static VerticalLayout createSidebarMenu(List<MenuItem> items) {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setSpacing(false);
        sidebar.setPadding(false);
        sidebar.addClassName("sidebar-menu");

        for (MenuItem item : items) {
            if (item.isVisible()) {
                sidebar.add(createSidebarItem(item));
            }
        }

        return sidebar;
    }

    /**
     * Create a footer menu
     */
    public static HorizontalLayout createFooterMenu(List<MenuItem> items) {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footer.addClassName("footer-menu");

        for (MenuItem item : items) {
            if (item.isVisible()) {
                footer.add(createFooterItem(item));
            }
        }

        return footer;
    }

    /**
     * Create a mobile hamburger menu
     */
    public static VerticalLayout createMobileMenu(List<MenuItem> items) {
        VerticalLayout mobileMenu = new VerticalLayout();
        mobileMenu.setSpacing(false);
        mobileMenu.setPadding(false);
        mobileMenu.addClassName("mobile-menu");

        for (MenuItem item : items) {
            if (item.isVisible()) {
                mobileMenu.add(createMobileItem(item));
            }
        }

        return mobileMenu;
    }

    /**
     * Create a context menu
     */
    public static VerticalLayout createContextMenu(List<MenuItem> items) {
        VerticalLayout contextMenu = new VerticalLayout();
        contextMenu.setSpacing(false);
        contextMenu.setPadding(false);
        contextMenu.addClassName("context-menu");

        for (MenuItem item : items) {
            if (item.isVisible()) {
                contextMenu.add(createContextItem(item));
            }
        }

        return contextMenu;
    }

    /**
     * Create a pagination menu
     */
    public static HorizontalLayout createPaginationMenu(
            int currentPage, int totalPages, java.util.function.Consumer<Integer> pageChangeHandler) {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setSpacing(true);
        pagination.setAlignItems(FlexComponent.Alignment.CENTER);
        pagination.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        pagination.addClassName("pagination-menu");

        // Previous button
        if (currentPage > 1) {
            Button prevBtn = ButtonHelper.createTertiaryButton("←", e -> pageChangeHandler.accept(currentPage - 1));
            prevBtn.addClassName("pagination-prev");
            pagination.add(prevBtn);
        }

        // Page numbers
        for (int i = Math.max(1, currentPage - 2); i <= Math.min(totalPages, currentPage + 2); i++) {
            final int pageNumber = i;
            Button pageBtn =
                    ButtonHelper.createTertiaryButton(String.valueOf(i), e -> pageChangeHandler.accept(pageNumber));
            if (i == currentPage) {
                pageBtn.addClassName("pagination-current");
            }
            pageBtn.addClassName("pagination-page");
            pagination.add(pageBtn);
        }

        // Next button
        if (currentPage < totalPages) {
            Button nextBtn = ButtonHelper.createTertiaryButton("→", e -> pageChangeHandler.accept(currentPage + 1));
            nextBtn.addClassName("pagination-next");
            pagination.add(nextBtn);
        }

        return pagination;
    }

    // Private helper methods
    private static Button createMenuButton(MenuItem item) {
        Button button = ButtonHelper.createTertiaryButton(item.getText(), e -> {
            if (item.getAction() != null) {
                item.getAction().run();
            } else if (item.getRoute() != null) {
                NavigationHelper.navigateTo(item.getRoute());
            }
        });

        if (item.getIcon() != null) {
            button.setIcon(item.getIcon());
        }

        button.addClassName("menu-button");
        return button;
    }

    private static Component createDropdownItem(MenuItem item) {
        Button itemBtn = ButtonHelper.createTertiaryButton(item.getText(), e -> {
            if (item.getAction() != null) {
                item.getAction().run();
            } else if (item.getRoute() != null) {
                NavigationHelper.navigateTo(item.getRoute());
            }
        });

        itemBtn.addClassName("dropdown-item");
        itemBtn.setWidthFull();
        return itemBtn;
    }

    private static Component createSidebarItem(MenuItem item) {
        Button itemBtn = ButtonHelper.createTertiaryButton(item.getText(), e -> {
            if (item.getAction() != null) {
                item.getAction().run();
            } else if (item.getRoute() != null) {
                NavigationHelper.navigateTo(item.getRoute());
            }
        });

        if (item.getIcon() != null) {
            itemBtn.setIcon(item.getIcon());
        }

        itemBtn.addClassName("sidebar-item");
        itemBtn.setWidthFull();
        return itemBtn;
    }

    private static Component createFooterItem(MenuItem item) {
        if (item.getRoute() != null) {
            Anchor link = new Anchor(item.getRoute(), item.getText());
            link.addClassName("footer-link");
            return link;
        } else {
            Span text = new Span(item.getText());
            text.addClassName("footer-text");
            return text;
        }
    }

    private static Component createMobileItem(MenuItem item) {
        Button itemBtn = ButtonHelper.createTertiaryButton(item.getText(), e -> {
            if (item.getAction() != null) {
                item.getAction().run();
            } else if (item.getRoute() != null) {
                NavigationHelper.navigateTo(item.getRoute());
            }
        });

        if (item.getIcon() != null) {
            itemBtn.setIcon(item.getIcon());
        }

        itemBtn.addClassName("mobile-item");
        itemBtn.setWidthFull();
        return itemBtn;
    }

    private static Component createContextItem(MenuItem item) {
        Button itemBtn = ButtonHelper.createTertiaryButton(item.getText(), e -> {
            if (item.getAction() != null) {
                item.getAction().run();
            } else if (item.getRoute() != null) {
                NavigationHelper.navigateTo(item.getRoute());
            }
        });

        if (item.getIcon() != null) {
            itemBtn.setIcon(item.getIcon());
        }

        itemBtn.addClassName("context-item");
        itemBtn.setWidthFull();
        return itemBtn;
    }

    // Data classes
    public static class MenuItem {
        private final String text;
        private final String route;
        private final com.vaadin.flow.component.icon.Icon icon;
        private final Runnable action;
        private final boolean visible;

        public MenuItem(
                String text, String route, com.vaadin.flow.component.icon.Icon icon, Runnable action, boolean visible) {
            this.text = text;
            this.route = route;
            this.icon = icon;
            this.action = action;
            this.visible = visible;
        }

        public MenuItem(String text, String route) {
            this(text, route, null, null, true);
        }

        public MenuItem(String text, Runnable action) {
            this(text, null, null, action, true);
        }

        public MenuItem(String text, String route, com.vaadin.flow.component.icon.Icon icon) {
            this(text, route, icon, null, true);
        }

        public String getText() {
            return text;
        }

        public String getRoute() {
            return route;
        }

        public com.vaadin.flow.component.icon.Icon getIcon() {
            return icon;
        }

        public Runnable getAction() {
            return action;
        }

        public boolean isVisible() {
            return visible;
        }
    }

    public static class BreadcrumbItem {
        private final String text;
        private final String route;

        public BreadcrumbItem(String text, String route) {
            this.text = text;
            this.route = route;
        }

        public String getText() {
            return text;
        }

        public String getRoute() {
            return route;
        }
    }

    public static class TabItem {
        private final String text;
        private final Runnable action;
        private final boolean active;

        public TabItem(String text, Runnable action, boolean active) {
            this.text = text;
            this.action = action;
            this.active = active;
        }

        public TabItem(String text, Runnable action) {
            this(text, action, false);
        }

        public String getText() {
            return text;
        }

        public Runnable getAction() {
            return action;
        }

        public boolean isActive() {
            return active;
        }
    }
}
