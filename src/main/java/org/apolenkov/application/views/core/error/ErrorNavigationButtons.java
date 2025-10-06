package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Component responsible for creating and managing navigation buttons in error views.
 * Provides buttons for going home and trying again with proper styling and click handlers.
 */
public final class ErrorNavigationButtons extends Composite<HorizontalLayout> {

    private final Button goHomeButton;
    private final Button tryAgainButton;
    private final String fromRoute;

    /**
     * Creates a new ErrorNavigationButtons component.
     *
     * @param fromRouteParam the route to navigate back to when "try again" is clicked
     */
    public ErrorNavigationButtons(final String fromRouteParam) {
        this.fromRoute = fromRouteParam;
        this.goHomeButton = createGoHomeButton();
        this.tryAgainButton = createTryAgainButton();
    }

    /**
     * Creates the go home button with navigation functionality.
     *
     * @return configured go home button
     */
    private Button createGoHomeButton() {
        return ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_GO_HOME_KEY),
                e -> NavigationHelper.navigateToHome(),
                ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the try again button with navigation functionality.
     *
     * @return configured try again button
     */
    private Button createTryAgainButton() {
        return ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_TRY_AGAIN_KEY),
                e -> NavigationHelper.navigateTo(fromRoute),
                ButtonVariant.LUMO_TERTIARY);
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout layout = new HorizontalLayout(goHomeButton, tryAgainButton);
        layout.setSpacing(true);
        layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);
        return layout;
    }
}
