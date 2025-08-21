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

        // Styling via theme CSS classes
        addClassName("deck-card");
        setWidthFull();

        HorizontalLayout titleLayout = new HorizontalLayout();

        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span icon = new Span(getTranslation("home.deckIcon"));
        icon.addClassName("deck-card__icon");

        H3 title = new H3(viewModel.title() + " (" + viewModel.deckSize() + ")");
        title.addClassName("deck-card__title");

        titleLayout.add(icon, title);

        Span description = new Span(viewModel.description());
        description.addClassName("deck-card__description");

        HorizontalLayout progressLayout = buildProgress();

        Button practiceButton = new Button(getTranslation("home.practice"));
        practiceButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        practiceButton.addClickListener(e -> navigateToPractice());
        practiceButton.addClassName("deck-card__practice-button");

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
        progressLabel.addClassName("deck-card__progress-label");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(Math.clamp(percent / 100.0, 0.0, 1.0));
        progressBar.setWidthFull();
        layout.setFlexGrow(1, progressBar);

        Span progressText = new Span(percent + getTranslation("home.percentSuffix"));
        progressText.addClassName("deck-card__progress-text");

        Span progressDetails = new Span(getTranslation("home.progress.details", known, deckSize));
        progressDetails.addClassName("deck-card__progress-details");

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
