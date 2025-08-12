package org.apolenkov.application.views.stats;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Статистика")
@Route("stats")
@AnonymousAllowed
public class StatsView extends VerticalLayout {
    public StatsView() {
        setWidth("100%");
        add(new H2("Статистика (в разработке)"));
    }
}
