package org.apolenkov.application.views.settings;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Настройки")
@Route("settings")
@AnonymousAllowed
public class SettingsView extends VerticalLayout {
    public SettingsView() {
        setWidth("100%");
        add(new H2("Настройки (в разработке)"));
    }
}
