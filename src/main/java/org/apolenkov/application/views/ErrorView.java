package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "error", layout = PublicLayout.class)
@AnonymousAllowed
public class ErrorView extends VerticalLayout implements HasDynamicTitle {

    public ErrorView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2(getTranslation("error.500"));
        Button goHome = new Button(getTranslation("main.gohome"));
        goHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goHome.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        add(title, goHome);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("error.500");
    }
}
