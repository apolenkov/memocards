package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "access-denied", layout = PublicLayout.class)
@PageTitle("Access denied")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout {
    public AccessDeniedView() {
        add(new H2(getTranslation("error.403")));
    }
}
