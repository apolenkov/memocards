package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.views.components.LanguageSwitcher;
import org.apolenkov.application.views.components.TopMenu;

@AnonymousAllowed
public class PublicLayout extends AppLayout {

    private static final String FLOATING_SHAPE_CLASS = "floating-shape";
    private static final String GLOW_ORB_CLASS = "glow-orb";
    private static final String ANIMATED_LINE_CLASS = "animated-line";
    private static final String WC_WORD_CLASS = "wc-word";

    private final LanguageSwitcher languageSwitcher;
    private final TopMenu topMenu;
    private Div wordCloud; // shown only on landing
    private Div ufoEl;
    private Div rocketEl;
    private Div cosmonautEl;

    public PublicLayout(LanguageSwitcher languageSwitcher, TopMenu topMenu) {
        this.languageSwitcher = languageSwitcher;
        this.topMenu = topMenu;
        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
        addAnimatedBackground();
    }

    private void addHeaderContent() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.addClassName("main-layout__navbar");
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
        right.setAlignItems(Alignment.CENTER);
        right.add(languageSwitcher);

        bar.add(topMenu, right);
        addToNavbar(true, bar);

        // Ensure proper spacing and positioning
        getElement().getStyle().set("margin-top", "0");
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

        // Show word cloud only on landing view for readability on other pages
        boolean isLanding = getContent() instanceof LandingView;
        if (wordCloud != null) {
            wordCloud.setVisible(isLanding);
        }
        // Disable animations on non-landing pages to avoid distraction during study
        toggleBackgroundAnimation(!isLanding);
    }

    private void toggleBackgroundAnimation(boolean staticMode) {
        if (ufoEl != null) ufoEl.getElement().getClassList().set("bg-static", staticMode);
        if (rocketEl != null) rocketEl.getElement().getClassList().set("bg-static", staticMode);
        if (cosmonautEl != null) cosmonautEl.getElement().getClassList().set("bg-static", staticMode);
    }

    private void addAnimatedBackground() {
        // Root fixed layer (see animated-background.css)
        Div background = new Div();
        background.addClassName("animated-background");

        // Decorative layers
        Div grid = new Div();
        grid.addClassName("grid-pattern");
        background.add(grid);

        Div shape1 = new Div();
        shape1.addClassNames(FLOATING_SHAPE_CLASS, "floating-shape--1");
        Div shape2 = new Div();
        shape2.addClassNames(FLOATING_SHAPE_CLASS, "floating-shape--2");
        Div shape3 = new Div();
        shape3.addClassNames(FLOATING_SHAPE_CLASS, "floating-shape--3");
        Div shape4 = new Div();
        shape4.addClassNames(FLOATING_SHAPE_CLASS, "floating-shape--4");
        Div shape5 = new Div();
        shape5.addClassNames(FLOATING_SHAPE_CLASS, "floating-shape--5");
        background.add(shape1, shape2, shape3, shape4, shape5);

        Div orb1 = new Div();
        orb1.addClassNames(GLOW_ORB_CLASS, "glow-orb--1");
        Div orb2 = new Div();
        orb2.addClassNames(GLOW_ORB_CLASS, "glow-orb--2");
        Div orb3 = new Div();
        orb3.addClassNames(GLOW_ORB_CLASS, "glow-orb--3");
        background.add(orb1, orb2, orb3);

        Div line1 = new Div();
        line1.addClassNames(ANIMATED_LINE_CLASS, "animated-line--1");
        Div line2 = new Div();
        line2.addClassNames(ANIMATED_LINE_CLASS, "animated-line--2");
        Div line3 = new Div();
        line3.addClassNames(ANIMATED_LINE_CLASS, "animated-line--3");
        background.add(line1, line2, line3);

        // Left illustration (image defined in CSS)
        Div illustrationLeft = new Div();
        illustrationLeft.addClassName("app-illustration-left");
        background.add(illustrationLeft);

        // Minimal floating icons (UFO and Rocket)
        ufoEl = new Div();
        ufoEl.addClassName("floating-ufo");
        ufoEl.getElement().setAttribute("aria-hidden", "true");

        rocketEl = new Div();
        rocketEl.addClassName("floating-rocket");
        rocketEl.getElement().setAttribute("aria-hidden", "true");

        // Large right-side cosmonaut accent
        cosmonautEl = new Div();
        cosmonautEl.addClassName("floating-cosmonaut");
        cosmonautEl.getElement().setAttribute("aria-hidden", "true");

        background.add(ufoEl, rocketEl, cosmonautEl);

        // Word cloud disabled per new design; keep placeholder for future use

        // Attach to AppLayout host element (no JS)
        getElement().appendChild(background.getElement());
    }
}
