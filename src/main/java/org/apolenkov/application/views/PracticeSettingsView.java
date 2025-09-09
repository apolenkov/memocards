package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.PracticeSettingsService;

/**
 * View for configuring practice session settings.
 * Allows users to set default preferences for practice sessions including
 * card count, order mode (random/sequential), and direction (front-to-back/back-to-front).
 */
@Route(value = "settings", layout = PublicLayout.class)
@RolesAllowed({SecurityConstants.ROLE_USER, SecurityConstants.ROLE_ADMIN})
public class PracticeSettingsView extends VerticalLayout implements HasDynamicTitle {

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
    private void init() {
        setPadding(true);
        setSpacing(true);

        add(new H3(getTranslation("settings.title")));

        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(getTranslation("settings.count"));
        countSelect.setItems(5, 10, 15, 20, 25, 30);
        countSelect.setValue(practiceSettingsService.getDefaultCount());

        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(getTranslation("settings.mode"));
        String random = getTranslation("settings.mode.random");
        String seq = getTranslation("settings.mode.sequential");
        modeGroup.setItems(random, seq);
        modeGroup.setValue(practiceSettingsService.isDefaultRandomOrder() ? random : seq);

        RadioButtonGroup<String> dirGroup = new RadioButtonGroup<>();
        dirGroup.setLabel(getTranslation("settings.direction"));
        String f2b = getTranslation("settings.direction.f2b");
        String b2f = getTranslation("settings.direction.b2f");
        dirGroup.setItems(f2b, b2f);
        dirGroup.setValue(practiceSettingsService.getDefaultDirection() == PracticeDirection.FRONT_TO_BACK ? f2b : b2f);

        Button save = new Button(getTranslation("settings.save"), e -> {
            practiceSettingsService.setDefaultCount(countSelect.getValue());
            practiceSettingsService.setDefaultRandomOrder(modeGroup.getValue().equals(random));
            practiceSettingsService.setDefaultDirection(
                    dirGroup.getValue().equals(f2b)
                            ? PracticeDirection.FRONT_TO_BACK
                            : PracticeDirection.BACK_TO_FRONT);

            Notification n = Notification.show(getTranslation("settings.saved"), 3000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        Button cancel = new Button(getTranslation("common.cancel"));
        cancel.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("decks")));
        HorizontalLayout actions = new HorizontalLayout(save, cancel);

        add(countSelect, modeGroup, dirGroup, actions);
    }

    /**
     * Gets the page title for the practice settings view.
     *
     * @return the localized settings title
     */
    @Override
    public String getPageTitle() {
        return getTranslation("settings.title");
    }
}
