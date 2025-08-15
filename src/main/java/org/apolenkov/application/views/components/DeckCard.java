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

    private final DeckCardViewModel viewModel;

    public DeckCard(DeckCardViewModel viewModel) {
        this.viewModel = viewModel;
        addClassName("deck-card");
        add(buildContent());
        addClickListener(e -> navigateToDeck());
    }

    private Component buildContent() {
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.addClassName("deck-card__content");
        cardContent.setPadding(false);
        cardContent.setSpacing(false);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.addClassName("deck-card__title");
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span icon = new Span(getTranslation("home.deckIcon"));
        icon.addClassName("deck-card__icon");

        H3 title = new H3(viewModel.title() + " (" + viewModel.deckSize() + ")");

        titleLayout.add(icon, title);

        Span description = new Span(viewModel.description());
        description.addClassName("deck-card__description");

        HorizontalLayout progressLayout = buildProgress();

        Button practiceButton = new Button(getTranslation("home.practice"));
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> navigateToPractice());

        cardContent.add(titleLayout, description, progressLayout, practiceButton);
        return cardContent;
    }

    private HorizontalLayout buildProgress() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName("deck-card__progress");
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setWidth("100%");

        int deckSize = viewModel.deckSize();
        int known = viewModel.knownCount();
        int percent = viewModel.progressPercent();

        Span progressLabel = new Span(getTranslation("home.progress"));
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.min(1.0, Math.max(0.0, percent / 100.0)));
        Span progressText = new Span(percent + getTranslation("home.percentSuffix"));
        Span progressDetails = new Span(getTranslation("home.progress.details", known, deckSize));
        progressDetails.addClassName("deck-card__progress-details");

        layout.add(progressLabel, progressBar, progressText, progressDetails);
        return layout;
    }

    private void navigateToDeck() {
        if (viewModel.id() != null) {
            getUI().ifPresent(ui -> ui.navigate("deck/" + viewModel.id().toString()));
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
