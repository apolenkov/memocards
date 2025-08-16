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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.views.components.DeckEditDialog;
import org.apolenkov.application.views.presenter.DeckPresenter;

@Route(value = "deck", layout = PublicLayout.class)
@RolesAllowed("ROLE_USER")
public class DeckView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    private final DeckFacade deckFacade;
    private final DeckPresenter presenter;
    private Deck currentDeck;
    private Grid<Flashcard> flashcardGrid;
    private H2 deckTitle;
    private Span deckDescription;
    private Span deckStats;
    private TextField flashcardSearchField;
    private ListDataProvider<Flashcard> flashcardsDataProvider;
    private Checkbox hideKnownCheckbox;

    public DeckView(DeckUseCase deckUseCase, DeckPresenter presenter, DeckFacade deckFacade) {
        this.presenter = presenter;
        this.deckFacade = deckFacade;

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
    public String getPageTitle() {
        return getTranslation("deck.cards");
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            Long deckId = Long.parseLong(parameter);
            loadDeck(deckId);
        } catch (NumberFormatException e) {
            Notification.show(getTranslation("deck.invalidId"), 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("error", QueryParameters.of("from", "decks")));
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
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("decks")));

        deckTitle = new H2(getTranslation("deck.loading"));
        deckTitle.addClassName("deckview-view__header-title");

        deckStats = new Span();
        deckStats.addClassName("deckview-view__header-stats");

        leftSection.add(backButton, deckTitle, deckStats);
        headerLayout.add(leftSection);

        getContent().add(headerLayout);
    }

    private void createDeckInfo() {
        Div infoSection = new Div();
        infoSection.addClassName("deckview-view__info");

        deckDescription = new Span(getTranslation("deck.description.loading"));
        deckDescription.addClassName("deckview-view__info-description");

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
                getUI().ifPresent(ui ->
                        ui.navigate(PracticeView.class, currentDeck.getId().toString()));
            }
        });

        Button addFlashcardButton = new Button(getTranslation("deck.addCard"), VaadinIcon.PLUS.create());
        addFlashcardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addFlashcardButton.getElement().setAttribute("data-testid", "deck-add-card");
        addFlashcardButton.addClickListener(e -> openFlashcardDialog(null));

        Button editDeckButton = new Button(VaadinIcon.EDIT.create());
        editDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editDeckButton.getElement().setProperty("title", getTranslation("deck.edit.tooltip"));
        editDeckButton.addClickListener(e -> {
            if (currentDeck != null) {
                new DeckEditDialog(deckFacade, currentDeck, updated -> updateDeckInfo()).open();
            }
        });

        actionsLayout.add(practiceButton, addFlashcardButton, editDeckButton);
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
                presenter.resetProgress(currentDeck.getId());
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

        flashcardGrid
                .addColumn(Flashcard::getFrontText)
                .setHeader(getTranslation("deck.col.front"))
                .setFlexGrow(2);

        flashcardGrid
                .addColumn(Flashcard::getBackText)
                .setHeader(getTranslation("deck.col.back"))
                .setFlexGrow(2);

        flashcardGrid
                .addColumn(flashcard -> {
                    String example = flashcard.getExample();
                    return example != null && !example.trim().isEmpty() ? example : "—";
                })
                .setHeader(getTranslation("deck.col.example"))
                .setFlexGrow(3);

        flashcardGrid
                .addComponentColumn(flashcard -> {
                    boolean known = currentDeck != null && presenter.isKnown(currentDeck.getId(), flashcard.getId());
                    Span mark = new Span(known ? getTranslation("deck.knownMark") : "");
                    if (known) {
                        mark.addClassName("deckview-view__status-known");
                    }
                    return mark;
                })
                .setHeader(getTranslation("deck.col.status"))
                .setFlexGrow(0)
                .setWidth("130px");

        flashcardGrid
                .addComponentColumn(flashcard -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);

                    Button editButton = new Button(VaadinIcon.EDIT.create());
                    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                    editButton.addClickListener(e -> openFlashcardDialog(flashcard));

                    Button toggleKnown = new Button(VaadinIcon.CHECK.create());
                    toggleKnown.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                    toggleKnown.getElement().setProperty("title", getTranslation("deck.toggleKnown.tooltip"));
                    toggleKnown.addClickListener(e -> {
                        presenter.toggleKnown(currentDeck.getId(), flashcard.getId());
                        flashcardGrid.getDataProvider().refreshAll();
                        applyFlashcardsFilter();
                    });

                    Button deleteButton = new Button(VaadinIcon.TRASH.create());
                    deleteButton.addThemeVariants(
                            ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
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
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            updateDeckInfo();
            loadFlashcards();
        } else {
            Notification.show(getTranslation("deck.notFound"), 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("error", QueryParameters.of("from", "decks")));
        }
    }

    private void updateDeckInfo() {
        if (currentDeck != null) {
            deckTitle.setText(currentDeck.getTitle());
            deckStats.setText(getTranslation("deck.count", presenter.deckSize(currentDeck.getId())));
            deckDescription.setText(
                    java.util.Optional.ofNullable(currentDeck.getDescription()).orElse(""));
        }
    }

    private void loadFlashcards() {
        if (currentDeck != null) {
            List<Flashcard> flashcards = presenter.loadFlashcards(currentDeck.getId());
            flashcardsDataProvider = new ListDataProvider<>(new java.util.ArrayList<>(flashcards));
            flashcardGrid.setDataProvider(flashcardsDataProvider);
            applyFlashcardsFilter();
        }
    }

    private void applyFlashcardsFilter() {
        if (flashcardsDataProvider == null || currentDeck == null) return;
        String q = flashcardSearchField != null && flashcardSearchField.getValue() != null
                ? flashcardSearchField.getValue().toLowerCase().trim()
                : "";
        boolean hideKnown = hideKnownCheckbox != null && Boolean.TRUE.equals(hideKnownCheckbox.getValue());
        List<Flashcard> filtered = presenter.listFilteredFlashcards(currentDeck.getId(), q, hideKnown);
        flashcardsDataProvider = new ListDataProvider<>(new java.util.ArrayList<>(filtered));
        flashcardGrid.setDataProvider(flashcardsDataProvider);
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

        BeanValidationBinder<Flashcard> binder = new BeanValidationBinder<>(Flashcard.class);
        Flashcard bean = flashcard != null ? flashcard : new Flashcard();
        binder.forField(frontTextField)
                .asRequired(getTranslation("dialog.fillRequired"))
                .bind(Flashcard::getFrontText, Flashcard::setFrontText);
        binder.forField(backTextField)
                .asRequired(getTranslation("dialog.fillRequired"))
                .bind(Flashcard::getBackText, Flashcard::setBackText);
        binder.forField(exampleArea).bind(Flashcard::getExample, Flashcard::setExample);
        binder.forField(imageUrlField).bind(Flashcard::getImageUrl, Flashcard::setImageUrl);

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
            try {
                bean.setDeckId(currentDeck.getId());
                binder.writeBean(bean);
                deckFacade.saveFlashcard(bean);
                loadFlashcards();
                updateDeckInfo();
                dialog.close();
                Notification.show(
                        flashcard == null ? getTranslation("deck.card.added") : getTranslation("deck.card.updated"),
                        2000,
                        Notification.Position.BOTTOM_START);
            } catch (ValidationException vex) {
                Notification.show(getTranslation("dialog.fillRequired"), 3000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addClickListener(e -> dialog.close());

        buttonsLayout.add(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(
                new H3(
                        flashcard == null
                                ? getTranslation("deck.card.addTitle")
                                : getTranslation("deck.card.editTitle")),
                formLayout,
                buttonsLayout);

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
            deckFacade.deleteFlashcard(flashcard.getId());
            loadFlashcards();
            updateDeckInfo();
            confirmDialog.close();
            Notification.show(getTranslation("deck.card.deleted"), 2000, Notification.Position.BOTTOM_START);
        });

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }
}
