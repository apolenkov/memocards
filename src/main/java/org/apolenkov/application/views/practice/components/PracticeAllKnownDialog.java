package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.practice.constants.PracticeConstants;

/**
 * Dialog component for displaying "All Cards Known" message.
 * Shown when user has already studied all cards in a deck.
 */
public final class PracticeAllKnownDialog extends Dialog {

    private final String deckTitle;
    private final transient Runnable onBackToDeck;

    /**
     * Creates a new PracticeAllKnownDialog.
     *
     * @param deckTitleParam the title of the deck
     * @param onBackToDeckParam callback executed when user clicks back to deck
     */
    public PracticeAllKnownDialog(final String deckTitleParam, final Runnable onBackToDeckParam) {
        super();
        this.deckTitle = deckTitleParam;
        this.onBackToDeck = onBackToDeckParam;
    }

    /**
     * Initializes the dialog components when attached to the UI.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureDialog();
        build();
    }

    /**
     * Configures basic dialog properties.
     */
    private void configureDialog() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModal(true);
        setDraggable(false);
        setResizable(false);
        addClassName(PracticeConstants.DIALOG_MD_CLASS);
        addClassName("congratulations-dialog");
    }

    /**
     * Builds the dialog UI components and layout.
     */
    private void build() {
        // Create celebration icon container
        Div iconContainer = createCelebrationIcon();

        // Create title with emoji
        H2 title = new H2(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        title.addClassName("congratulations-title");

        // Create message with better formatting
        Paragraph message = new Paragraph(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, deckTitle));
        message.addClassName("congratulations-message");

        // Create success badge
        Span successBadge = new Span(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_BADGE_KEY));
        successBadge.addClassName("success-badge");

        // Create action button with icon
        Button backToDeckButton = new Button(getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY));
        backToDeckButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        backToDeckButton.setIconAfterText(false);
        backToDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        backToDeckButton.addClassName("action-button");
        backToDeckButton.addClickListener(e -> {
            if (onBackToDeck != null) {
                onBackToDeck.run();
            }
            close();
        });
        backToDeckButton.focus();

        // Create main layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.addClassName("congratulations-layout");
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setSpacing(false);
        dialogLayout.setPadding(false);

        // Add components with proper spacing
        dialogLayout.add(iconContainer, title, message, successBadge, backToDeckButton);

        add(dialogLayout);
    }

    /**
     * Creates a celebration icon container with animation.
     *
     * @return a Div container with animated trophy icon
     */
    private Div createCelebrationIcon() {
        Div iconContainer = new Div();
        iconContainer.addClassName("celebration-icon-container");

        Span trophyIcon = new Span("🏆");
        trophyIcon.addClassName("trophy-icon");

        iconContainer.add(trophyIcon);
        return iconContainer;
    }
}
