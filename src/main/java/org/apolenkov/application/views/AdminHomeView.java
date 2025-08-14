package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin")
@RolesAllowed("ADMIN")
public class AdminHomeView extends VerticalLayout {
    public AdminHomeView() {
        add(new H2("Admin dashboard"));
    }
}
