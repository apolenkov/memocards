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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

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
    private BeanValidationBinder<Deck> binder;

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
                getTranslation("common.back"),
                VaadinIcon.ARROW_LEFT,
                e -> NavigationHelper.navigateToDecks(),
                ButtonVariant.LUMO_TERTIARY);
        backButton.setText(getTranslation("deckCreate.back"));
        return backButton;
    }

    /**
     * Creates the page title.
     *
     * @return configured title component
     */
    private H2 createTitle() {
        return new H2(getTranslation("deckCreate.title"));
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
        formContainer.addClassName("surface-panel");
        formContainer.addClassName("deck-create__form");
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
        formLayout.addClassName("deck-create__form-layout");

        H3 formTitle = createFormTitle();
        TextField titleField = createTitleField();
        TextArea descriptionArea = createDescriptionArea();
        HorizontalLayout buttonsLayout = createButtonsLayout(titleField, descriptionArea);

        formLayout.add(formTitle, titleField, descriptionArea, buttonsLayout);
        return formLayout;
    }

    /**
     * Creates the form title.
     *
     * @return configured form title
     */
    private H3 createFormTitle() {
        return new H3(getTranslation("deckCreate.section"));
    }

    /**
     * Creates the title input field.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField titleField = new TextField(getTranslation("deckCreate.name"));
        titleField.setPlaceholder(getTranslation("deckCreate.name.placeholder"));
        titleField.setRequiredIndicatorVisible(true);
        titleField.setWidthFull();
        return titleField;
    }

    /**
     * Creates the description text area.
     *
     * @return configured description area
     */
    private TextArea createDescriptionArea() {
        TextArea descriptionArea = new TextArea(getTranslation("deckCreate.description"));
        descriptionArea.setPlaceholder(getTranslation("deckCreate.description.placeholder"));
        descriptionArea.setClearButtonVisible(true);
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");
        return descriptionArea;
    }

    /**
     * Creates the buttons layout with save and cancel buttons.
     *
     * @param titleField the title field for validation
     * @param descriptionArea the description area for validation
     * @return configured buttons layout
     */
    private HorizontalLayout createButtonsLayout(final TextField titleField, final TextArea descriptionArea) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonsLayout.setWidthFull();

        binder = createBinder(titleField, descriptionArea);
        Button saveButton = createSaveButton();
        Button cancelButton = createCancelButton();

        buttonsLayout.add(saveButton, cancelButton);
        return buttonsLayout;
    }

    /**
     * Creates the validation binder for form fields.
     *
     * @param titleField the title field to bind
     * @param descriptionArea the description area to bind
     * @return configured validation binder
     */
    private BeanValidationBinder<Deck> createBinder(final TextField titleField, final TextArea descriptionArea) {
        BeanValidationBinder<Deck> validationBinder = new BeanValidationBinder<>(Deck.class);
        validationBinder
                .forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        validationBinder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);
        return validationBinder;
    }

    /**
     * Creates the save button.
     *
     * @return configured save button
     */
    private Button createSaveButton() {
        Button saveButton = ButtonHelper.createPrimaryButton(getTranslation("deckCreate.create"), e -> saveDeck());
        saveButton.setIcon(VaadinIcon.CHECK.create());
        return saveButton;
    }

    /**
     * Creates the cancel button.
     *
     * @return configured cancel button
     */
    private Button createCancelButton() {
        Button cancelButton = ButtonHelper.createTertiaryButton(
                getTranslation("deckCreate.cancel"), e -> NavigationHelper.navigateToDecks());
        cancelButton.setIcon(VaadinIcon.CLOSE.create());
        return cancelButton;
    }

    /**
     * Saves the new deck to the system.
     * Handles the deck creation process including validation,
     * saving, and navigation to the new deck.
     */
    private void saveDeck() {
        try {
            Deck newDeck = new Deck();
            newDeck.setUserId(userUseCase.getCurrentUser().getId());
            binder.writeBean(newDeck);
            Deck savedDeck = deckUseCase.saveDeck(newDeck);
            NotificationHelper.showSuccessBottom(getTranslation("deckCreate.created", savedDeck.getTitle()));
            NavigationHelper.navigateToDeck(savedDeck.getId());
        } catch (ValidationException vex) {
            NotificationHelper.showError(getTranslation("dialog.fillRequired"));
        } catch (Exception e) {
            NotificationHelper.showError(getTranslation("deckCreate.error", e.getMessage()));
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
        return getTranslation("deckCreate.title");
    }
}
