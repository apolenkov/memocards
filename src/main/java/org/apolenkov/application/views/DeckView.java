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
import org.apolenkov.application.config.RouteConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.views.components.DeckEditDialog;
import org.apolenkov.application.views.presenter.DeckPresenter;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "deck", layout = PublicLayout.class)
@RolesAllowed("ROLE_USER")
public class DeckView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    private static final Logger log = LoggerFactory.getLogger(DeckView.class);

    // Constants for duplicated literals
    private static final String FILL_REQUIRED_KEY = "dialog.fillRequired";

    private final transient DeckFacade deckFacade;
    private final transient DeckPresenter presenter;
    private transient Deck currentDeck;
    private Grid<Flashcard> flashcardGrid;
    private H2 deckTitle;
    private Span deckDescription;
    private Span deckStats;
    private TextField flashcardSearchField;
    private ListDataProvider<Flashcard> flashcardsDataProvider;
    private Checkbox hideKnownCheckbox;

    public DeckView(DeckPresenter presenter, DeckFacade deckFacade) {
        this.presenter = presenter;
        this.deckFacade = deckFacade;

        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        // Create a container with consistent width
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.setMaxWidth("900px"); // Consistent max width
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        createHeader(contentContainer);
        createDeckInfo(contentContainer);
        createActions(contentContainer);
        createFlashcardsGrid(contentContainer);

        getContent().add(contentContainer);
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
            NotificationHelper.showError(getTranslation("deck.invalidId"));
            NavigationHelper.navigateToError(RouteConstants.DECKS_ROUTE);
        }
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = ButtonHelper.createBackButton(e -> NavigationHelper.navigateToDecks());
        backButton.setText(getTranslation("main.decks"));

        deckTitle = new H2(getTranslation("deck.loading"));
        deckTitle.addClassName("deck-view__title");

        deckStats = new Span();
        deckStats.addClassName("deck-view__stats");

        leftSection.add(backButton, deckTitle, deckStats);
        headerLayout.add(leftSection);

        container.add(headerLayout);
    }

    private void createDeckInfo(VerticalLayout container) {
        Div infoSection = new Div();
        infoSection.addClassName("deck-view__info-section");

        deckDescription = new Span(getTranslation("deck.description.loading"));
        deckDescription.addClassName("deck-view__description");

        infoSection.add(deckDescription);
        container.add(infoSection);
    }

    private void createActions(VerticalLayout container) {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidthFull();
        actionsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        actionsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button practiceButton = ButtonHelper.createPlayButton(e -> {
            if (currentDeck != null) {
                NavigationHelper.navigateToPractice(currentDeck.getId());
            }
        });
        practiceButton.setText(getTranslation("deck.startSession"));

        Button addFlashcardButton = ButtonHelper.createPlusButton(e -> openFlashcardDialog(null));
        addFlashcardButton.setText(getTranslation("deck.addCard"));
        addFlashcardButton.getElement().setAttribute("data-testid", "deck-add-card");

        Button editDeckButton = ButtonHelper.createEditButton(e -> {
            if (currentDeck != null) {
                new DeckEditDialog(deckFacade, currentDeck, updated -> updateDeckInfo()).open();
            }
        });
        editDeckButton.getElement().setProperty("title", getTranslation("deck.edit.tooltip"));

        Button deleteDeckButton = new Button(VaadinIcon.TRASH.create());
        deleteDeckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteDeckButton.getElement().setProperty("title", getTranslation("deck.delete.tooltip"));
        deleteDeckButton.addClickListener(e -> deleteDeck());

        actionsLayout.add(practiceButton, addFlashcardButton, editDeckButton, deleteDeckButton);
        container.add(actionsLayout);
    }

    private void createFlashcardsGrid(VerticalLayout container) {
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
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
                NotificationHelper.showSuccessBottom(getTranslation("deck.progressReset"));
                loadFlashcards();
            }
        });
        rightFilters.add(hideKnownCheckbox, resetProgress);

        searchRow.add(flashcardSearchField, rightFilters);
        container.add(searchRow);

        flashcardGrid = new Grid<>(Flashcard.class, false);
        flashcardGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        flashcardGrid.setWidthFull();

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
                    return example != null && !example.trim().isEmpty() ? example : "-";
                })
                .setHeader(getTranslation("deck.col.example"))
                .setFlexGrow(3);

        flashcardGrid
                .addComponentColumn(flashcard -> {
                    boolean known = currentDeck != null && presenter.isKnown(currentDeck.getId(), flashcard.getId());
                    Span mark = new Span(known ? getTranslation("deck.knownMark") : "");
                    if (known) {}
                    return mark;
                })
                .setHeader(getTranslation("deck.col.status"))
                .setFlexGrow(0)
                .setWidth("130px");

        flashcardGrid
                .addComponentColumn(flashcard -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(true);
                    actions.setAlignItems(FlexComponent.Alignment.CENTER);
                    actions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

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
                .setFlexGrow(0)
                .setWidth("180px");

        container.add(flashcardGrid);
    }

    private void loadDeck(Long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            updateDeckInfo();
            loadFlashcards();
        } else {
            NotificationHelper.showError(getTranslation("deck.notFound"));
            NavigationHelper.navigateToError(RouteConstants.DECKS_ROUTE);
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

        FormLayout formLayout = createFlashcardForm(flashcard);
        BeanValidationBinder<Flashcard> binder = createFlashcardBinder(formLayout);
        HorizontalLayout buttonsLayout = createDialogButtons(dialog, binder, flashcard);

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

    private FormLayout createFlashcardForm(Flashcard flashcard) {
        FormLayout formLayout = new FormLayout();

        TextField frontTextField = new TextField(getTranslation("deck.col.front"));
        frontTextField.setWidthFull();
        frontTextField.setRequired(true);

        TextField backTextField = new TextField(getTranslation("deck.col.back"));
        backTextField.setWidthFull();
        backTextField.setRequired(true);

        TextArea exampleArea = new TextArea(getTranslation("deck.example.optional"));
        exampleArea.setWidthFull();
        exampleArea.setMaxHeight("100px");

        TextField imageUrlField = new TextField(getTranslation("deck.imageUrl.optional"));
        imageUrlField.setWidthFull();

        if (flashcard != null) {
            frontTextField.setValue(flashcard.getFrontText() != null ? flashcard.getFrontText() : "");
            backTextField.setValue(flashcard.getBackText() != null ? flashcard.getBackText() : "");
            exampleArea.setValue(flashcard.getExample() != null ? flashcard.getExample() : "");
            imageUrlField.setValue(flashcard.getImageUrl() != null ? flashcard.getImageUrl() : "");
        }

        formLayout.add(frontTextField, backTextField, exampleArea, imageUrlField);
        return formLayout;
    }

    private BeanValidationBinder<Flashcard> createFlashcardBinder(FormLayout formLayout) {
        BeanValidationBinder<Flashcard> binder = new BeanValidationBinder<>(Flashcard.class);

        TextField frontTextField = (TextField) formLayout.getChildren().toArray()[0];
        TextField backTextField = (TextField) formLayout.getChildren().toArray()[1];
        TextArea exampleArea = (TextArea) formLayout.getChildren().toArray()[2];
        TextField imageUrlField = (TextField) formLayout.getChildren().toArray()[3];

        binder.forField(frontTextField)
                .asRequired(getTranslation(FILL_REQUIRED_KEY))
                .bind(Flashcard::getFrontText, Flashcard::setFrontText);
        binder.forField(backTextField)
                .asRequired(getTranslation(FILL_REQUIRED_KEY))
                .bind(Flashcard::getBackText, Flashcard::setBackText);
        binder.forField(exampleArea).bind(Flashcard::getExample, Flashcard::setExample);
        binder.forField(imageUrlField).bind(Flashcard::getImageUrl, Flashcard::setImageUrl);

        return binder;
    }

    private HorizontalLayout createDialogButtons(
            Dialog dialog, BeanValidationBinder<Flashcard> binder, Flashcard flashcard) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        Button saveButton = new Button(getTranslation("dialog.save"), VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> handleFlashcardSave(dialog, binder, flashcard));

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addClickListener(e -> dialog.close());

        buttonsLayout.add(saveButton, cancelButton);
        return buttonsLayout;
    }

    private void handleFlashcardSave(Dialog dialog, BeanValidationBinder<Flashcard> binder, Flashcard flashcard) {
        try {
            Flashcard bean = flashcard != null ? flashcard : new Flashcard();
            bean.setDeckId(currentDeck.getId());
            binder.writeBean(bean);
            deckFacade.saveFlashcard(bean);
            loadFlashcards();
            updateDeckInfo();
            dialog.close();
            NotificationHelper.showSuccessBottom(
                    flashcard == null ? getTranslation("deck.card.added") : getTranslation("deck.card.updated"));
        } catch (ValidationException vex) {
            NotificationHelper.showValidationError();
        } catch (Exception ex) {
            NotificationHelper.showErrorLong(ex.getMessage());
        }
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
            NotificationHelper.showDeleteSuccess();
        });

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private void deleteDeck() {
        if (currentDeck == null) {
            return;
        }

        // Check if deck is empty using presenter.deckSize() for accurate count
        int cardCount = presenter.deckSize(currentDeck.getId());
        boolean isEmpty = cardCount == 0;

        // Debug logging
        log.debug(
                "Deck deletion check - Title: {}, Card count: {}, isEmpty: {}",
                currentDeck.getTitle(),
                cardCount,
                isEmpty);

        if (isEmpty) {
            showSimpleDeleteDialog();
        } else {
            showComplexDeleteDialog();
        }
    }

    private void showSimpleDeleteDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setModal(true);
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Simple icon and title
        Div icon = new Div();
        icon.add(VaadinIcon.INFO_CIRCLE.create());
        icon.addClassName("deck-delete-dialog__icon");

        H3 title = new H3(getTranslation("deck.delete.simpleTitle"));
        title.addClassName("deck-delete-dialog__title");

        // Description
        Span description = new Span(getTranslation("deck.delete.simpleDescription", currentDeck.getTitle()));
        description.addClassName("deck-delete-dialog__description");

        // Additional info about card count
        int actualCardCount = presenter.deckSize(currentDeck.getId());
        if (actualCardCount > 0) {
            Span cardCountInfo = new Span(getTranslation("deck.delete.actualCardCount", actualCardCount));
            cardCountInfo.addClassName("deck-delete-dialog__card-count");
            layout.add(cardCountInfo);
        }

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button confirmButton = new Button(getTranslation("deck.delete.simpleConfirm"), VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        confirmButton.addClickListener(e -> {
            try {
                deckFacade.deleteDeck(currentDeck.getId());
                confirmDialog.close();
                NotificationHelper.showSuccessBottom(getTranslation("deck.delete.success"));
                NavigationHelper.navigateTo(RouteConstants.DECKS_ROUTE);
            } catch (Exception ex) {
                NotificationHelper.showErrorLong(ex.getMessage());
            }
        });

        cancelButton.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(icon, title, description, buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private void showComplexDeleteDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setModal(true);
        confirmDialog.setCloseOnEsc(true);
        confirmDialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Warning icon and title
        Div warningIcon = new Div();
        warningIcon.add(VaadinIcon.WARNING.create());
        warningIcon.addClassName("deck-delete-confirm__warning-icon");

        H3 title = new H3(getTranslation("deck.delete.confirmTitle"));
        title.addClassName("deck-delete-confirm__title");

        // Description
        Span description = new Span(getTranslation("deck.delete.confirmDescription"));
        description.addClassName("deck-delete-confirm__description");

        // Deck info
        Div deckInfo = new Div();
        deckInfo.addClassName("deck-delete-confirm__info");

        Span deckName = new Span(currentDeck.getTitle());
        deckName.addClassName("deck-delete-confirm__deck-name");

        int actualCardCount = presenter.deckSize(currentDeck.getId());
        Span cardCount = new Span(getTranslation("deck.delete.cardCount", actualCardCount));
        cardCount.addClassName("deck-delete-confirm__card-count");

        deckInfo.add(deckName, cardCount);

        // Confirmation input
        TextField confirmInput = new TextField(getTranslation("deck.delete.confirmInput"));
        confirmInput.setPlaceholder(currentDeck.getTitle());
        confirmInput.setWidthFull();
        confirmInput.setRequired(true);

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button confirmButton = new Button(getTranslation("deck.delete.confirm"), VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.setEnabled(false);

        Button cancelButton = new Button(getTranslation("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Enable confirm button only when deck name is typed correctly
        // TODO: Replace with Backend validation for security in the future
        confirmInput.addValueChangeListener(e -> {
            confirmButton.setEnabled(currentDeck.getTitle().equals(e.getValue()));
        });

        // Check on every value change
        confirmInput.addInputListener(e -> {
            log.info("Confirm input value: {}", confirmInput.getValue());
            confirmButton.setEnabled(currentDeck.getTitle().equals(confirmInput.getValue()));
        });

        // Removed duplicate KeyDownListener since InputListener already handles all changes

        confirmButton.addClickListener(e -> {
            try {
                deckFacade.deleteDeck(currentDeck.getId());
                confirmDialog.close();
                NotificationHelper.showSuccessBottom(getTranslation("deck.delete.success"));
                NavigationHelper.navigateTo(RouteConstants.DECKS_ROUTE);
            } catch (Exception ex) {
                NotificationHelper.showErrorLong(ex.getMessage());
            }
        });

        cancelButton.addClickListener(e -> confirmDialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(warningIcon, title, description, deckInfo, confirmInput, buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }
}
