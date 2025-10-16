package org.apolenkov.application.views.core.layout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.core.navigation.DesktopNavigationBar;
import org.apolenkov.application.views.core.navigation.LanguageSwitcher;
import org.apolenkov.application.views.core.navigation.MobileLanguageSwitcher;
import org.apolenkov.application.views.core.navigation.MobileNavigationMenu;

/**
 * Responsive public layout component with adaptive navigation.
 * Provides desktop navbar and mobile drawer with hamburger menu.
 *
 * <p>Desktop (≥768px): Full horizontal navbar with buttons and greeting
 * <p>Mobile (<768px): Compact navbar with hamburger, drawer with SideNav
 *
 * <p>Vaadin AppLayout automatically manages:
 * <ul>
 *   <li>Drawer overlay mode on small screens</li>
 *   <li>DrawerToggle hamburger icon animation</li>
 *   <li>Drawer open/close state</li>
 * </ul>
 */
@AnonymousAllowed
public class PublicLayout extends AppLayout {

    // ==================== Dependencies ====================

    private final LanguageSwitcher desktopLanguageSwitcher;
    private final MobileLanguageSwitcher mobileLanguageSwitcher;
    private final MobileNavigationMenu mobileNavigationMenu;
    private final DesktopNavigationBar desktopNavigationBar;

    // References to layouts for onAttach
    private HorizontalLayout desktopNavbarRight;
    private HorizontalLayout mobileNavbarRight;

    // ==================== Constructor ====================

    /**
     * Creates a new PublicLayout with required components.
     *
     * @param desktopSwitcher language switcher for desktop navbar
     * @param mobileSwitcher language switcher for mobile navbar (separate @UIScope class!)
     * @param mobileNavMenu mobile navigation menu for drawer
     * @param desktopNavBar desktop navigation bar for navbar
     */
    public PublicLayout(
            final LanguageSwitcher desktopSwitcher,
            final MobileLanguageSwitcher mobileSwitcher,
            final MobileNavigationMenu mobileNavMenu,
            final DesktopNavigationBar desktopNavBar) {
        this.desktopLanguageSwitcher = desktopSwitcher;
        this.mobileLanguageSwitcher = mobileSwitcher;
        this.mobileNavigationMenu = mobileNavMenu;
        this.desktopNavigationBar = desktopNavBar;
    }

    // ==================== Lifecycle ====================

    /**
     * Initializes the layout components after dependency injection is complete.
     * Sets up responsive layout with drawer for mobile and navbar for desktop.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setPrimarySection(Section.NAVBAR);
        addClassName(CoreConstants.PUBLIC_LAYOUT_CLASS);

        setupDrawer();
        setupNavbar();

        // Ensure drawer is closed by default (especially important for desktop)
        setDrawerOpened(false);
    }

    /**
     * Adds @UIScope LanguageSwitcher components when the layout is attached to the UI.
     * At this point, all @UIScope components are ready.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Add desktop LanguageSwitcher
        if (desktopNavbarRight != null && desktopLanguageSwitcher != null) {
            desktopNavbarRight.add(desktopLanguageSwitcher);
        }

        // Add mobile LanguageSwitcher
        if (mobileNavbarRight != null && mobileLanguageSwitcher != null) {
            mobileNavbarRight.add(mobileLanguageSwitcher);
        }

        // Ensure drawer is closed when layout is attached (double-check for desktop)
        setDrawerOpened(false);
    }

    // ==================== Drawer Setup (Mobile) ====================

    /**
     * Sets up the drawer slot with mobile navigation menu.
     * Drawer automatically becomes overlay on small screens (managed by Vaadin).
     */
    private void setupDrawer() {
        addToDrawer(mobileNavigationMenu);
    }

    // ==================== Navbar Setup (Desktop + Mobile) ====================

    /**
     * Sets up navbar with responsive layouts.
     * - Desktop (≥768px): Full navigation bar with buttons
     * - Mobile (<768px): Compact bar with hamburger + logo + language
     */
    private void setupNavbar() {
        // Desktop navbar (hidden on mobile via CSS)
        HorizontalLayout desktopBar = createDesktopNavbar();

        // Mobile navbar (hidden on desktop via CSS)
        HorizontalLayout mobileBar = createMobileNavbar();

        addToNavbar(true, desktopBar, mobileBar);
    }

