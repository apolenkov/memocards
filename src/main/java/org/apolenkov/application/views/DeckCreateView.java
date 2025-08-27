package org.apolenkov.application.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.IconHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.apolenkov.application.views.utils.TextHelper;

/**
 * View for creating new flashcard decks.
 * Provides a form interface for users to create new decks with
 * name and description. Includes validation and navigation controls.
 */
@Route("decks/new")
@RolesAllowed("ROLE_USER")
public class DeckCreateView extends Composite<VerticalLayout> implements HasDynamicTitle {

    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private BeanValidationBinder<Deck> binder;

    /**
     * Creates a new deck creation view.
     * Initializes the view with proper layout and styling, creating
     * the header with navigation controls and the main form for deck creation.
     * The view is configured with responsive design and proper spacing.
     *
     * @param deckUseCaseValue deck business logic for saving new decks
     * @param userUseCaseValue user business logic for current user operations
     */
    public DeckCreateView(final DeckUseCase deckUseCaseValue, final UserUseCase userUseCaseValue) {
        this.deckUseCase = deckUseCaseValue;
        this.userUseCase = userUseCaseValue;

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

        Button backButton = ButtonHelper.createBackButton(e -> NavigationHelper.navigateTo("decks"));
        backButton.setText(getTranslation("deckCreate.back"));

        H2 title = TextHelper.createPageTitle(getTranslation("deckCreate.title"));

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

        H3 formTitle = TextHelper.createSectionTitle(getTranslation("deckCreate.section"));

        TextField titleField = FormHelper.createRequiredTextField(
                getTranslation("deckCreate.name"), getTranslation("deckCreate.name.placeholder"));
        titleField.setWidthFull();

        TextArea descriptionArea = FormHelper.createTextArea(
                getTranslation("deckCreate.description"), getTranslation("deckCreate.description.placeholder"));
        descriptionArea.setWidthFull();
        descriptionArea.addClassName("text-area--md");

        binder = new BeanValidationBinder<>(Deck.class);
        binder.forField(titleField)
                .asRequired(getTranslation("deckCreate.enterTitle"))
                .bind(Deck::getTitle, Deck::setTitle);
        binder.forField(descriptionArea).bind(Deck::getDescription, Deck::setDescription);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button saveButton = ButtonHelper.createPrimaryButton(getTranslation("deckCreate.create"), e -> saveDeck());
        saveButton.setIcon(IconHelper.createCheckIcon());

        Button cancelButton = ButtonHelper.createTertiaryButton(
                getTranslation("deckCreate.cancel"), e -> NavigationHelper.navigateTo("decks"));
        cancelButton.setIcon(IconHelper.createCloseIcon());

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
            NavigationHelper.navigateTo("deck/" + savedDeck.getId().toString());
        } catch (ValidationException vex) {
            NotificationHelper.showValidationError();
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
