package org.apolenkov.application.views.presentation.layouts;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.views.presentation.components.LanguageSwitcher;
import org.apolenkov.application.views.presentation.components.TopMenu;

/**
 * Public layout component with top navigation bar, language switcher, and content area.
 */
@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;

    /**
     * Creates a new PublicLayout with required components.
     *
     * @param languageSwitcherValue component for language selection
     * @param topMenuValue component for top navigation and user menu
     */
    public PublicLayout(final LanguageSwitcher languageSwitcherValue, final TopMenu topMenuValue) {
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
     * Creates and configures the header content area.
     * Sets up the top navigation bar with proper styling, spacing, and
     * component placement. The header includes the main menu on the left
     * and language switcher on the right.
     */
    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");
        bar.addClassName("surface-panel");
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

        addClassName("public-layout");
    }

    /**
     * Handles post-navigation processing for the layout.
     * This method is called after navigation occurs and performs
     * necessary updates to ensure the layout remains consistent with
     * the current application state.
     * Key actions include:
     * - Adding CSS classes to the content area for styling
     * - Refreshing the header menu to reflect authentication changes
     */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        if (getContent() != null) {
            getContent().addClassName("app-content");
        }
        // Refresh header menu (greeting, buttons) after route changes, including login/logout
        if (topMenu != null) {
            topMenu.refreshMenu();
        }
    }
}
