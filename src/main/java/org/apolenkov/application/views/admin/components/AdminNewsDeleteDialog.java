package org.apolenkov.application.views.admin.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.model.News;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;

/**
 * Dialog component for news deletion confirmation.
 * Provides a confirmation interface before deleting news articles.
 */
public final class AdminNewsDeleteDialog extends Dialog {

    // Data
    private final transient News news;

    // Callbacks
    private final transient Runnable onConfirm;

    /**
     * Creates a new AdminNewsDeleteDialog.
     *
     * @param newsParam the news to delete
     * @param onConfirmParam callback executed when deletion is confirmed
     */
    public AdminNewsDeleteDialog(final News newsParam, final Runnable onConfirmParam) {
        super();
        this.news = newsParam;
        this.onConfirm = onConfirmParam;
    }

    /**
     * Initializes the dialog components when attached to the UI.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        addClassName(AdminConstants.DIALOG_SM_CLASS);
        build();
    }

    /**
     * Builds the dialog UI components and layout.
     */
    private void build() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_TITLE_KEY)));

        Div messageContainer = createMessageContainer();
        layout.add(messageContainer);

        HorizontalLayout buttons = createButtonLayout();
        layout.add(buttons);

        add(layout);
    }

    /**
     * Creates the message container with news title.
     *
     * @return configured message container
     */
    private Div createMessageContainer() {
        Div messageContainer = new Div();
        messageContainer.addClassName(AdminConstants.TEXT_CENTER_CLASS);
        messageContainer.setWidthFull();

        Span prefixSpan = new Span(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_PREFIX_KEY) + " ");
        Span titleSpan = new Span(news.getTitle());
        titleSpan.addClassName(AdminConstants.ADMIN_DIALOG_CONFIRM_TITLE_CLASS);
        Span suffixSpan = new Span(getTranslation(AdminConstants.ADMIN_NEWS_CONFIRM_DELETE_SUFFIX_KEY));

        messageContainer.add(prefixSpan, titleSpan, suffixSpan);
        return messageContainer;
    }

    /**
     * Creates the button layout with confirm and cancel buttons.
     *
     * @return configured button layout
     */
    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton =
                ButtonHelper.createConfirmButton(getTranslation(AdminConstants.DIALOG_CONFIRM_KEY), e -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                    close();
                });

        Button cancelButton =
                ButtonHelper.createCancelButton(getTranslation(AdminConstants.DIALOG_CANCEL_KEY), e -> close());

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }
}
