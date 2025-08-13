package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("settings")
@AnonymousAllowed
public class SettingsView extends VerticalLayout implements HasDynamicTitle {
    public SettingsView() {
        setWidth("100%");
        add(new H2(getTranslation("settings.page.underConstruction")));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("settings.page.title");
    }
}
