package org.apolenkov.application.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;
import org.apolenkov.application.config.RouteConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.presenter.practice.PracticePresenter;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.NotificationHelper;

@Route(value = "practice", layout = PublicLayout.class)
@RolesAllowed("ROLE_USER")
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    // Constants for duplicated literals
    private static final String PRACTICE_TITLE_KEY = "practice.title";

    private final transient FlashcardUseCase flashcardUseCase;
    private final transient PracticePresenter presenter;
    private transient Deck currentDeck;
    private transient PracticePresenter.Session session;

    private boolean autoStartAttempted = false;
    private boolean noCardsNotified = false;

    // session stats
    private int correctCount = 0;
    private int hardCount = 0;
    private int totalViewed = 0;

    private PracticeDirection sessionDirection = PracticeDirection.FRONT_TO_BACK;

    // UI Components
    private H2 deckTitle;
    private Div cardContent;
    private HorizontalLayout actionButtons;
    private Button showAnswerButton;
    private Button knowButton;
    private Button hardButton;
    private Span statsSpan;

    public PracticeView(FlashcardUseCase flashcardUseCase, PracticePresenter presenter) {
        this.flashcardUseCase = flashcardUseCase;
        this.presenter = presenter;

        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        // Create a container with consistent width
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.setMaxWidth("800px"); // Consistent max width
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        createHeader(contentContainer);
        createProgressSection(contentContainer);
        createCardContainer(contentContainer);
        createActionButtons(contentContainer);

        getContent().add(contentContainer);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            Long deckId = Long.parseLong(parameter);
            loadDeck(deckId);
            if (currentDeck != null && !autoStartAttempted) {
                autoStartAttempted = true;
                startDefaultPractice();
            }
        } catch (NumberFormatException e) {
            NotificationHelper.showError(getTranslation("practice.invalidId"));
            NavigationHelper.navigateToError(RouteConstants.DECKS_ROUTE);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (currentDeck != null && !autoStartAttempted) {
            autoStartAttempted = true;
            startDefaultPractice();
        }
    }

    private void startDefaultPractice() {
        List<Flashcard> notKnownCards = presenter.getNotKnownCards(currentDeck.getId());
        if (notKnownCards.isEmpty()) {
            showNoCardsOnce();
            return;
        }
        int defaultCount = presenter.resolveDefaultCount(currentDeck.getId());
        boolean random = presenter.isRandom();
        sessionDirection = presenter.defaultDirection();
        startPractice(defaultCount, random);
    }

    private void showNoCardsOnce() {
        if (!noCardsNotified) {
            noCardsNotified = true;
            NotificationHelper.showInfo(getTranslation("practice.allKnown"));
        }
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = ButtonHelper.createBackButton(e -> {
            if (currentDeck != null) {
                NavigationHelper.navigateToDeck(currentDeck.getId());
            } else {
                NavigationHelper.navigateToError(RouteConstants.DECKS_ROUTE);
            }
        });
        backButton.setText(getTranslation("practice.back"));

        deckTitle = new H2(getTranslation(PRACTICE_TITLE_KEY));
        deckTitle.addClassName("practice-view__deck-title");

        leftSection.add(backButton, deckTitle);

        headerLayout.add(leftSection);
        container.add(headerLayout);
    }

    private void createProgressSection(VerticalLayout container) {
        Div progressSection = new Div();
        progressSection.addClassName("practice-progress");

        statsSpan = new Span(getTranslation("practice.getReady"));
        statsSpan.addClassName("practice-progress__text");
        progressSection.add(statsSpan);

        container.add(progressSection);
    }

    private void createCardContainer(VerticalLayout container) {
        Div cardContainer = new Div();
        cardContainer.addClassName("practice-card-container");

        cardContent = new Div();
        cardContent.addClassName("practice-card-content");

        cardContent.add(new Span(getTranslation("practice.loadingCards")));
        cardContainer.add(cardContent);

        container.add(cardContainer);
    }

    private void createActionButtons(VerticalLayout container) {
        actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        showAnswerButton = ButtonHelper.createLargeButton(getTranslation("practice.showAnswer"), e -> showAnswer());
        showAnswerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        knowButton = ButtonHelper.createLargeButton(getTranslation("practice.know"), e -> markLabeled("know"));
        knowButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        knowButton.setVisible(false);

        hardButton = ButtonHelper.createLargeButton(getTranslation("practice.hard"), e -> markLabeled("hard"));
        hardButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        hardButton.setVisible(false);

        actionButtons.add(showAnswerButton, knowButton, hardButton);
        container.add(actionButtons);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        deckOpt.ifPresentOrElse(
                deck -> {
                    currentDeck = deck;
                    deckTitle.setText(getTranslation(PRACTICE_TITLE_KEY, currentDeck.getTitle()));
                },
                () -> {
                    NotificationHelper.showError(getTranslation("deck.notFound"));
                    NavigationHelper.navigateToError(RouteConstants.DECKS_ROUTE);
                });
    }

    private void startPractice(int count, boolean random) {
        if (currentDeck == null) return;
        List<Flashcard> filtered = presenter.getNotKnownCards(currentDeck.getId());
        if (filtered.isEmpty()) {
            showNoCardsOnce();
            return;
        }
        session = presenter.startSession(currentDeck.getId(), count, random, sessionDirection);
        correctCount = 0;
        hardCount = 0;
        totalViewed = 0;
        noCardsNotified = false;
        showCurrentCard();
    }

    private void updateProgress() {
        if (session == null || session.getCards() == null || session.getCards().isEmpty()) return;
        var p = presenter.progress(session);
        statsSpan.setText(getTranslation(
                "practice.progressLine", p.current(), p.total(), p.totalViewed(), p.correct(), p.hard(), p.percent()));
    }

    private String getQuestionText(Flashcard card) {
        return sessionDirection == PracticeDirection.BACK_TO_FRONT ? card.getBackText() : card.getFrontText();
    }

    private String getAnswerText(Flashcard card) {
        return sessionDirection == PracticeDirection.BACK_TO_FRONT ? card.getFrontText() : card.getBackText();
    }

    private void showCurrentCard() {
        if (session == null || presenter.isComplete(session)) {
            showPracticeComplete();
            return;
        }
        updateProgress();
        Flashcard currentCard = presenter.currentCard(session);
        presenter.startQuestion(session);
        cardContent.removeAll();

        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);

        H1 question = new H1(Optional.ofNullable(getQuestionText(currentCard)).orElse(""));

        Span transcription = new Span(" ");

        cardLayout.add(question, transcription);
        cardContent.add(cardLayout);

        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    private void showAnswer() {
        if (session == null || presenter.isComplete(session)) return;
        Flashcard currentCard = presenter.currentCard(session);
        presenter.reveal(session);

        cardContent.removeAll();
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);

        H2 question = new H2(Optional.ofNullable(getQuestionText(currentCard)).orElse(""));

        Hr divider = new Hr();

        H1 answer = new H1(Optional.ofNullable(getAnswerText(currentCard)).orElse(""));

        cardLayout.add(question, divider, answer);
        if (currentCard.getExample() != null && !currentCard.getExample().isBlank()) {
            Span exampleText = new Span(getTranslation("practice.example.prefix", currentCard.getExample()));

            cardLayout.add(exampleText);
        }
        cardContent.add(cardLayout);

        showAnswerButton.setVisible(false);
        knowButton.setVisible(true);
        hardButton.setVisible(true);
    }

    private void markLabeled(String label) {
        if (session == null) return;
        if (!session.isShowingAnswer()) return;
        if ("know".equals(label)) presenter.markKnow(session);
        else presenter.markHard(session);
        totalViewed = session.getTotalViewed();
        correctCount = session.getCorrectCount();
        hardCount = session.getHardCount();
        knowButton.setVisible(false);
        hardButton.setVisible(false);
        nextCard();
    }

    private void nextCard() {
        if (presenter.isComplete(session)) {
            showPracticeComplete();
        } else {
            showCurrentCard();
        }
    }

    private void showSessionButtons() {
        actionButtons.removeAll();
        actionButtons.add(showAnswerButton, knowButton, hardButton);
        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    private void showPracticeComplete() {
        presenter.recordAndPersist(session);

        cardContent.removeAll();
        VerticalLayout completionLayout = new VerticalLayout();
        completionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        completionLayout.setSpacing(true);

        int total = (session != null && session.getCards() != null)
                ? session.getCards().size()
                : totalViewed;
        H1 completionTitle = new H1(getTranslation("practice.sessionComplete", currentDeck.getTitle()));
        H3 results = new H3(getTranslation("practice.results", correctCount, total, hardCount));
        long minutes = java.time.Duration.between(session.getSessionStart(), java.time.Instant.now())
                .toMinutes();
        minutes = Math.clamp(minutes, 1, Integer.MAX_VALUE);
        double denom = Math.clamp(totalViewed, 1.0, Double.MAX_VALUE);
        long avgSec = Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0);
        Span timeInfo = new Span(getTranslation("practice.time", minutes, avgSec));

        completionLayout.add(completionTitle, results, timeInfo);
        cardContent.add(completionLayout);

        actionButtons.removeAll();
        Button againButton = new Button(getTranslation("practice.repeatHard"), e -> {
            List<Flashcard> failed = flashcardUseCase.getFlashcardsByDeckId(currentDeck.getId()).stream()
                    .filter(fc -> session.getFailedCardIds().contains(fc.getId()))
                    .filter(fc -> presenter.getNotKnownCards(currentDeck.getId()).stream()
                            .map(Flashcard::getId)
                            .collect(Collectors.toSet())
                            .contains(fc.getId()))
                    .toList();
            showSessionButtons();
            if (failed.isEmpty()) {
                startDefaultPractice();
                return;
            }
            session = new PracticePresenter.Session(
                    currentDeck.getId(), new ArrayList<>(failed), presenter.defaultDirection());
            Collections.shuffle(session.getCards());
            correctCount = 0;
            hardCount = 0;
            totalViewed = 0;
            showCurrentCard();
        });
        Button backToDeckButton = new Button(getTranslation("practice.backToDeck"), e -> getUI().ifPresent(
                        ui -> ui.navigate("deck/" + currentDeck.getId().toString())));
        Button homeButton = new Button(getTranslation("practice.backToDecks"), e -> NavigationHelper.navigateToDecks());
        actionButtons.add(againButton, backToDeckButton, homeButton);
    }

    @Override
    public String getPageTitle() {
        return getTranslation(PRACTICE_TITLE_KEY);
    }
}
