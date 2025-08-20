package org.apolenkov.application.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.apolenkov.application.views.home.DeckCardViewModel;

/** Reusable deck card component for deck listing. */
public class DeckCard extends Div {

    private final transient DeckCardViewModel viewModel;

    public DeckCard(DeckCardViewModel viewModel) {
        this.viewModel = viewModel;

        add(buildContent());
        addClickListener(e -> navigateToDeck());
    }

    private Component buildContent() {
        VerticalLayout cardContent = new VerticalLayout();

        cardContent.setPadding(false);
        cardContent.setSpacing(false);

        // Add Lumo styling to the card
        getStyle().set("background", "var(--lumo-contrast-5pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        getStyle().set("padding", "var(--lumo-space-m)");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("cursor", "pointer");
        getStyle().set("transition", "all var(--lumo-transition-duration) ease");
        setWidthFull(); // Full width within container
        getStyle().set("max-width", "700px"); // Consistent max width for all cards

        // Hover effect
        getElement()
                .executeJs(
                        """
            this.addEventListener('mouseenter', () => {
                this.style.transform = 'translateY(-2px)';
                this.style.boxShadow = 'var(--lumo-box-shadow-m)';
            });
            this.addEventListener('mouseleave', () => {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = 'none';
            });
        """);

        HorizontalLayout titleLayout = new HorizontalLayout();

        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span icon = new Span(getTranslation("home.deckIcon"));
        icon.getStyle().set("font-size", "var(--lumo-font-size-xl)");
        icon.getStyle().set("color", "var(--lumo-primary-color)");

        H3 title = new H3(viewModel.title() + " (" + viewModel.deckSize() + ")");
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)");

        titleLayout.add(icon, title);

        Span description = new Span(viewModel.description());
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        description.getStyle().set("font-size", "var(--lumo-font-size-s)");
        description.getStyle().set("margin", "var(--lumo-space-s) 0");
        description.getStyle().set("line-height", "1.4");

        HorizontalLayout progressLayout = buildProgress();

        Button practiceButton = new Button(getTranslation("home.practice"));
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> navigateToPractice());
        practiceButton.getStyle().set("margin-top", "var(--lumo-space-s)");

        cardContent.add(titleLayout, description, progressLayout, practiceButton);
        return cardContent;
    }

    private HorizontalLayout buildProgress() {
        HorizontalLayout layout = new HorizontalLayout();

        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidthFull();

        int deckSize = viewModel.deckSize();
        int known = viewModel.knownCount();
        int percent = viewModel.progressPercent();

        Span progressLabel = new Span(getTranslation("home.progress"));
        progressLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        progressLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.clamp(percent / 100.0, 0.0, 1.0));
        progressBar.getStyle().set("flex-grow", "1");

        Span progressText = new Span(percent + getTranslation("home.percentSuffix"));
        progressText.getStyle().set("color", "var(--lumo-primary-color)");
        progressText.getStyle().set("font-weight", "bold");
        progressText.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Span progressDetails = new Span(getTranslation("home.progress.details", known, deckSize));
        progressDetails.getStyle().set("color", "var(--lumo-secondary-text-color)");
        progressDetails.getStyle().set("font-size", "var(--lumo-font-size-s)");

        layout.add(progressLabel, progressBar, progressText, progressDetails);
        return layout;
    }

    private void navigateToDeck() {
        if (viewModel.id() != null) {
            getUI().ifPresent(ui -> ui.navigate("deck/" + viewModel.id()));
        }
    }

    private void navigateToPractice() {
        if (viewModel.id() != null) {
            getUI().ifPresent(ui -> ui.navigate(
                    org.apolenkov.application.views.PracticeView.class,
                    viewModel.id().toString()));
        }
    }
}
