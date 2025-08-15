package org.apolenkov.application.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.presenter.PracticePresenter;

@Route(value = "practice", layout = PublicLayout.class)
@RolesAllowed("ROLE_USER")
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    @SuppressWarnings("unused")
    private final DeckUseCase deckUseCase; // kept for navigation/context, may be removed later

    private final FlashcardUseCase flashcardUseCase;
    private final PracticePresenter presenter;
    private Deck currentDeck;
    private PracticePresenter.Session session;

    @SuppressWarnings("unused")
    private boolean sessionStarted = false; // kept for UI state, may be used by future features

    private boolean autoStartAttempted = false;
    private boolean noCardsNotified = false;

    // session stats
    private int correctCount = 0;
    private int hardCount = 0;
    private int totalViewed = 0;

    private PracticeDirection sessionDirection = PracticeDirection.FRONT_TO_BACK;

    // UI Components
    private H2 deckTitle;
    private Div progressSection;
    private Div cardContainer;
    private Div cardContent;
    private HorizontalLayout actionButtons;
    private Button showAnswerButton;
    private Button knowButton;
    private Button hardButton;
    private Span statsSpan;

    public PracticeView(DeckUseCase deckUseCase, FlashcardUseCase flashcardUseCase, PracticePresenter presenter) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.presenter = presenter;

        getContent().setWidth("100%");
        getContent().addClassName("practice-view");

        createHeader();
        createProgressSection();
        createCardContainer();
        createActionButtons();
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
            Notification.show(getTranslation("practice.invalidId"), 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("error"));
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
            Notification.show(getTranslation("practice.allKnown"), 3000, Notification.Position.MIDDLE);
        }
    }

    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("practice-view__header");

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.addClassName("practice-view__header-left");

        Button backButton = new Button(getTranslation("practice.backToDeck"), VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> {
            if (currentDeck != null) {
                getUI().ifPresent(
                                ui -> ui.navigate("deck/" + currentDeck.getId().toString()));
            } else {
                getUI().ifPresent(ui -> ui.navigate("error"));
            }
        });

        deckTitle = new H2(getTranslation("practice.title"));
        deckTitle.addClassName("practice-view__title");

        leftSection.add(backButton, deckTitle);

        headerLayout.add(leftSection);
        getContent().add(headerLayout);
    }

    private void createProgressSection() {
        progressSection = new Div();
        progressSection.addClassName("practice-view__progress");

        statsSpan = new Span(getTranslation("practice.getReady"));
        progressSection.add(statsSpan);

        getContent().add(progressSection);
    }

    private void createCardContainer() {
        cardContainer = new Div();
        cardContainer.addClassName("practice-view__card-container");

        cardContent = new Div();
        cardContent.addClassName("practice-view__card-content");

        cardContent.add(new Span(getTranslation("practice.loadingCards")));
        cardContainer.add(cardContent);

        getContent().add(cardContainer);
    }

    private void createActionButtons() {
        actionButtons = new HorizontalLayout();
        actionButtons.addClassName("practice-view__actions");

        showAnswerButton = new Button(getTranslation("practice.showAnswer"), VaadinIcon.EYE.create());
        showAnswerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        showAnswerButton.addClickListener(e -> showAnswer());

        knowButton = new Button(getTranslation("practice.know"), VaadinIcon.CHECK.create());
        knowButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        knowButton.addClickListener(e -> markLabeled("know"));
        knowButton.setVisible(false);

        hardButton = new Button(getTranslation("practice.hard"), VaadinIcon.WARNING.create());
        hardButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
        hardButton.addClickListener(e -> markLabeled("hard"));
        hardButton.setVisible(false);

        actionButtons.add(showAnswerButton, knowButton, hardButton);
        getContent().add(actionButtons);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        deckOpt.ifPresentOrElse(
                deck -> {
                    currentDeck = deck;
                    deckTitle.setText(getTranslation("practice.header", currentDeck.getTitle()));
                },
                () -> {
                    Notification.show(getTranslation("deck.notFound"), 3000, Notification.Position.MIDDLE);
                    getUI().ifPresent(ui -> ui.navigate("error"));
                });
    }

    @SuppressWarnings("unused")
    private void showPracticeSetupDialog() {
        Dialog setupDialog = new Dialog();
        setupDialog.setWidth("420px");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation("practice.setup.title")));

        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel(getTranslation("practice.setup.count"));
        countSelect.setItems(5, 10, 15, 20, 25);
        countSelect.setValue(10);

        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel(getTranslation("practice.setup.mode"));
        String randomText = getTranslation("practice.setup.mode.random");
        String seqText = getTranslation("practice.setup.mode.seq");
        modeGroup.setItems(randomText, seqText);
        modeGroup.setValue(randomText);

        RadioButtonGroup<String> directionGroup = new RadioButtonGroup<>();
        directionGroup.setLabel(getTranslation("practice.setup.direction"));
        String f2bText = getTranslation("practice.setup.direction.f2b");
        String b2fText = getTranslation("practice.setup.direction.b2f");
        directionGroup.setItems(f2bText, b2fText);
        directionGroup.setValue(f2bText);

        HorizontalLayout buttons = new HorizontalLayout();

        Button startButton = new Button(getTranslation("practice.setup.start"), VaadinIcon.PLAY.create());
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startButton.addClickListener(e -> {
            int count = countSelect.getValue();
            boolean random = randomText.equals(modeGroup.getValue());
            startPractice(count, random);
            setupDialog.close();
        });

        Button cancelButton = new Button(getTranslation("practice.setup.cancel"));
        cancelButton.addClickListener(e -> setupDialog.close());

        buttons.add(startButton, cancelButton);
        layout.add(countSelect, modeGroup, directionGroup, buttons);

        setupDialog.add(layout);
        setupDialog.open();
    }

    private void startPractice(int count, boolean random) {
        if (currentDeck == null) return;
        List<Flashcard> filtered = presenter.getNotKnownCards(currentDeck.getId());
        if (filtered.isEmpty()) {
            showNoCardsOnce();
            return;
        }
        session = presenter.startSession(currentDeck.getId(), count, random, sessionDirection);
        sessionStarted = true;
        correctCount = 0;
        hardCount = 0;
        totalViewed = 0;
        noCardsNotified = false;
        showCurrentCard();
    }

    private void updateProgress() {
        if (session == null || session.cards == null || session.cards.isEmpty()) return;
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
        question.addClassName("practice-view__question");
        Span transcription = new Span(" ");
        transcription.addClassName("practice-view__transcription");

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
        question.addClassName("practice-view__question");
        Hr divider = new Hr();
        divider.addClassName("practice-view__divider");
        H1 answer = new H1(Optional.ofNullable(getAnswerText(currentCard)).orElse(""));

        cardLayout.add(question, divider, answer);
        if (currentCard.getExample() != null && !currentCard.getExample().isBlank()) {
            Span exampleText = new Span(getTranslation("practice.example.prefix", currentCard.getExample()));
            exampleText.addClassName("practice-view__example");
            cardLayout.add(exampleText);
        }
        cardContent.add(cardLayout);

        showAnswerButton.setVisible(false);
        knowButton.setVisible(true);
        hardButton.setVisible(true);
    }

    private void markLabeled(String label) {
        if (session == null) return;
        if (!session.showingAnswer) return;
        if ("know".equals(label)) presenter.markKnow(session);
        else presenter.markHard(session);
        totalViewed = session.totalViewed;
        correctCount = session.correctCount;
        hardCount = session.hardCount;
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

        int total = (session != null && session.cards != null) ? session.cards.size() : totalViewed;
        H1 completionTitle = new H1(getTranslation("practice.sessionComplete", currentDeck.getTitle()));
        H3 results = new H3(getTranslation("practice.results", correctCount, total, hardCount));
        Span timeInfo = new Span(getTranslation(
                "practice.time",
                Math.max(
                        1,
                        java.time.Duration.between(session.sessionStart, java.time.Instant.now())
                                .toMinutes()),
                Math.round((session.totalAnswerDelayMs / Math.max(1.0, (double) totalViewed)) / 1000.0)));

        completionLayout.add(completionTitle, results, timeInfo);
        cardContent.add(completionLayout);

        actionButtons.removeAll();
        Button againButton = new Button(getTranslation("practice.repeatHard"), e -> {
            List<Flashcard> failed = flashcardUseCase.getFlashcardsByDeckId(currentDeck.getId()).stream()
                    .filter(fc -> session.failedCardIds.contains(fc.getId()))
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
            Collections.shuffle(session.cards);
            sessionStarted = true;
            correctCount = 0;
            hardCount = 0;
            totalViewed = 0;
            showCurrentCard();
        });
        Button backToDeckButton = new Button(getTranslation("practice.backToDeck"), e -> getUI().ifPresent(
                        ui -> ui.navigate("deck/" + currentDeck.getId().toString())));
        Button homeButton =
                new Button(getTranslation("practice.backToDecks"), e -> getUI().ifPresent(ui -> ui.navigate("decks")));
        actionButtons.add(againButton, backToDeckButton, homeButton);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("practice.title");
    }
}