    /**
     * Creates desktop navigation bar with full menu and language switcher.
     * Hidden on mobile via CSS (Display.Breakpoint.Small.HIDDEN equivalent).
     *
     * @return configured desktop navbar layout
     */
    private HorizontalLayout createDesktopNavbar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName(CoreConstants.MAIN_LAYOUT_NAVBAR_CLASS);
        bar.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        bar.addClassName(CoreConstants.DESKTOP_NAV_BAR_CLASS);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Add navigation bar sections
        bar.add(desktopNavigationBar);

        // Create right section for language switcher (will be populated in onAttach)
        HorizontalLayout right = new HorizontalLayout();
        right.addClassName(CoreConstants.MAIN_LAYOUT_RIGHT_CLASS);
        right.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        right.setAlignItems(Alignment.CENTER);

        // Store reference for onAttach
        this.desktopNavbarRight = right;

        bar.add(right);
        return bar;
    }

    /**
     * Creates mobile navigation bar with 3 areas: hamburger (left), logo (center), language (right).
     * Hidden on desktop via CSS (Display.Breakpoint.Medium.FLEX equivalent).
     *
     * @return configured mobile navbar layout
     */
    private HorizontalLayout createMobileNavbar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName(CoreConstants.MOBILE_NAVBAR_CLASS);
        bar.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Left: Hamburger menu only
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.addClassName(CoreConstants.MOBILE_NAVBAR_LEFT_CLASS);

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel(getTranslation(CoreConstants.MAIN_DECKS_KEY)); // Accessible label
        drawerToggle.addClassName(CoreConstants.DRAWER_TOGGLE_WRAPPER_CLASS);
        left.add(drawerToggle);

        // Center: Text Logo
        HorizontalLayout center = new HorizontalLayout();
        center.setAlignItems(Alignment.CENTER);
        center.setJustifyContentMode(JustifyContentMode.CENTER);
        center.addClassName(CoreConstants.MOBILE_NAVBAR_CENTER_CLASS);

        Anchor textLogo = createTextLogoAnchor();
        center.add(textLogo);

        // Right: Mobile language switcher (will be populated in onAttach)
        HorizontalLayout right = new HorizontalLayout();
        right.setAlignItems(Alignment.CENTER);
        right.addClassName(CoreConstants.MOBILE_NAVBAR_RIGHT_CLASS);

        // Store reference for onAttach
        this.mobileNavbarRight = right;

        bar.add(left, center, right);
        return bar;
    }

    /**
     * Creates text logo anchor for mobile navbar.
     *
     * @return configured Anchor with text logo
     */
    private Anchor createTextLogoAnchor() {
        Anchor anchor = new Anchor(RouteConstants.ROOT_PATH, "");

        Span textLogo = new Span(getTranslation(CoreConstants.APP_TITLE_KEY));
        textLogo.addClassName(CoreConstants.MOBILE_TEXT_LOGO_CLASS);
        anchor.add(textLogo);
        return anchor;
    }

    // ==================== Lifecycle Callbacks ====================

    /**
     * Handles post-navigation processing for the layout.
     * Refreshes navigation menus to reflect authentication changes and adds CSS classes.
     *
     * <p>Key actions:
     * <ul>
     *   <li>Adding CSS classes to the content area for styling</li>
     *   <li>Refreshing desktop and mobile menus for auth state changes</li>
     *   <li>Auto-closing drawer on mobile after navigation</li>
     * </ul>
     */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        if (getContent() != null) {
            getContent().addClassName(CoreConstants.APP_CONTENT_CLASS);
        }

        // Refresh both navigation menus after route changes (login/logout)
        // Only refresh if authentication state might have changed
        if (desktopNavigationBar != null) {
            desktopNavigationBar.refreshMenuIfNeeded();
        }
        if (mobileNavigationMenu != null) {
            mobileNavigationMenu.refreshMenuIfNeeded();
        }

        // Note: Drawer closing is handled immediately by click listeners in MobileNavigationMenu
    }
}
