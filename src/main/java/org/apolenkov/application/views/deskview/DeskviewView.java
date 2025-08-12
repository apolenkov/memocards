package org.apolenkov.application.views.deskview;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.FlashcardService;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;
import java.util.Optional;

@PageTitle("Просмотр колоды")
@Route("decks")
@AnonymousAllowed
public class DeskviewView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final FlashcardService flashcardService;
    private Deck currentDeck;
    private Grid<Flashcard> flashcardGrid;
    private H2 deckTitle;
    private Span deckDescription;
    private Span deckStats;
    private TextField flashcardSearchField;
    private ListDataProvider<Flashcard> flashcardsDataProvider;

    public DeskviewView(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
        
        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        
        createHeader();
        createDeckInfo();
        createActions();
        createFlashcardsGrid();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            Long deckId = Long.parseLong(parameter);
            loadDeck(deckId);
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
        
        Button backButton = new Button("← Колоды", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));
        
        deckTitle = new H2("Загрузка...");
        deckTitle.getStyle().set("margin-left", "var(--lumo-space-m)");
        
        deckStats = new Span();
        deckStats.getStyle()
            .set("margin-left", "var(--lumo-space-s)")
            .set("color", "var(--lumo-secondary-text-color)");
        
        leftSection.add(backButton, deckTitle, deckStats);
        headerLayout.add(leftSection);
        
        getContent().add(headerLayout);
    }

    private void createDeckInfo() {
        Div infoSection = new Div();
        infoSection.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("margin-bottom", "var(--lumo-space-m)");
        
        deckDescription = new Span("Загрузка описания...");
        deckDescription.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-style", "italic");
        
        infoSection.add(deckDescription);
        getContent().add(infoSection);
    }

    private void createActions() {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidth("100%");
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        HorizontalLayout leftActions = new HorizontalLayout();
        
        Button practiceButton = new Button("▶ Начать сессию", VaadinIcon.PLAY.create());
        practiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        practiceButton.addClickListener(e -> {
            if (currentDeck != null) {
                getUI().ifPresent(ui -> ui.navigate(org.apolenkov.application.views.practice.PracticeView.class, currentDeck.getId().toString()));
            }
        });
        
        Button addFlashcardButton = new Button("✚ Добавить карточку", VaadinIcon.PLUS.create());
        addFlashcardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addFlashcardButton.addClickListener(e -> openFlashcardDialog(null));
        
        leftActions.add(practiceButton, addFlashcardButton);
        actionsLayout.add(leftActions);
        
        getContent().add(actionsLayout);
    }

    private void createFlashcardsGrid() {
        H3 flashcardsTitle = new H3("Карточки");
        getContent().add(flashcardsTitle);
        
        // search bar
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidth("100%");
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        flashcardSearchField = new TextField();
        flashcardSearchField.setPlaceholder("Поиск по карточкам...");
        flashcardSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        flashcardSearchField.setClearButtonVisible(true);
        flashcardSearchField.setValueChangeMode(ValueChangeMode.EAGER);
        flashcardSearchField.addValueChangeListener(e -> applyFlashcardsFilter());
        
        searchRow.add(flashcardSearchField);
        getContent().add(searchRow);
        
        flashcardGrid = new Grid<>(Flashcard.class, false);
        flashcardGrid.setWidth("100%");
        flashcardGrid.setHeight("400px");
        
        flashcardGrid.addColumn(Flashcard::getFrontText)
            .setHeader("Лицевая сторона")
            .setFlexGrow(2);
        
        flashcardGrid.addColumn(Flashcard::getBackText)
            .setHeader("Обратная сторона")
            .setFlexGrow(2);
        
        flashcardGrid.addColumn(flashcard -> {
            String example = flashcard.getExample();
            return example != null && !example.trim().isEmpty() ? example : "—";
        })
            .setHeader("Пример")
            .setFlexGrow(3);
        
        flashcardGrid.addComponentColumn(flashcard -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openFlashcardDialog(flashcard));
            
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteFlashcard(flashcard));
            
            actions.add(editButton, deleteButton);
            return actions;
        })
            .setHeader("Действия")
            .setWidth("120px")
            .setFlexGrow(0);
        
        getContent().add(flashcardGrid);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = flashcardService.getDeckById(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            updateDeckInfo();
            loadFlashcards();
        } else {
            Notification.show("Колода не найдена", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    private void updateDeckInfo() {
        if (currentDeck != null) {
            deckTitle.setText(currentDeck.getTitle());
            deckStats.setText("(" + currentDeck.getFlashcardCount() + " карточек)");
            deckDescription.setText(currentDeck.getDescription());
        }
    }

    private void loadFlashcards() {
        if (currentDeck != null) {
            List<Flashcard> flashcards = flashcardService.getFlashcardsByDeckId(currentDeck.getId());
            flashcardsDataProvider = new ListDataProvider<>(flashcards);
            flashcardGrid.setDataProvider(flashcardsDataProvider);
            applyFlashcardsFilter();
        }
    }

    private void applyFlashcardsFilter() {
        if (flashcardsDataProvider == null) return;
        String q = flashcardSearchField != null && flashcardSearchField.getValue() != null
                ? flashcardSearchField.getValue().toLowerCase().trim() : "";
        flashcardsDataProvider.clearFilters();
        if (!q.isEmpty()) {
            flashcardsDataProvider.addFilter(fc -> {
                String front = fc.getFrontText() != null ? fc.getFrontText().toLowerCase() : "";
                String back = fc.getBackText() != null ? fc.getBackText().toLowerCase() : "";
                String ex = fc.getExample() != null ? fc.getExample().toLowerCase() : "";
                return front.contains(q) || back.contains(q) || ex.contains(q);
            });
        }
    }

    private void openFlashcardDialog(Flashcard flashcard) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        
        FormLayout formLayout = new FormLayout();
        
        TextField frontTextField = new TextField("Лицевая сторона");
        frontTextField.setWidth("100%");
        frontTextField.setRequired(true);
        
        TextField backTextField = new TextField("Обратная сторона");
        backTextField.setWidth("100%");
        backTextField.setRequired(true);
        
        TextArea exampleArea = new TextArea("Пример (опционально)");
        exampleArea.setWidth("100%");
        exampleArea.setMaxHeight("100px");
        
        TextField imageUrlField = new TextField("URL изображения (опционально)");
        imageUrlField.setWidth("100%");
        
        if (flashcard != null) {
            frontTextField.setValue(flashcard.getFrontText() != null ? flashcard.getFrontText() : "");
            backTextField.setValue(flashcard.getBackText() != null ? flashcard.getBackText() : "");
            exampleArea.setValue(flashcard.getExample() != null ? flashcard.getExample() : "");
            imageUrlField.setValue(flashcard.getImageUrl() != null ? flashcard.getImageUrl() : "");
        }
        
        formLayout.add(frontTextField, backTextField, exampleArea, imageUrlField);
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        
        Button saveButton = new Button("Сохранить", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (frontTextField.isEmpty() || backTextField.isEmpty()) {
                Notification.show("Заполните обязательные поля", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            Flashcard cardToSave = flashcard != null ? flashcard : new Flashcard();
            cardToSave.setDeckId(currentDeck.getId());
            cardToSave.setFrontText(frontTextField.getValue().trim());
            cardToSave.setBackText(backTextField.getValue().trim());
            cardToSave.setExample(exampleArea.getValue().trim());
            cardToSave.setImageUrl(imageUrlField.getValue().trim());
            
            flashcardService.saveFlashcard(cardToSave);
            loadFlashcards();
            updateDeckInfo();
            dialog.close();
            
            Notification.show(flashcard == null ? "Карточка добавлена" : "Карточка обновлена", 
                2000, Notification.Position.BOTTOM_START);
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.addClickListener(e -> dialog.close());
        
        buttonsLayout.add(saveButton, cancelButton);
        
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(
            new H3(flashcard == null ? "Добавить карточку" : "Редактировать карточку"),
            formLayout,
            buttonsLayout
        );
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteFlashcard(Flashcard flashcard) {
        Dialog confirmDialog = new Dialog();
        
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Подтверждение удаления"));
        layout.add(new Span("Вы уверены, что хотите удалить эту карточку?"));
        
        HorizontalLayout buttons = new HorizontalLayout();
        
        Button confirmButton = new Button("Удалить", VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            flashcardService.deleteFlashcard(flashcard.getId());
            loadFlashcards();
            updateDeckInfo();
            confirmDialog.close();
            Notification.show("Карточка удалена", 2000, Notification.Position.BOTTOM_START);
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.addClickListener(e -> confirmDialog.close());
        
        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);
        
        confirmDialog.add(layout);
        confirmDialog.open();
    }
}