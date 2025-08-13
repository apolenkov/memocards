package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private final org.apolenkov.application.views.components.LanguageSwitcher languageSwitcher;

    public PublicLayout(org.apolenkov.application.views.components.LanguageSwitcher languageSwitcher) {
        this.languageSwitcher = languageSwitcher;
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
    }

    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");

        Button homeBtn = new Button(getTranslation("main.home"), e -> getUI().ifPresent(ui -> ui.navigate("")));

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
        right.add(languageSwitcher);

        bar.add(homeBtn, right);
        addToNavbar(true, bar);
    }
}
