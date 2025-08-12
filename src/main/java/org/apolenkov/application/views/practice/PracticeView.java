package org.apolenkov.application.views.practice;

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
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;
import java.util.Optional;

@PageTitle("Практика")
@Route("practice")
@AnonymousAllowed
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final FlashcardService flashcardService;
    private Deck currentDeck;
    private List<Flashcard> practiceCards;
    private int currentCardIndex = 0;
    private boolean showingAnswer = false;
    private int correctCount = 0;
    private int totalViewed = 0;
    
    // UI Components
    private H2 deckTitle;
    private Div progressSection;
    private Div cardContainer;
    private Div cardContent;
    private HorizontalLayout actionButtons;
    private Button showAnswerButton;
    private Button correctButton;
    private Button incorrectButton;
    private Button nextButton;
    private Span statsSpan;

    public PracticeView(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
        
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
            
            // Show practice setup dialog after loading deck
            if (currentDeck != null) {
                getUI().ifPresent(ui -> ui.access(() -> showPracticeSetupDialog()));
            }
        } catch (NumberFormatException e) {
            Notification.show("Неверный ID колоды", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }



    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Button backButton = new Button("← Назад", VaadinIcon.ARROW_LEFT.create());
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
        
        Button setupButton = new Button("Настройки", VaadinIcon.COG.create());
        setupButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        setupButton.addClickListener(e -> showPracticeSetupDialog());
        
        headerLayout.add(leftSection, setupButton);
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
        cardContainer.setMaxWidth("600px");
        cardContainer.getStyle()
            .set("border", "2px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-xl)")
            .set("margin", "var(--lumo-space-l) auto")
            .set("background", "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-s)")
            .set("min-height", "300px")
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
        
        correctButton = new Button("Правильно", VaadinIcon.CHECK.create());
        correctButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        correctButton.addClickListener(e -> markAnswer(true));
        correctButton.setVisible(false);
        
        incorrectButton = new Button("Неправильно", VaadinIcon.CLOSE.create());
        incorrectButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
        incorrectButton.addClickListener(e -> markAnswer(false));
        incorrectButton.setVisible(false);
        
        nextButton = new Button("Следующая", VaadinIcon.ARROW_RIGHT.create());
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        nextButton.addClickListener(e -> nextCard());
        nextButton.setVisible(false);
        
        actionButtons.add(showAnswerButton, correctButton, incorrectButton, nextButton);
        getContent().add(actionButtons);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = flashcardService.getDeckById(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            deckTitle.setText("Практика: " + currentDeck.getTitle());
        } else {
            Notification.show("Колода не найдена", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    private void showPracticeSetupDialog() {
        Dialog setupDialog = new Dialog();
        setupDialog.setWidth("400px");
        
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
        directionGroup.setItems("Лицевая → Обратная", "Обратная → Лицевая", "Смешанный");
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
        cancelButton.addClickListener(e -> {
            setupDialog.close();
            if (currentDeck != null) {
                getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.deskview.DeskviewView.class, currentDeck.getId().toString()));
            } else {
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        });
        
        buttons.add(startButton, cancelButton);
        layout.add(countSelect, modeGroup, directionGroup, buttons);
        
        setupDialog.add(layout);
        setupDialog.open();
    }

    private void startPractice(int count, boolean random) {
        if (currentDeck == null) return;
        
        practiceCards = flashcardService.getFlashcardsForPractice(currentDeck.getId(), count, random);
        
        if (practiceCards.isEmpty()) {
            Notification.show("В колоде нет карточек для практики", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.deskview.DeskviewView.class, currentDeck.getId().toString()));
            return;
        }
        
        currentCardIndex = 0;
        showingAnswer = false;
        correctCount = 0;
        totalViewed = 0;
        
        updateProgress();
        showCurrentCard();
    }

    private void updateProgress() {
        if (practiceCards == null || practiceCards.isEmpty()) return;
        
        int current = currentCardIndex + 1;
        int total = practiceCards.size();
        double percentage = totalViewed > 0 ? (double) correctCount / totalViewed * 100 : 0;
        
        statsSpan.setText(String.format(
            "Карточка %d из %d | Просмотрено: %d | Правильных: %d (%.0f%%)",
            current, total, totalViewed, correctCount, percentage
        ));
    }

    private void showCurrentCard() {
        if (practiceCards == null || practiceCards.isEmpty() || currentCardIndex >= practiceCards.size()) {
            showPracticeComplete();
            return;
        }
        
        Flashcard currentCard = practiceCards.get(currentCardIndex);
        showingAnswer = false;
        
        cardContent.removeAll();
        
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        
        H1 frontText = new H1(currentCard.getFrontText());
        frontText.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-primary-text-color)")
            .set("text-align", "center");
        
        Span hintText = new Span("Подумайте над ответом, затем нажмите 'Показать ответ'");
        hintText.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-style", "italic");
        
        cardLayout.add(frontText, hintText);
        cardContent.add(cardLayout);
        
        // Update button visibility
        showAnswerButton.setVisible(true);
        correctButton.setVisible(false);
        incorrectButton.setVisible(false);
        nextButton.setVisible(false);
    }

    private void showAnswer() {
        if (practiceCards == null || currentCardIndex >= practiceCards.size()) return;
        
        Flashcard currentCard = practiceCards.get(currentCardIndex);
        showingAnswer = true;
        
        cardContent.removeAll();
        
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        
        H2 frontText = new H2(currentCard.getFrontText());
        frontText.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-secondary-text-color)");
        
        Hr divider = new Hr();
        divider.getStyle().set("width", "50%");
        
        H1 backText = new H1(currentCard.getBackText());
        backText.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-primary-text-color)")
            .set("text-align", "center");
        
        cardLayout.add(frontText, divider, backText);
        
        // Add example if exists
        if (currentCard.getExample() != null && !currentCard.getExample().trim().isEmpty()) {
            Span exampleText = new Span("Пример: " + currentCard.getExample());
            exampleText.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("text-align", "center");
            cardLayout.add(exampleText);
        }
        
        cardContent.add(cardLayout);
        
        // Update button visibility
        showAnswerButton.setVisible(false);
        correctButton.setVisible(true);
        incorrectButton.setVisible(true);
        nextButton.setVisible(false);
    }

    private void markAnswer(boolean correct) {
        if (!showingAnswer) return;
        
        totalViewed++;
        if (correct) {
            correctCount++;
        }
        
        updateProgress();
        
        // Update button visibility
        correctButton.setVisible(false);
        incorrectButton.setVisible(false);
        nextButton.setVisible(true);
        
        // Change next button text if this is the last card
        if (currentCardIndex >= practiceCards.size() - 1) {
            nextButton.setText("Завершить");
            nextButton.setIcon(VaadinIcon.CHECK.create());
        } else {
            nextButton.setText("Следующая");
            nextButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        }
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
        cardContent.removeAll();
        
        VerticalLayout completionLayout = new VerticalLayout();
        completionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        completionLayout.setSpacing(true);
        
        H1 completionTitle = new H1("🎉 Практика завершена!");
        completionTitle.getStyle()
            .set("margin", "0")
            .set("color", "var(--lumo-success-text-color)")
            .set("text-align", "center");
        
        double percentage = totalViewed > 0 ? (double) correctCount / totalViewed * 100 : 0;
        H3 results = new H3(String.format(
            "Результат: %d из %d правильных (%.0f%%)",
            correctCount, totalViewed, percentage
        ));
        results.getStyle().set("text-align", "center");
        
        completionLayout.add(completionTitle, results);
        cardContent.add(completionLayout);
        
        // Update buttons
        actionButtons.removeAll();
        
        Button againButton = new Button("Еще раз", VaadinIcon.REFRESH.create());
        againButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        againButton.addClickListener(e -> showPracticeSetupDialog());
        
        Button backToDeckButton = new Button("К колоде", VaadinIcon.CLIPBOARD.create());
        backToDeckButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.deskview.DeskviewView.class, currentDeck.getId().toString())));
        
        Button homeButton = new Button("На главную", VaadinIcon.HOME.create());
        homeButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        
        actionButtons.add(againButton, backToDeckButton, homeButton);
    }
}
