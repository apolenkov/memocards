package org.apolenkov.application.views.landing.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Hero section component for the landing page.
 * Displays the main title, subtitle, hero image, and action buttons.
 */
public final class HeroSection extends Composite<Div> {

    private final transient Authentication auth;

    // Event Registrations
    private Registration heroImageClickListenerRegistration;

    /**
     * Creates a new HeroSection component.
     *
     * @param authParam the current authentication context
     */
    public HeroSection(final Authentication authParam) {
        this.auth = authParam;
    }

    @Override
    protected Div initContent() {
        Div content = new Div();
        content.addClassName(LandingConstants.SURFACE_PANEL_CLASS);
        content.addClassName(LandingConstants.LANDING_HERO_SECTION_CLASS);

        Div heroIcon = createHeroIcon();
        H1 title = createTitle();
        Paragraph subtitle = createSubtitle();
        HorizontalLayout actions = createActionButtons(auth);

        content.add(heroIcon, title, subtitle, actions);
        return content;
    }

    /**
     * Creates the hero icon container with clickable image.
     *
     * @return Div containing the hero image
     */
    private Div createHeroIcon() {
        Div heroIcon = new Div();
        heroIcon.addClassName(LandingConstants.LANDING_HERO_ICON_CLASS);

        Image hero = new Image(
                new StreamResource(LandingConstants.PIXEL_ICON_NAME, () -> getClass()
                        .getResourceAsStream(LandingConstants.PIXEL_ICON_PATH)),
                getTranslation(LandingConstants.LANDING_HERO_ALT_KEY));

        hero.addClassName(LandingConstants.LANDING_HERO_IMAGE_CLASS);
        heroImageClickListenerRegistration = hero.addClickListener(e -> handleHeroClick());

        heroIcon.add(hero);
        return heroIcon;
    }

    /**
     * Handles hero image click based on authentication status.
     */
    private void handleHeroClick() {
        Authentication currentAuth = getCurrentAuthentication();
        if (isAuthenticated(currentAuth)) {
            NavigationHelper.navigateToDecks();
        } else {
            NavigationHelper.navigateToLogin();
        }
    }

    /**
     * Creates the main title.
     *
     * @return H1 element with the app title
     */
    private H1 createTitle() {
        H1 title = new H1(getTranslation(LandingConstants.APP_TITLE_KEY));
        title.addClassName(LandingConstants.LANDING_HERO_TITLE_CLASS);
        return title;
    }

    /**
     * Creates the subtitle paragraph.
     *
     * @return Paragraph element with the subtitle
     */
    private Paragraph createSubtitle() {
        Paragraph subtitle = new Paragraph(getTranslation(LandingConstants.LANDING_SUBTITLE_KEY));
        subtitle.addClassName(LandingConstants.LANDING_HERO_SUBTITLE_CLASS);
        return subtitle;
    }

    /**
     * Creates action buttons based on authentication status.
     *
     * @param authentication the current authentication context
     * @return HorizontalLayout containing action buttons
     */
    private HorizontalLayout createActionButtons(final Authentication authentication) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (isAuthenticated(authentication)) {
            addAuthenticatedButtons(actions, authentication);
        } else {
            addUnauthenticatedButtons(actions);
        }

        return actions;
    }

    /**
     * Adds buttons for authenticated users.
     *
     * @param actions the layout to add buttons to
     * @param authentication the authentication context
     */
    private void addAuthenticatedButtons(final HorizontalLayout actions, final Authentication authentication) {
        if (hasUserRole(authentication)) {
            Button goToDecks = ButtonHelper.createPrimaryButton(
                    getTranslation(LandingConstants.LANDING_GO_TO_DECKS_KEY), e -> NavigationHelper.navigateToDecks());
            actions.add(goToDecks);
        }
    }

    /**
     * Adds buttons for unauthenticated users.
     *
     * @param actions the layout to add buttons to
     */
    private void addUnauthenticatedButtons(final HorizontalLayout actions) {
        Button login = ButtonHelper.createPrimaryButton(
                getTranslation(LandingConstants.AUTH_LOGIN_KEY), e -> NavigationHelper.navigateToLogin());

        Button register = ButtonHelper.createTertiaryButton(
                getTranslation(LandingConstants.AUTH_REGISTER_KEY), e -> NavigationHelper.navigateToRegister());

        actions.add(login, register);
    }

    /**
     * Checks if the user is authenticated.
     *
     * @param authentication the authentication context
     * @return true if user is authenticated, false otherwise
     */
    private boolean isAuthenticated(final Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    /**
     * Checks if the user has USER role.
     *
     * @param authentication the authentication context
     * @return true if user has USER role, false otherwise
     */
    private boolean hasUserRole(final Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
    }

    /**
     * Gets the current authentication context.
     * This method can be overridden for testing purposes.
     *
     * @return the current authentication context
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (heroImageClickListenerRegistration != null) {
            heroImageClickListenerRegistration.remove();
            heroImageClickListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
