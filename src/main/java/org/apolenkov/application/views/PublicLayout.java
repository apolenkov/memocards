package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.TopMenu;

/**
 * Public layout component with top navigation bar, language switcher, and content area.
 */
@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;

    /**
     * Creates a new PublicLayout with required components.
     * Initializes the layout with language switcher and top menu components,
     * setting up the primary navigation section and header content.
     *
     * @param languageSwitcher component for language selection
     * @param topMenu component for top navigation and user menu
     */
    public PublicLayout(LanguageSwitcher languageSwitcher, TopMenu topMenu) {
        this.languageSwitcher = languageSwitcher;
        this.topMenu = topMenu;
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
     *
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
