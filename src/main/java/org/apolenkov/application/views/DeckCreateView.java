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

@Route("decks/new")
@RolesAllowed("ROLE_USER")
public class DeckCreateView extends Composite<VerticalLayout> implements HasDynamicTitle {

    private final transient DeckUseCase deckUseCase;
    private final transient UserUseCase userUseCase;
    private BeanValidationBinder<Deck> binder;

    public DeckCreateView(DeckUseCase deckUseCase, UserUseCase userUseCase) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;

        getContent().setWidth("100%");
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        createHeader();
        createForm();
    }

    private void createHeader() {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = ButtonHelper.createBackButton(e -> NavigationHelper.navigateTo("decks"));
        backButton.setText(getTranslation("deckCreate.back"));

        H2 title = TextHelper.createPageTitle(getTranslation("deckCreate.title"));
        title.addClassName("deckedit-view__header-title");

        leftSection.add(backButton, title);
        headerLayout.add(leftSection);

        getContent().add(headerLayout);
    }

    private void createForm() {
        Div formContainer = new Div();
        formContainer.setWidth("100%");
        formContainer.setMaxWidth("600px");
        formContainer
                .getStyle()
                .set("border", "2px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-xl)")
                .set("margin", "var(--lumo-space-l) auto")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.setWidth("100%");

        H3 formTitle = TextHelper.createSectionTitle(getTranslation("deckCreate.section"));
        formTitle.addClassName("deckedit-view__section-title");

        TextField titleField = FormHelper.createRequiredTextField(
                getTranslation("deckCreate.name"), getTranslation("deckCreate.name.placeholder"));
        titleField.setWidth("100%");

        TextArea descriptionArea = FormHelper.createTextArea(
                getTranslation("deckCreate.description"), getTranslation("deckCreate.description.placeholder"));
        descriptionArea.setWidth("100%");
        descriptionArea.setMaxHeight("150px");

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

    @Override
    public String getPageTitle() {
        return getTranslation("deckCreate.title");
    }
}
