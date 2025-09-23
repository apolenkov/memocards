package org.apolenkov.application.views.presentation.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal component for displaying "Entity Not Found" errors.
 * Can be used for any entity type (decks, cards, users, etc.).
 */
public class EntityNotFoundError extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityNotFoundError.class);

    // CSS classes
    private static final String SURFACE_PANEL_CLASS = "surface-panel";
    private static final String CONTAINER_MD_CLASS = "container-md";
    private static final String ENTITY_ERROR_SECTION_CLASS = "entity-error__section";
    private static final String ENTITY_ERROR_TITLE_CLASS = "entity-error__title";
    private static final String ENTITY_ERROR_DESCRIPTION_CLASS = "entity-error__description";
    private static final String ENTITY_ERROR_SUGGESTION_CLASS = "entity-error__suggestion";

    // Translation keys
    private static final String MAIN_GO_HOME_KEY = "main.gohome";

    private final String entityId;
    private final String backRoute;
    private final String customMessage;
    private final transient Runnable onBackAction;

    /**
     * Creates a new EntityNotFoundError component.
     *
     * @param entityIdParam the ID of the entity that was not found
     * @param backRouteParam the route to navigate back to
     * @param customMessageParam custom error message to display
     * @param onBackActionParam optional custom back action (can be null)
     */
    public EntityNotFoundError(
            final String entityIdParam,
            final String backRouteParam,
            final String customMessageParam,
            final Runnable onBackActionParam) {
        this.entityId = entityIdParam;
        this.backRoute = backRouteParam;
        this.customMessage = customMessageParam;
        this.onBackAction = onBackActionParam;
    }

    /**
     * Initializes the UI after construction.
     * This method should be called after the constructor to avoid this-escape warnings.
     */
    public void initialize() {
        initializeUI();
    }

    /**
     * Creates a new EntityNotFoundError component with default back action.
     *
     * @param entityIdParam the ID of the entity that was not found
     * @param backRouteParam the route to navigate back to
     * @param customMessageParam custom error message to display
     */
    public EntityNotFoundError(
            final String entityIdParam, final String backRouteParam, final String customMessageParam) {
        this(entityIdParam, backRouteParam, customMessageParam, null);
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        LOGGER.debug("Creating EntityNotFoundError for entity with ID: {}", entityId);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        createErrorContent();
    }

    /**
     * Creates the main error content section.
     */
    private void createErrorContent() {
        VerticalLayout errorContainer = new VerticalLayout();
        errorContainer.setSpacing(true);
        errorContainer.setWidthFull();
        errorContainer.addClassName(CONTAINER_MD_CLASS);
        errorContainer.setAlignItems(Alignment.CENTER);

        VerticalLayout errorSection = new VerticalLayout();
        errorSection.setSpacing(true);
        errorSection.setPadding(true);
        errorSection.setWidthFull();
        errorSection.addClassName(ENTITY_ERROR_SECTION_CLASS);
        errorSection.addClassName(SURFACE_PANEL_CLASS);
        errorSection.addClassName(CONTAINER_MD_CLASS);

        // Error title
        H2 errorTitle = new H2(getTranslation("entity.notFound.title"));
        errorTitle.addClassName(ENTITY_ERROR_TITLE_CLASS);

        // Error description
        Paragraph errorDescription = new Paragraph();
        errorDescription.addClassName(ENTITY_ERROR_DESCRIPTION_CLASS);
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            // Show custom message if provided
            errorDescription.setText(customMessage);
        } else {
            // Show default message
            errorDescription.setText(getTranslation("entity.notFound.description", entityId));
        }

        // Error suggestion
        Paragraph errorSuggestion = new Paragraph();
        errorSuggestion.addClassName(ENTITY_ERROR_SUGGESTION_CLASS);
        errorSuggestion.setText(getTranslation("entity.notFound.suggestion"));

        // Navigation buttons
        Button goBackButton = ButtonHelper.createButton(
                getTranslation("entity.notFound.goBack"), e -> handleBackAction(), ButtonVariant.LUMO_PRIMARY);

        Button goHomeButton = ButtonHelper.createButton(
                getTranslation(MAIN_GO_HOME_KEY), e -> navigateToHome(), ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setWidthFull();
        buttonLayout.add(goBackButton, goHomeButton);

        errorSection.add(errorTitle, errorDescription, errorSuggestion, buttonLayout);
        errorContainer.add(errorSection);
        add(errorContainer);
    }

    /**
     * Handles the back action.
     */
    private void handleBackAction() {
        LOGGER.debug("Handling back action for entity error");
        if (onBackAction != null) {
            onBackAction.run();
        } else {
            NavigationHelper.navigateTo(backRoute);
        }
    }

    /**
     * Navigates to the home page.
     */
    private void navigateToHome() {
        LOGGER.debug("Navigating to home page from entity error");
        NavigationHelper.navigateToHome();
    }
}
