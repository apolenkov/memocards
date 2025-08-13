package org.apolenkov.application.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import org.apolenkov.application.service.StatsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Просмотр колоды")
@Route("decks")
@AnonymousAllowed
public class DeckView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    private final FlashcardService flashcardService;
    private final StatsService statsService;
    private Deck currentDeck;
    private Grid<Flashcard> flashcardGrid;
    private H2 deckTitle;
    private Span deckDescription;
    private Span deckStats;
    private TextField flashcardSearchField;
    private ListDataProvider<Flashcard> flashcardsDataProvider;
    private Checkbox hideKnownCheckbox;

    public DeckView(FlashcardService flashcardService, StatsService statsService) {
        this.flashcardService = flashcardService;
        this.statsService = statsService;

        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().addClassName("deckview-view");

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
            Notification.show(getTranslation("deck.invalidId"), 3000, Notification.Position.MIDDLE);
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

        Button backButton = new Button(getTranslation("main.decks"), VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        deckTitle = new H2(getTranslation("deck.loading"));
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

        deckDescription = new Span(getTranslation("deck.description.loading"));
        deckDescription.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-style", "italic");

        infoSection.add(deckDescription);
        getContent().add(infoSection);
    }

    private void createActions() {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidth("100%");
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        Button practiceButton = new Button(getTranslation("deck.startSession"), VaadinIcon.PLAY.create());
        practiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        practiceButton.addClickListener(e -> {
            if (currentDeck != null) {
                getUI().ifPresent(ui -> ui.navigate(PracticeView.class, currentDeck.getId().toString()));
            }
        });

        Button addFlashcardButton = new Button(getTranslation("deck.addCard"), VaadinIcon.PLUS.create());
        addFlashcardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addFlashcardButton.addClickListener(e -> openFlashcardDialog(null));

        actionsLayout.add(practiceButton, addFlashcardButton);
        getContent().add(actionsLayout);
    }

    private void createFlashcardsGrid() {
        H3 flashcardsTitle = new H3(getTranslation("deck.cards"));
        getContent().add(flashcardsTitle);

        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidth("100%");
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        flashcardSearchField = new TextField();
        flashcardSearchField.setPlaceholder(getTranslation("deck.searchCards"));
        flashcardSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        flashcardSearchField.setClearButtonVisible(true);
        flashcardSearchField.setValueChangeMode(ValueChangeMode.EAGER);
        flashcardSearchField.addValueChangeListener(e -> applyFlashcardsFilter());

        HorizontalLayout rightFilters = new HorizontalLayout();
        rightFilters.setAlignItems(FlexComponent.Alignment.CENTER);
        hideKnownCheckbox = new Checkbox(getTranslation("deck.hideKnown"), true);
        hideKnownCheckbox.addValueChangeListener(e -> applyFlashcardsFilter());
        Button resetProgress = new Button(getTranslation("deck.resetProgress"), VaadinIcon.ROTATE_LEFT.create());
        resetProgress.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        resetProgress.addClickListener(e -> {
            if (currentDeck != null) {
                statsService.resetDeckProgress(currentDeck.getId());
                Notification.show(getTranslation("deck.progressReset"), 2000, Notification.Position.BOTTOM_START);
                loadFlashcards();
            }
        });
        rightFilters.add(hideKnownCheckbox, resetProgress);

        searchRow.add(flashcardSearchField, rightFilters);
        getContent().add(searchRow);

        flashcardGrid = new Grid<>(Flashcard.class, false);
        flashcardGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        flashcardGrid.setWidth("100%");
        flashcardGrid.setHeight("420px");

        flashcardGrid.addColumn(Flashcard::getFrontText)
            .setHeader(getTranslation("deck.col.front"))
            .setFlexGrow(2);

        flashcardGrid.addColumn(Flashcard::getBackText)
            .setHeader(getTranslation("deck.col.back"))
            .setFlexGrow(2);

        flashcardGrid.addColumn(flashcard -> {
            String example = flashcard.getExample();
            return example != null && !example.trim().isEmpty() ? example : "—";
        })
            .setHeader(getTranslation("deck.col.example"))
            .setFlexGrow(3);

        flashcardGrid.addComponentColumn(flashcard -> {
            boolean known = currentDeck != null && statsService.isCardKnown(currentDeck.getId(), flashcard.getId());
            Span mark = new Span(known ? getTranslation("deck.knownMark") : "");
            mark.getStyle().set("color", "var(--lumo-success-text-color)");
            return mark;
        }).setHeader(getTranslation("deck.col.status")).setFlexGrow(0).setWidth("130px");

        flashcardGrid.addComponentColumn(flashcard -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openFlashcardDialog(flashcard));

            Button toggleKnown = new Button(VaadinIcon.CHECK.create());
            toggleKnown.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            toggleKnown.getElement().setProperty("title", getTranslation("deck.toggleKnown.tooltip"));
            toggleKnown.addClickListener(e -> {
                boolean known = statsService.isCardKnown(currentDeck.getId(), flashcard.getId());
                statsService.setCardKnown(currentDeck.getId(), flashcard.getId(), !known);
                flashcardGrid.getDataProvider().refreshAll();
                applyFlashcardsFilter();
            });

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteFlashcard(flashcard));

            actions.add(editButton, toggleKnown, deleteButton);
            return actions;
        })
            .setHeader(getTranslation("deck.col.actions"))
            .setWidth("220px")
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
            Notification.show(getTranslation("deck.notFound"), 3000, Notification.Position.MIDDLE);
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
        if (flashcardsDataProvider == null || currentDeck == null) return;
        String q = flashcardSearchField != null && flashcardSearchField.getValue() != null
                ? flashcardSearchField.getValue().toLowerCase().trim() : "";
        Set<Long> knownIds = statsService.getKnownCardIds(currentDeck.getId());
        boolean hideKnown = hideKnownCheckbox != null && Boolean.TRUE.equals(hideKnownCheckbox.getValue());
        flashcardsDataProvider.clearFilters();
        flashcardsDataProvider.addFilter(fc -> {
            boolean matches = (fc.getFrontText() != null && fc.getFrontText().toLowerCase().contains(q))
                    || (fc.getBackText() != null && fc.getBackText().toLowerCase().contains(q))
                    || (fc.getExample() != null && fc.getExample().toLowerCase().contains(q));
            if (!matches) return false;
            if (hideKnown && knownIds.contains(fc.getId())) return false;
            return true;
        });
    }

    private void openFlashcardDialog(Flashcard flashcard) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        TextField frontTextField = new TextField(getTranslation("deck.col.front"));
        frontTextField.setWidth("100%");
        frontTextField.setRequired(true);

        TextField backTextField = new TextField(getTranslation("deck.col.back"));
        backTextField.setWidth("100%");
        backTextField.setRequired(true);

        TextArea exampleArea = new TextArea(getTranslation("deck.example.optional"));
        exampleArea.setWidth("100%");
        exampleArea.setMaxHeight("100px");

        TextField imageUrlField = new TextField(getTranslation("deck.imageUrl.optional"));
        imageUrlField.setWidth("100%");

        if (flashcard != null) {
            frontTextField.setValue(flashcard.getFrontText() != null ? flashcard.getFrontText() : "");
            backTextField.setValue(flashcard.getBackText() != null ? flashcard.getBackText() : "");
            exampleArea.setValue(flashcard.getExample() != null ? flashcard.getExample() : "");
            imageUrlField.setValue(flashcard.getImageUrl() != null ? flashcard.getImageUrl() : "");
        }

        formLayout.add(frontTextField, backTextField, exampleArea, imageUrlField);

        HorizontalLayout buttonsLayout = new HorizontalLayout();

        Button saveButton = new Button(getTranslation("dialog.save"), VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (frontTextField.isEmpty() || backTextField.isEmpty()) {
                Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.MIDDLE);
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

            Notification.show(flashcard == null ? getTranslation("deck.card.added") : getTranslation("deck.card.updated"),
                2000, Notification.Position.BOTTOM_START);
        });

        Button cancelButton = new Button(getTranslation("dialog.cancel"));
        cancelButton.addClickListener(e -> dialog.close());

        buttonsLayout.add(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(
            new H3(flashcard == null ? getTranslation("deck.card.addTitle") : getTranslation("deck.card.editTitle")),
            formLayout,
            buttonsLayout
        );

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteFlashcard(Flashcard flashcard) {
        Dialog confirmDialog = new Dialog();

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation("dialog.delete.confirmTitle")));
        layout.add(new Span(getTranslation("dialog.delete.confirmText")));

        HorizontalLayout buttons = new HorizontalLayout();

        Button confirmButton = new Button(getTranslation("dialog.delete"), VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            flashcardService.deleteFlashcard(flashcard.getId());
            loadFlashcards();
            updateDeckInfo();
            confirmDialog.close();
            Notification.show(getTranslation("deck.card.deleted"), 2000, Notification.Position.BOTTOM_START);
        });

        Button cancelButton = new Button(getTranslation("dialog.cancel"));
        cancelButton.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }
}


