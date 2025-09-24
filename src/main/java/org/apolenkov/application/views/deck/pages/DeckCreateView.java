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
     * Builds a header layout containing a back button for navigation
     * to the decks list and the main page title. The header is styled
     * with proper alignment and spacing.
     */
    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = ButtonHelper.createButton(
                getTranslation("common.back"),
                VaadinIcon.ARROW_LEFT,
                e -> NavigationHelper.navigateToDecks(),
                ButtonVariant.LUMO_TERTIARY);
        backButton.setText(getTranslation("deckCreate.back"));

        H2 title = new H2(getTranslation("deckCreate.title"));

        leftSection.add(backButton, title);
        headerLayout.add(leftSection);

        getContent().add(headerLayout);
    }

    /**
     * Creates the main form for deck creation.
     * Builds a comprehensive form with title and description fields,
     * validation binding, and action buttons.
     */
    private void createForm() {
        Div formContainer = new Div();
        formContainer.addClassName("surface-panel");
        formContainer.addClassName("deck-create__form");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.addClassName("deck-create__form-layout");

        H3 formTitle = new H3(getTranslation("deckCreate.section"));

        TextField titleField = new TextField(getTranslation("deckCreate.name"));
        titleField.setPlaceholder(getTranslation("deckCreate.name.placeholder"));
        titleField.setRequiredIndicatorVisible(true);
        titleField.setWidthFull();

        TextArea descriptionArea = new TextArea(getTranslation("deckCreate.description"));
        descriptionArea.setPlaceholder(getTranslation("deckCreate.description.placeholder"));
        descriptionArea.setClearButtonVisible(true);
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");

        binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonsLayout.setWidthFull();

        Button saveButton = ButtonHelper.createPrimaryButton(getTranslation("deckCreate.create"), e -> saveDeck());
        saveButton.setIcon(VaadinIcon.CHECK.create());

        Button cancelButton = ButtonHelper.createTertiaryButton(
                getTranslation("deckCreate.cancel"), e -> NavigationHelper.navigateToDecks());
        cancelButton.setIcon(VaadinIcon.CLOSE.create());

        buttonsLayout.add(saveButton, cancelButton);

        formLayout.add(formTitle, titleField, descriptionArea, buttonsLayout);
        formContainer.add(formLayout);

        getContent().add(formContainer);
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
