package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * View component for displaying access denied (403 Forbidden) errors.
 *
 * <p>Shows user-friendly error message for unauthorized access attempts.</p>
 */
@Route(value = "access-denied", layout = PublicLayout.class)
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Creates a new AccessDeniedView.
     *
     * <p>This constructor initializes the view with a heading that displays
     * the localized access denied message. The view is designed to be simple
     * and informative, providing clear feedback to users about why they
     * cannot access the requested resource.</p>
     *
     * <p>The view layout includes:</p>
     *   <li><strong>Error Heading:</strong> H2 element with localized error message</li>
     *   <li><strong>Vertical Layout:</strong> Simple, centered layout structure</li>
     *   <li><strong>Consistent Styling:</strong> Follows application design patterns</li>
     * </ul>
     *
     * <p><strong>Internationalization:</strong> The error message is retrieved
     * using the {@code getTranslation("error.403")} method, ensuring that
     * the message is displayed in the user's preferred language.</p>
     */
    public AccessDeniedView() {
        add(new H2(getTranslation("error.403")));
    }

    /**
     * Gets the page title for this view.
     *
     * <p>This method implements the {@link HasDynamicTitle} interface to provide
     * a dynamic page title that reflects the current view's purpose. The title
     * is retrieved from the internationalization system to ensure proper
     * localization.</p>
     *
     * <p><strong>Title Behavior:</strong></p>
     *   <li><strong>Dynamic:</strong> Title changes based on current locale</li>
     *   <li><strong>Localized:</strong> Uses i18n message keys for translation</li>
     *   <li><strong>Consistent:</strong> Matches the heading displayed in the view</li>
     * </ul>
     *
     * <p><strong>Message Key:</strong> Uses "error.403" which should be defined
     * in the application's message bundles for all supported languages.</p>
     *
     * @return the localized page title for the access denied view
     * @see HasDynamicTitle#getPageTitle()
     * @see #getTranslation(String)
     */
    @Override
    public String getPageTitle() {
        return getTranslation("error.403");
    }
}
