package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.HttpStatusCode;

@ParentLayout(PublicLayout.class)
public class NotFoundView extends Div implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        removeAll();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setAlignItems(VerticalLayout.Alignment.CENTER);
        layout.setJustifyContentMode(VerticalLayout.JustifyContentMode.CENTER);

        H2 title = new H2("Page not found");
        Button goHome = new Button("Go to Home", VaadinIcon.HOME.create());
        goHome.addClickListener(e -> event.getUI().navigate("decks"));

        layout.add(title, goHome);
        add(layout);
        return HttpStatusCode.NOT_FOUND.getCode();
    }
}
