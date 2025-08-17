package org.apolenkov.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

        HorizontalLayout right = new HorizontalLayout();
        right.addClassName("main-layout__right");
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

        // Right word cloud (texts via i18n)
        wordCloud = new Div();
        wordCloud.addClassName("app-wordcloud-right");
        Span w1 = new Span(getTranslation("bg.word.hello"));
        Span w2 = new Span(getTranslation("bg.word.hola"));
        Span w3 = new Span(getTranslation("bg.word.bonjour"));
        Span w4 = new Span(getTranslation("bg.word.privet"));
        w1.addClassName(WC_WORD_CLASS);
        w2.addClassName(WC_WORD_CLASS);
        w3.addClassName(WC_WORD_CLASS);
        w4.addClassName(WC_WORD_CLASS);
        wordCloud.add(w1, w2, w3, w4);
        background.add(wordCloud);

        // Attach to AppLayout host element (no JS)
        getElement().appendChild(background.getElement());
    }
}