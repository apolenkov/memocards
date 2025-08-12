package org.apolenkov.application.views.deskview;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Desk view")
@Route("desk-view")
@Menu(order = 2, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@PermitAll
public class DeskviewView extends Composite<VerticalLayout> {

    public DeskviewView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        H1 h1 = new H1();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        Button buttonPrimary = new Button();
        Anchor link = new Anchor();
        Anchor link2 = new Anchor();
        Anchor link3 = new Anchor();
        VerticalLayout layoutColumn4 = new VerticalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("50px");
        h1.setText("Name desk");
        h1.setWidth("max-content");
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutColumn3.getStyle().set("flex-grow", "1");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        layoutRow4.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow4);
        layoutRow4.addClassName(Gap.MEDIUM);
        layoutRow4.setWidth("100%");
        layoutRow4.setHeight("min-content");
        buttonPrimary.setText("Add");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        link.setText("1. word — [transcription] — translation");
        link.setHref("#");
        link.setWidth("100%");
        link2.setText("2. word — [transcription] — translation");
        link2.setHref("#");
        link2.setWidth("100%");
        link3.setText("3. word — [transcription] — translation");
        link3.setHref("#");
        link3.setWidth("100%");
        layoutColumn4.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(h1);
        getContent().add(layoutRow3);
        layoutRow3.add(layoutColumn3);
        layoutRow3.add(layoutColumn2);
        layoutColumn2.add(layoutRow4);
        layoutRow4.add(buttonPrimary);
        layoutColumn2.add(link);
        layoutColumn2.add(link2);
        layoutColumn2.add(link3);
        layoutRow3.add(layoutColumn4);
    }
}
