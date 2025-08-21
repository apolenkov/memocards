package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import org.apolenkov.application.service.PracticeSettingsService;

/** Settings dialog extracted from MainLayout. */
public class PracticeSettingsDialog extends Dialog {

    public PracticeSettingsDialog(PracticeSettingsService practiceSettingsService) {
        addClassName("dialog-md");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(new H3(getTranslation("settings.title")));

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
        dirGroup.setValue(
                practiceSettingsService.getDefaultDirection()
                                == org.apolenkov.application.model.PracticeDirection.FRONT_TO_BACK
                        ? f2b
                        : b2f);

        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button(getTranslation("settings.save"), e -> {
            practiceSettingsService.setDefaultCount(countSelect.getValue());
            practiceSettingsService.setDefaultRandomOrder(modeGroup.getValue().equals(random));
            practiceSettingsService.setDefaultDirection(
                    dirGroup.getValue().equals(f2b)
                            ? org.apolenkov.application.model.PracticeDirection.FRONT_TO_BACK
                            : org.apolenkov.application.model.PracticeDirection.BACK_TO_FRONT);
            Notification n =
                    Notification.show(getTranslation("settings" + ".saved"), 3000, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        });
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        buttons.add(save, cancel);

        layout.add(countSelect, modeGroup, dirGroup, buttons);
        add(layout);
    }
}
