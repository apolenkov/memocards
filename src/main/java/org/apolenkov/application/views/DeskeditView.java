package org.apolenkov.application.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Desk edit")
@Route("desk/id/1")
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@PermitAll
public class DeskeditView extends Composite<VerticalLayout> {

    public DeskeditView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        H1 h1 = new H1();
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        Button buttonPrimary = new Button();
        TextField textField = new TextField();
        TextField textField2 = new TextField();
        ComboBox comboBox = new ComboBox();
        ComboBox comboBox2 = new ComboBox();
        VerticalLayout layoutColumn4 = new VerticalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow2.addClassName(Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.setHeight("50px");
        h1.setText("Edit desk");
        h1.setWidth("max-content");
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutColumn3.getStyle().set("flex-grow", "1");
        layoutColumn2.setWidth("100%");
        layoutColumn2.getStyle().set("flex-grow", "1");
        buttonPrimary.setText("Button");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        textField.setLabel("Name");
        textField.setWidth("min-content");
        textField2.setLabel("Descrition");
        textField2.setWidth("min-content");
        comboBox.setLabel("Source language");
        comboBox.setWidth("min-content");
        setComboBoxSampleData(comboBox);
        comboBox2.setLabel("Target language");
        comboBox2.setWidth("min-content");
        setComboBoxSampleData(comboBox2);
        layoutColumn4.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(layoutRow2);
        layoutRow2.add(h1);
        getContent().add(layoutRow3);
        layoutRow3.add(layoutColumn3);
        layoutRow3.add(layoutColumn2);
        layoutColumn2.add(buttonPrimary);
        layoutColumn2.add(textField);
        layoutColumn2.add(textField2);
        layoutColumn2.add(comboBox);
        layoutColumn2.add(comboBox2);
        layoutRow3.add(layoutColumn4);
    }

    record SampleItem(String value, String label, Boolean disabled) {
    }

    private void setComboBoxSampleData(ComboBox comboBox) {
        List<SampleItem> sampleItems = new ArrayList<>();
        sampleItems.add(new SampleItem("first", "First", null));
        sampleItems.add(new SampleItem("second", "Second", null));
        sampleItems.add(new SampleItem("third", "Third", Boolean.TRUE));
        sampleItems.add(new SampleItem("fourth", "Fourth", null));
        comboBox.setItems(sampleItems);
        comboBox.setItemLabelGenerator(item -> ((SampleItem) item).label());
    }
}
