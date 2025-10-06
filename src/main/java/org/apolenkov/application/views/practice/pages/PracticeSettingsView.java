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
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
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

    /**
     * Creates a new practice settings view.
     *
     * @param service service for managing practice settings
     */
    public PracticeSettingsView(final PracticeSettingsService service) {
        this.practiceSettingsService = service;
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
        // Get translated texts
        String countLabel = getTranslation(PracticeConstants.SETTINGS_COUNT_KEY);
        String modeLabel = getTranslation(PracticeConstants.SETTINGS_MODE_KEY);
        String randomText = getTranslation(PracticeConstants.SETTINGS_MODE_RANDOM_KEY);
        String sequentialText = getTranslation(PracticeConstants.SETTINGS_MODE_SEQUENTIAL_KEY);
        String directionLabel = getTranslation(PracticeConstants.SETTINGS_DIRECTION_KEY);
        String frontToBackText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_F2B_KEY);
        String backToFrontText = getTranslation(PracticeConstants.SETTINGS_DIRECTION_B2F_KEY);

        Select<Integer> countSelect = PracticeSettingsComponents.createCountSelect(practiceSettingsService, countLabel);
        RadioButtonGroup<String> modeGroup = PracticeSettingsComponents.createModeGroup(
                practiceSettingsService, modeLabel, randomText, sequentialText);
        RadioButtonGroup<String> dirGroup = PracticeSettingsComponents.createDirectionGroup(
                practiceSettingsService, directionLabel, frontToBackText, backToFrontText);
        HorizontalLayout actions = createActionButtons(countSelect, modeGroup, dirGroup, randomText, frontToBackText);

        add(countSelect, modeGroup, dirGroup, actions);
    }

    /**
     * Creates the action buttons layout with save and cancel buttons.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     * @return configured action buttons layout
     */
    private HorizontalLayout createActionButtons(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        Button save = createSaveButton(countSelect, modeGroup, dirGroup, randomText, frontToBackText);
        Button cancel = createCancelButton();

        return new HorizontalLayout(save, cancel);
    }

    /**
     * Creates the save button with settings persistence logic.
     *
     * @param countSelect the count selection component
     * @param modeGroup the mode selection component
     * @param dirGroup the direction selection component
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     * @return configured save button
     */
    private Button createSaveButton(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.SETTINGS_SAVE_KEY),
                e -> saveSettings(countSelect, modeGroup, dirGroup, randomText, frontToBackText),
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
     * @param randomText the translated text for random mode
     * @param frontToBackText the translated text for front to back direction
     */
    private void saveSettings(
            final Select<Integer> countSelect,
            final RadioButtonGroup<String> modeGroup,
            final RadioButtonGroup<String> dirGroup,
            final String randomText,
            final String frontToBackText) {

        PracticeSettingsComponents.saveSettings(
                practiceSettingsService, countSelect, modeGroup, dirGroup, randomText, frontToBackText);
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
