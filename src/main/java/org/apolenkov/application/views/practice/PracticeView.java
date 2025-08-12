package org.apolenkov.application.views.practice;

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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.FlashcardService;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Практика")
@Route("practice")
@AnonymousAllowed
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final FlashcardService flashcardService;
    private final StatsService statsService;
    private final PracticeSettingsService practiceSettingsService;
    private Deck currentDeck;
    private List<Flashcard> practiceCards;
    private int currentCardIndex = 0;
    private boolean showingAnswer = false;
    private boolean sessionStarted = false;

    // session stats
    private int correctCount = 0;
    private int repeatCount = 0;
    private int hardCount = 0;
    private int totalViewed = 0;
    private Instant sessionStart;
    private Instant cardShowTime;
    private long totalAnswerDelayMs = 0L;
    private final List<Long> knownCardIdsDelta = new ArrayList<>();
    private final List<Long> failedCardIds = new ArrayList<>();
    
    // UI Components
    private H2 deckTitle;
    private Div progressSection;
    private Div cardContainer;
    private Div cardContent;
    private HorizontalLayout actionButtons;
    private Button showAnswerButton;
    private Button knowButton;
    private Button repeatButton;
    private Button hardButton;
    private Span statsSpan;

    public PracticeView(FlashcardService flashcardService, StatsService statsService, PracticeSettingsService practiceSettingsService) {
        this.flashcardService = flashcardService;
        this.statsService = statsService;
        this.practiceSettingsService = practiceSettingsService;
        
        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        
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
            if (currentDeck != null && !sessionStarted) {
                startDefaultPractice();
            }
        } catch (NumberFormatException e) {
            Notification.show("Неверный ID колоды", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (currentDeck != null && !sessionStarted) {
            startDefaultPractice();
        }
    }

    private void startDefaultPractice() {
        List<Flashcard> all = flashcardService.getFlashcardsByDeckId(currentDeck.getId());
        int notKnown = (int) all.stream().filter(fc -> !statsService.isCardKnown(currentDeck.getId(), fc.getId())).count();
        int configured = practiceSettingsService.getDefaultCount();
        int defaultCount = Math.max(1, Math.min(configured, notKnown > 0 ? notKnown : all.size()));
        boolean random = practiceSettingsService.isDefaultRandomOrder();
        startPractice(defaultCount, random);
    }

    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Button backButton = new Button("← Колода", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> {
            if (currentDeck != null) {
                getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.deskview.DeskviewView.class, currentDeck.getId().toString()));
            } else {
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        });
        
        deckTitle = new H2("Практика");
        deckTitle.getStyle().set("margin-left", "var(--lumo-space-m)");
        
        leftSection.add(backButton, deckTitle);
        
        headerLayout.add(leftSection);
        getContent().add(headerLayout);
    }

    private void createProgressSection() {
        progressSection = new Div();
        progressSection.setWidth("100%");
        progressSection.getStyle()
            .set("text-align", "center")
            .set("padding", "var(--lumo-space-m)")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("margin-bottom", "var(--lumo-space-l)");
        
        statsSpan = new Span("Готовимся к практике...");
        progressSection.add(statsSpan);
        
        getContent().add(progressSection);
    }

    private void createCardContainer() {
        cardContainer = new Div();
        cardContainer.setWidth("100%");
        cardContainer.setMaxWidth("720px");
        cardContainer.getStyle()
            .set("border", "2px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("margin", "var(--lumo-space-l) auto")
            .set("background", "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-s)")
            .set("min-height", "320px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");
        
        cardContent = new Div();
        cardContent.setWidth("100%");
        cardContent.getStyle()
            .set("text-align", "center")
            .set("font-size", "var(--lumo-font-size-xl)");
        
        cardContent.add(new Span("Загрузка карточек..."));
        cardContainer.add(cardContent);
        
        getContent().add(cardContainer);
    }

    private void createActionButtons() {
        actionButtons = new HorizontalLayout();
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        actionButtons.setSpacing(true);
        actionButtons.setWidth("100%");
        
        showAnswerButton = new Button("Показать ответ", VaadinIcon.EYE.create());
        showAnswerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        showAnswerButton.addClickListener(e -> showAnswer());
        
        knowButton = new Button("Знаю", VaadinIcon.CHECK.create());
        knowButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        knowButton.addClickListener(e -> { markLabeled("know"); });
        knowButton.setVisible(false);

        repeatButton = new Button("Повторить", VaadinIcon.REFRESH.create());
        repeatButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        repeatButton.addClickListener(e -> { markLabeled("repeat"); });
        repeatButton.setVisible(false);

        hardButton = new Button("Сложно", VaadinIcon.WARNING.create());
        hardButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
        hardButton.addClickListener(e -> { markLabeled("hard"); });
        hardButton.setVisible(false);
        
        actionButtons.add(showAnswerButton, knowButton, repeatButton, hardButton);
        getContent().add(actionButtons);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = flashcardService.getDeckById(deckId);
        deckOpt.ifPresentOrElse(deck -> {
            currentDeck = deck;
            deckTitle.setText("Практика: " + currentDeck.getTitle());
        }, () -> {
            Notification.show("Колода не найдена", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        });
    }

    private void showPracticeSetupDialog() {
        Dialog setupDialog = new Dialog();
        setupDialog.setWidth("420px");
        
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Настройки практики"));
        
        Select<Integer> countSelect = new Select<>();
        countSelect.setLabel("Количество карточек");
        countSelect.setItems(5, 10, 15, 20, 25);
        countSelect.setValue(10);
        
        RadioButtonGroup<String> modeGroup = new RadioButtonGroup<>();
        modeGroup.setLabel("Режим показа");
        modeGroup.setItems("Случайный порядок", "По порядку");
        modeGroup.setValue("Случайный порядок");
        
        RadioButtonGroup<String> directionGroup = new RadioButtonGroup<>();
        directionGroup.setLabel("Направление");
        directionGroup.setItems("Лицевая → Обратная", "Обратная → Лицевая");
        directionGroup.setValue("Лицевая → Обратная");
        
        HorizontalLayout buttons = new HorizontalLayout();
        
        Button startButton = new Button("Начать практику", VaadinIcon.PLAY.create());
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startButton.addClickListener(e -> {
            int count = countSelect.getValue();
            boolean random = "Случайный порядок".equals(modeGroup.getValue());
            startPractice(count, random);
            setupDialog.close();
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.addClickListener(e -> setupDialog.close());
        
        buttons.add(startButton, cancelButton);
        layout.add(countSelect, modeGroup, directionGroup, buttons);
        
        setupDialog.add(layout);
        setupDialog.open();
    }

    private void startPractice(int count, boolean random) {
        if (currentDeck == null) return;
        List<Flashcard> all = flashcardService.getFlashcardsByDeckId(currentDeck.getId());
        Set<Long> known = statsService.getKnownCardIds(currentDeck.getId());
        List<Flashcard> filtered = all.stream().filter(fc -> !known.contains(fc.getId())).collect(Collectors.toList());
        if (filtered.isEmpty()) {
            Notification.show("Все карточки выучены — нечего практиковать", 3000, Notification.Position.MIDDLE);
            return;
        }
        practiceCards = random ? new ArrayList<>(filtered) : filtered;
        if (random) java.util.Collections.shuffle(practiceCards);
        if (count < practiceCards.size()) practiceCards = practiceCards.subList(0, count);
        
        sessionStarted = true;
        currentCardIndex = 0;
        showingAnswer = false;
        correctCount = 0;
        repeatCount = 0;
        hardCount = 0;
        totalViewed = 0;
        totalAnswerDelayMs = 0L;
        knownCardIdsDelta.clear();
        failedCardIds.clear();
        sessionStart = Instant.now();
        updateProgress();
        showCurrentCard();
    }

    private void updateProgress() {
        if (practiceCards == null || practiceCards.isEmpty()) return;
        int current = Math.min(currentCardIndex + 1, practiceCards.size());
        int total = practiceCards.size();
        int percent = (int) Math.round((current * 100.0) / Math.max(1, total));
        statsSpan.setText(String.format("Карточка %d из %d | Просмотрено: %d | Правильных: %d | Повтор: %d | Сложно: %d | Прогресс: %d%%",
                current, total, totalViewed, correctCount, repeatCount, hardCount, percent));
    }

    private void showCurrentCard() {
        if (practiceCards == null || practiceCards.isEmpty() || currentCardIndex >= practiceCards.size()) {
            showPracticeComplete();
            return;
        }
        Flashcard currentCard = practiceCards.get(currentCardIndex);
        showingAnswer = false;
        cardShowTime = Instant.now();
        cardContent.removeAll();
        
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        
        H1 frontText = new H1(currentCard.getFrontText());
        frontText.getStyle().set("margin", "0");
        Span transcription = new Span(" ");
        transcription.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        cardLayout.add(frontText, transcription);
        cardContent.add(cardLayout);
        
        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        repeatButton.setVisible(false);
        hardButton.setVisible(false);
    }

    private void showAnswer() {
        if (practiceCards == null || currentCardIndex >= practiceCards.size()) return;
        Flashcard currentCard = practiceCards.get(currentCardIndex);
        showingAnswer = true;
        long delay = Duration.between(cardShowTime, Instant.now()).toMillis();
        totalAnswerDelayMs += Math.max(delay, 0);
        
        cardContent.removeAll();
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        
        H2 frontText = new H2(currentCard.getFrontText());
        Hr divider = new Hr();
        divider.getStyle().set("width", "60%");
        H1 backText = new H1(currentCard.getBackText());
        
        cardLayout.add(frontText, divider, backText);
        if (currentCard.getExample() != null && !currentCard.getExample().isBlank()) {
            Span exampleText = new Span("Пример: " + currentCard.getExample());
            exampleText.getStyle().set("color", "var(--lumo-secondary-text-color)");
            cardLayout.add(exampleText);
        }
        cardContent.add(cardLayout);
        
        showAnswerButton.setVisible(false);
        knowButton.setVisible(true);
        repeatButton.setVisible(true);
        hardButton.setVisible(true);
    }

    private void markLabeled(String label) {
        if (!showingAnswer) return;
        totalViewed++;
        Flashcard currentCard = practiceCards.get(currentCardIndex);
        switch (label) {
            case "know" -> {
                correctCount++;
                knownCardIdsDelta.add(currentCard.getId());
            }
            case "repeat" -> {
                repeatCount++;
                failedCardIds.add(currentCard.getId());
            }
            case "hard" -> {
                hardCount++;
                failedCardIds.add(currentCard.getId());
            }
        }
        updateProgress();
        knowButton.setVisible(false);
        repeatButton.setVisible(false);
        hardButton.setVisible(false);
        nextCard();
    }

    private void nextCard() {
        currentCardIndex++;
        if (currentCardIndex >= practiceCards.size()) {
            showPracticeComplete();
        } else {
            showCurrentCard();
        }
    }

    private void showPracticeComplete() {
        Duration sessionDuration = Duration.between(sessionStart, Instant.now());
        statsService.recordSession(
                currentDeck.getId(),
                totalViewed,
                correctCount,
                repeatCount,
                hardCount,
                sessionDuration,
                totalAnswerDelayMs,
                knownCardIdsDelta
        );
        
        cardContent.removeAll();
        VerticalLayout completionLayout = new VerticalLayout();
        completionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        completionLayout.setSpacing(true);
        
        int total = practiceCards != null ? practiceCards.size() : totalViewed;
        H1 completionTitle = new H1("Сессия завершена — Колода: " + currentDeck.getTitle());
        H3 results = new H3(String.format("Правильно: %d/%d    На повтор: %d", correctCount, total, repeatCount));
        Span timeInfo = new Span(String.format("Время: %d мин    Средняя задержка: %d сек/кар",
                Math.max(1, sessionDuration.toMinutes()), Math.round((totalAnswerDelayMs / Math.max(1.0, totalViewed)) / 1000.0)));
        
        completionLayout.add(completionTitle, results, timeInfo);
        cardContent.add(completionLayout);
        
        actionButtons.removeAll();
        Button againButton = new Button("Повторить ошибочные", e -> {
            List<Flashcard> all = flashcardService.getFlashcardsByDeckId(currentDeck.getId());
            Set<Long> known = statsService.getKnownCardIds(currentDeck.getId());
            List<Flashcard> failed = all.stream()
                    .filter(fc -> failedCardIds.contains(fc.getId()))
                    .filter(fc -> !known.contains(fc.getId()))
                    .collect(Collectors.toList());
            if (failed.isEmpty()) {
                Notification.show("Нет ошибочных для повтора — стартуем по невыученным", 2500, Notification.Position.MIDDLE);
                startDefaultPractice();
                return;
            }
            practiceCards = new ArrayList<>(failed);
            Collections.shuffle(practiceCards);
            sessionStarted = true;
            currentCardIndex = 0;
            showingAnswer = false;
            correctCount = 0;
            repeatCount = 0;
            hardCount = 0;
            totalViewed = 0;
            totalAnswerDelayMs = 0L;
            knownCardIdsDelta.clear();
            failedCardIds.clear();
            sessionStart = Instant.now();
            updateProgress();
            showCurrentCard();
        });
        Button backToDeckButton = new Button("К колоде", e ->
                getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.deskview.DeskviewView.class, currentDeck.getId().toString())));
        Button homeButton = new Button("К колодам", e -> getUI().ifPresent(ui -> ui.navigate("")));
        actionButtons.add(againButton, backToDeckButton, homeButton);
    }
}
