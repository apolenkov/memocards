package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.TopMenu;

/**
 * Public layout component for the application.
 *
 * <p>This layout provides the main application shell including the top navigation
 * bar with user menu, language switcher, and content area. It serves as the
 * primary layout for all public-facing views in the application.</p>
 *
 * <p>The layout features:</p>
 * <ul>
 *   <li>Top navigation bar with user menu and language selection</li>
 *   <li>Responsive design with proper spacing and alignment</li>
 *   <li>Content area for child views</li>
 *   <li>Automatic menu refresh after navigation changes</li>
 * </ul>
 *
 * <p>The layout automatically refreshes the header menu after route changes
 * to ensure the user's authentication state is accurately reflected.</p>
 */
@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;

    /**
     * Creates a new PublicLayout with required components.
     *
     * <p>Initializes the layout with language switcher and top menu components,
     * setting up the primary navigation section and header content.</p>
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
     *
     * <p>Sets up the top navigation bar with proper styling, spacing, and
     * component placement. The header includes the main menu on the left
     * and language switcher on the right.</p>
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
     *
     * <p>This method is called after navigation occurs and performs
     * necessary updates to ensure the layout remains consistent with
     * the current application state.</p>
     *
     * <p>Key actions include:</p>
     * <ul>
     *   <li>Adding CSS classes to the content area for styling</li>
     *   <li>Refreshing the header menu to reflect authentication changes</li>
     * </ul>
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
