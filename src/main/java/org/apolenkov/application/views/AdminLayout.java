package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.TopMenu;

/**
 * Administrative layout component with top navigation bar, language switcher, and content area.
 * This layout is specifically designed for admin-only pages and provides a consistent
 * interface for administrative operations.
 */
@AnonymousAllowed
public class AdminLayout extends AppLayout {

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;

    /**
     * Creates a new AdminLayout with required components.
     *
     * @param languageSwitcherValue component for language selection
     * @param topMenuValue component for top navigation and user menu
     */
    public AdminLayout(final LanguageSwitcher languageSwitcherValue, final TopMenu topMenuValue) {
        this.languageSwitcher = languageSwitcherValue;
        this.topMenu = topMenuValue;
    }

    /**
     * Initializes the layout components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
    }

    /**
     * Creates and configures the header content area for admin pages.
     * Sets up the top navigation bar with proper styling, spacing, and
     * component placement. The header includes the main menu on the left
     * and language switcher on the right, with admin-specific styling.
     */
    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");
        bar.addClassName("surface-panel");
        bar.addClassName("admin-layout__navbar");
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
        right.addClassName("surface-panel");
        right.setAlignItems(Alignment.CENTER);
        right.add(languageSwitcher);

        bar.add(topMenu, right);
        addToNavbar(true, bar);

        addClassName("admin-layout");
    }

    /**
     * Handles post-navigation processing for the admin layout.
     * This method is called after navigation occurs and performs
     * necessary updates to ensure the layout remains consistent with
     * the current application state.
     * Key actions include:
     * - Adding CSS classes to the content area for styling
     * - Refreshing the header menu to reflect authentication changes
     * - Applying admin-specific styling to the content area
     */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        if (getContent() != null) {
            getContent().addClassName("app-content");
            getContent().addClassName("admin-content");
        }
        // Refresh header menu (greeting, buttons) after route changes, including login/logout
        if (topMenu != null) {
            topMenu.refreshMenu();
        }
    }
}
