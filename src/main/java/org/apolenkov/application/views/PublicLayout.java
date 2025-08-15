package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.TopMenu;

@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;

    public PublicLayout(LanguageSwitcher languageSwitcher, TopMenu topMenu) {
        this.languageSwitcher = languageSwitcher;
        this.topMenu = topMenu;
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
    }

    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
        right.add(languageSwitcher);

        bar.add(topMenu, right);
        addToNavbar(true, bar);
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        if (getContent() != null) {
            getContent().addClassName("app-content");
        }
        // Refresh header menu (greeting, buttons) after route changes, including login/logout
        if (topMenu != null) {
            topMenu.refreshMenu();
        }
    }
}
