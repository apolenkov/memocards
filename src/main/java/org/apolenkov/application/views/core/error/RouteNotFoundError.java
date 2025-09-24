package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom error view for handling 404 Not Found errors.
 * This component is automatically used by Vaadin when a route is not found.
 */
@Route(value = RouteConstants.ERROR_404_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class RouteNotFoundError extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteNotFoundError.class);

    // Translation keys
    private static final String ERROR_404_KEY = "error.404";
    private static final String ERROR_404_TITLE_KEY = "error.404.title";
    private static final String ERROR_404_DESCRIPTION_KEY = "error.404.description";
    private static final String ERROR_404_SUGGESTION_KEY = "error.404.suggestion";
    private static final String ERROR_404_GO_HOME_KEY = "error.404.goHome";
    private static final String MAIN_GO_HOME_KEY = "main.gohome";

    /**
     * Creates a new RouteNotFoundError view.
     */
    public RouteNotFoundError() {
        // Constructor for dependency injection
    }

    /**
     * Initializes the UI components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void initializeUI() {
        LOGGER.debug("Initializing RouteNotFoundError UI components");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        createErrorContent();

        LOGGER.debug("RouteNotFoundError UI initialization completed");
    }

    /**
     * Handles the {@link NotFoundException} by logging a debug message and returning
     * the HTTP status code for "Not Found" (404).
     *
     * @param event the {@link BeforeEnterEvent} that triggered the error
     * @param parameter the {@link ErrorParameter} containing the {@link NotFoundException}
     * @return the HTTP status code {@link HttpServletResponse#SC_NOT_FOUND}
     */
    @Override
    public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<NotFoundException> parameter) {
        LOGGER.debug("Handling NotFoundException, displaying custom 404 page");
        return HttpServletResponse.SC_NOT_FOUND;
    }

    /**
     * Creates the main error content section.
     */
    private void createErrorContent() {
        Div errorContainer = new Div();
        errorContainer.addClassName("not-found-container");
        errorContainer.addClassName("surface-panel");
        errorContainer.getStyle().setTextAlign(Style.TextAlign.CENTER);

        // Error code (404)
        H1 errorCode = new H1();
        errorCode.addClassName("not-found__code");
        errorCode.setText(getTranslation(ERROR_404_KEY));

        // Error title
        H2 errorTitle = new H2();
        errorTitle.addClassName("not-found__title");
        errorTitle.setText(getTranslation(ERROR_404_TITLE_KEY));

        // Error description
        Paragraph errorDescription = new Paragraph();
        errorDescription.addClassName("not-found__description");
        errorDescription.setText(getTranslation(ERROR_404_DESCRIPTION_KEY));

        // Error suggestion
        Paragraph errorSuggestion = new Paragraph();
        errorSuggestion.addClassName("not-found__suggestion");
        errorSuggestion.setText(getTranslation(ERROR_404_SUGGESTION_KEY));

        // Additional suggestion
        Paragraph goHomeSuggestion = new Paragraph();
        goHomeSuggestion.addClassName("not-found__go-home-suggestion");
        goHomeSuggestion.setText(getTranslation(ERROR_404_GO_HOME_KEY));

        // Navigation buttons
        Button goHomeButton = ButtonHelper.createButton(
                getTranslation(MAIN_GO_HOME_KEY), e -> navigateToHome(), ButtonVariant.LUMO_PRIMARY);

        Button goBackButton =
                ButtonHelper.createButton(getTranslation("common.back"), e -> goBack(), ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(goHomeButton, goBackButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        errorContainer.add(errorCode, errorTitle, errorDescription, errorSuggestion, goHomeSuggestion, buttonLayout);
        add(errorContainer);
    }

    /**
     * Navigates to the home page.
     */
    private void navigateToHome() {
        LOGGER.debug("Navigating to home page from RouteNotFoundError");
        NavigationHelper.navigateToHome();
    }

    /**
     * Navigates back to the previous page.
     */
    private void goBack() {
        LOGGER.debug("Going back from RouteNotFoundError");
        getUI().ifPresent(ui -> ui.getPage().getHistory().back());
    }
}
