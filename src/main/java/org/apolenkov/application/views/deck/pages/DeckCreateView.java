package org.apolenkov.application.views.deck.pages;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;

/**
 * View for creating new flashcard decks.
 * Provides a form interface for users to create new decks with
 * name and description. Includes validation and navigation controls.
 */
@Route("decks/new")
@RolesAllowed(SecurityConstants.ROLE_USER)
public class DeckCreateView extends Composite<VerticalLayout> implements HasDynamicTitle {

    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;

    private TextField titleField;
    private TextArea descriptionArea;

    /**
     * Creates a new deck creation view.
     *
     * @param deckUseCaseValue deck business logic for saving new decks
     * @param userUseCaseValue user business logic for current user operations
     */
    public DeckCreateView(final DeckUseCase deckUseCaseValue, final UserUseCase userUseCaseValue) {
        this.deckUseCase = deckUseCaseValue;
        this.userUseCase = userUseCaseValue;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        createHeader();
        createForm();
    }

    /**
     * Creates the header section with navigation controls.
     * Coordinates the creation of header components.
     */
    private void createHeader() {
        HorizontalLayout headerLayout = createHeaderLayout();
        HorizontalLayout leftSection = createLeftSection();

        headerLayout.add(leftSection);
        getContent().add(headerLayout);
    }

    /**
     * Creates the main header layout.
     *
     * @return configured header layout
     */
    private HorizontalLayout createHeaderLayout() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return headerLayout;
    }

    /**
     * Creates the left section with back button and title.
     *
     * @return configured left section layout
     */
    private HorizontalLayout createLeftSection() {
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = createBackButton();
        H2 title = createTitle();

        leftSection.add(backButton, title);
        return leftSection;
    }

    /**
     * Creates the back navigation button.
     *
     * @return configured back button
     */
    private Button createBackButton() {
        Button backButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.COMMON_BACK),
                VaadinIcon.ARROW_LEFT,
                e -> NavigationHelper.navigateToDecks(),
                ButtonVariant.LUMO_TERTIARY);
        backButton.setText(getTranslation(DeckConstants.DECK_CREATE_BACK));
        return backButton;
    }

    /**
     * Creates the page title.
     *
     * @return configured title component
     */
    private H2 createTitle() {
        return new H2(getTranslation(DeckConstants.DECK_CREATE_TITLE));
    }

    /**
     * Creates the main form for deck creation.
     * Coordinates the creation of form components.
     */
    private void createForm() {
        Div formContainer = createFormContainer();
        VerticalLayout formLayout = createFormLayout();

        formContainer.add(formLayout);
        getContent().add(formContainer);
    }

    /**
     * Creates the form container with styling.
     *
     * @return configured form container
     */
    private Div createFormContainer() {
        Div formContainer = new Div();
        formContainer.addClassName(DeckConstants.SURFACE_PANEL_CLASS);
        formContainer.addClassName(DeckConstants.DECK_CREATE_FORM_CLASS);
        return formContainer;
    }

    /**
     * Creates the form layout with all form elements.
     *
     * @return configured form layout
     */
    private VerticalLayout createFormLayout() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.addClassName(DeckConstants.DECK_CREATE_FORM_LAYOUT_CLASS);

        H3 formTitle = createFormTitle();
        titleField = createTitleField();
        descriptionArea = createDescriptionArea();
        HorizontalLayout buttonsLayout = createButtonsLayout();

        formLayout.add(formTitle, titleField, descriptionArea, buttonsLayout);
        return formLayout;
    }

    /**
     * Creates the form title.
     *
     * @return configured form title
     */
    private H3 createFormTitle() {
        return new H3(getTranslation(DeckConstants.DECK_CREATE_SECTION));
    }

    /**
     * Creates the title input field.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField field = new TextField(getTranslation(DeckConstants.DECK_CREATE_NAME));
        field.setPlaceholder(getTranslation(DeckConstants.DECK_CREATE_NAME_PLACEHOLDER));
        field.setRequiredIndicatorVisible(true);
        field.setWidthFull();
        return field;
    }

    /**
     * Creates the description text area.
     *
     * @return configured description area
     */
    private TextArea createDescriptionArea() {
        TextArea area = new TextArea(getTranslation(DeckConstants.DECK_CREATE_DESCRIPTION));
        area.setPlaceholder(getTranslation(DeckConstants.DECK_CREATE_DESCRIPTION_PLACEHOLDER));
        area.setClearButtonVisible(true);
        area.setWidthFull();
        area.addClassName(DeckConstants.TEXT_AREA_MD_CLASS);
        return area;
    }

    /**
     * Creates the buttons layout with save and cancel buttons.
     *
     * @return configured buttons layout
     */
    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonsLayout.setWidthFull();

        Button saveButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_CREATE_CREATE),
                VaadinIcon.CHECK,
                e -> saveDeck(),
                ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = ButtonHelper.createButton(
                getTranslation(DeckConstants.DECK_CREATE_CANCEL),
                VaadinIcon.CLOSE,
                e -> NavigationHelper.navigateToDecks(),
                ButtonVariant.LUMO_TERTIARY);

        buttonsLayout.add(saveButton, cancelButton);
        return buttonsLayout;
    }

    /**
     * Saves the new deck to the system.
     */
    private void saveDeck() {
        String title = ValidationHelper.safeTrimToEmpty(titleField.getValue());
        String description = ValidationHelper.safeTrim(descriptionArea.getValue()); // Nullable field

        if (ValidationHelper.validateRequiredSimple(
                titleField, title, getTranslation(DeckConstants.DECK_CREATE_ENTER_TITLE))) {
            return;
        }

        try {
            Deck deck = new Deck();
            deck.setUserId(userUseCase.getCurrentUser().getId());
            deck.setTitle(title);
            deck.setDescription(description);

            Deck savedDeck = deckUseCase.saveDeck(deck);

            NotificationHelper.showSuccessBottom(
                    getTranslation(DeckConstants.DECK_CREATE_CREATED, savedDeck.getTitle()));
            NavigationHelper.navigateToDeck(savedDeck.getId());
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation(DeckConstants.DECK_CREATE_ERROR, e.getMessage()));
        }
    }

    /**
     * Returns the page title for this view.
     * Implements the HasDynamicTitle interface to provide localized
     * page titles for the deck creation view.
     *
     * @return the localized page title for the deck creation view
     */
    @Override
    public String getPageTitle() {
        return getTranslation(DeckConstants.DECK_CREATE_TITLE);
    }
}
