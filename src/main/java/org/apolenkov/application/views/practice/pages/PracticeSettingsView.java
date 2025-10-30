package org.apolenkov.application.views.practice.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * View for configuring practice session settings.
 * Allows users to set default preferences for practice sessions including
 * card count, order mode (random/sequential), and direction (front-to-back/back-to-front).
 */
@Route(value = RouteConstants.SETTINGS_ROUTE, layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class PracticeSettingsView extends BaseView {

    // Dependencies
    private final transient PracticeSettingsService practiceSettingsService;
    private final transient PracticeSettingsComponents settingsComponents;

    /**
     * Creates a new practice settings view.
     *
     * @param service service for managing practice settings
     * @param components components factory for creating settings UI elements
     */
    public PracticeSettingsView(final PracticeSettingsService service, final PracticeSettingsComponents components) {
        this.practiceSettingsService = service;
        this.settingsComponents = components;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setupLayout();
        addSettingsComponents();
    }

    /**
     * Sets up the basic layout properties.
     */
    private void setupLayout() {
        setPadding(true);
        setSpacing(true);
        add(new H3(getTranslation(PracticeConstants.SETTINGS_TITLE_KEY)));
    }

    /**
     * Adds all settings components to the view.
     */
    private void addSettingsComponents() {
        Select<Integer> countSelect = settingsComponents.createCountSelect(practiceSettingsService);
        RadioButtonGroup<String> modeGroup = settingsComponents.createModeGroup(practiceSettingsService);
        RadioButtonGroup<String> dirGroup = settingsComponents.createDirectionGroup(practiceSettingsService);
        HorizontalLayout actions = createActionButtons(countSelect, modeGroup, dirGroup);

        add(countSelect, modeGroup, dirGroup, actions);
    }

    /**
     * Creates the action buttons layout with save and cancel buttons.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @return configured action buttons layout
     */
    private HorizontalLayout createActionButtons(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        Button save = createSaveButton(countSelect, modeGroup, dirGroup);
        Button cancel = createCancelButton();

        return new HorizontalLayout(save, cancel);
    }

    /**
     * Creates the save button with settings persistence logic.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @return configured save button
     */
    private Button createSaveButton(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.SETTINGS_SAVE_KEY),
                e -> saveSettings(countSelect, modeGroup, dirGroup),
                ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the cancel button.
     *
     * @return configured cancel button
     */
    private Button createCancelButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_CANCEL_KEY),
                e -> NavigationHelper.navigateToDecks(),
                ButtonVariant.LUMO_TERTIARY);
    }

    /**
     * Saves all selected settings to the service and shows success notification.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     */
    private void saveSettings(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup) {

        settingsComponents.saveSettings(practiceSettingsService, countSelect, modeGroup, dirGroup);
        NotificationHelper.showSuccess(getTranslation(PracticeConstants.SETTINGS_SAVED_KEY));
    }

    /**
     * Gets the page title for the practice settings view.
     *
     * @return the localized settings title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(PracticeConstants.SETTINGS_TITLE_KEY);
    }
}
