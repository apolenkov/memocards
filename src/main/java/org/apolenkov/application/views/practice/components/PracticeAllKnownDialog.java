package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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
    }

    /**
     * Builds the dialog UI components and layout.
     */
    private void build() {
        H3 title = new H3(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        Paragraph message = new Paragraph(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, deckTitle));

        Button backToDeckButton = new Button(getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY), e -> {
            if (onBackToDeck != null) {
                onBackToDeck.run();
            }
            close();
        });
        backToDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backToDeckButton.focus();

        VerticalLayout dialogLayout = new VerticalLayout(title, message, backToDeckButton);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        add(dialogLayout);
    }
}
