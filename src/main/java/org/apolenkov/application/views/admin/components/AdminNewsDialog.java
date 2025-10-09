package org.apolenkov.application.views.admin.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;
import org.apolenkov.application.model.News;
import org.apolenkov.application.views.admin.constants.AdminConstants;
import org.apolenkov.application.views.shared.utils.AuthRedirectHelper;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.ValidationHelper;

/**
 * Dialog component for creating and editing news articles.
 * Provides form validation and callback-based communication.
 */
public final class AdminNewsDialog extends Dialog {

    // Data
    private final transient News news;

    // Callbacks
    private final transient Consumer<NewsFormData> onSave;

    /**
     * Creates a new AdminNewsDialog for creating or editing news.
     *
     * @param newsParam the news to edit, or null for creating new
     * @param onSaveParam callback executed when news is saved with form data
     */
    public AdminNewsDialog(final News newsParam, final Consumer<NewsFormData> onSaveParam) {
        super();
        this.news = newsParam;
        this.onSave = onSaveParam;
    }

    /**
     * Initializes the dialog components when attached to the UI.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        addClassName(AdminConstants.DIALOG_MD_CLASS);
        build();
    }

    /**
     * Builds the dialog UI components and layout.
     */
    private void build() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        H3 dialogTitle = new H3(
                news == null
                        ? getTranslation(AdminConstants.ADMIN_NEWS_ADD_KEY)
                        : getTranslation(AdminConstants.DIALOG_EDIT_KEY));
        content.add(dialogTitle);

        TextField titleField = createTitleField();
        TextArea contentField = createContentField();
        TextField authorField = createAuthorField();

        content.add(titleField, contentField, authorField);

        HorizontalLayout buttonRow = createButtonLayout(titleField, contentField, authorField);
        content.add(buttonRow);

        add(content);
    }

    /**
     * Creates the title input field.
     *
     * @return configured title field
     */
    private TextField createTitleField() {
        TextField titleField = new TextField(getTranslation(AdminConstants.ADMIN_NEWS_TITLE_KEY));
        titleField.setWidthFull();
        if (news != null) {
            titleField.setValue(news.getTitle());
        }
        return titleField;
    }

    /**
     * Creates the content text area.
     *
     * @return configured content field
     */
    private TextArea createContentField() {
        TextArea contentField = new TextArea(getTranslation(AdminConstants.ADMIN_NEWS_CONTENT_KEY));
        contentField.setWidthFull();
        contentField.addClassName(AdminConstants.ADMIN_NEWS_CONTENT_AREA_CLASS);
        if (news != null) {
            contentField.setValue(news.getContent());
        }
        return contentField;
    }

    /**
     * Creates the author input field.
     *
     * @return configured author field
     */
    private TextField createAuthorField() {
        TextField authorField = new TextField(getTranslation(AdminConstants.ADMIN_NEWS_AUTHOR_KEY));
        authorField.setWidthFull();
        if (news != null) {
            authorField.setValue(news.getAuthor());
        } else {
            String username = AuthRedirectHelper.getAuthenticatedUsername();
            if (username != null) {
                authorField.setValue(username);
            }
        }
        return authorField;
    }

    /**
     * Creates the button layout with save and cancel buttons.
     *
     * @param titleField the title field for validation
     * @param contentField the content field for validation
     * @param authorField the author field
     * @return configured button layout
     */
    private HorizontalLayout createButtonLayout(
            final TextField titleField, final TextArea contentField, final TextField authorField) {
        Button saveBtn = ButtonHelper.createPrimaryButton(getTranslation(AdminConstants.DIALOG_SAVE_KEY), e -> {
            if (handleSave(titleField, contentField, authorField)) {
                close();
            }
        });

        Button cancelBtn =
                ButtonHelper.createTertiaryButton(getTranslation(AdminConstants.COMMON_CANCEL_KEY), e -> close());

        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);
        buttonRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonRow.add(saveBtn, cancelBtn);

        return buttonRow;
    }

    /**
     * Handles save action with validation.
     *
     * @param titleField the title field
     * @param contentField the content field
     * @param authorField the author field
     * @return true if validation passed and callback executed
     */
    private boolean handleSave(final TextField titleField, final TextArea contentField, final TextField authorField) {
        String t = ValidationHelper.safeTrimToEmpty(titleField.getValue());
        String c = ValidationHelper.safeTrimToEmpty(contentField.getValue());

        if (ValidationHelper.validateRequiredSimple(
                titleField, t, getTranslation(AdminConstants.ADMIN_NEWS_VALIDATION_TITLE_REQUIRED_KEY))) {
            return false;
        }
        if (ValidationHelper.validateRequiredSimple(
                contentField, c, getTranslation(AdminConstants.ADMIN_NEWS_VALIDATION_CONTENT_REQUIRED_KEY))) {
            return false;
        }

        if (onSave != null) {
            Long newsId = news != null ? news.getId() : null;
            String author = ValidationHelper.safeTrimToEmpty(authorField.getValue());
            onSave.accept(new NewsFormData(newsId, t, c, author));
        }

        return true;
    }

    /**
     * Form data record for news submission.
     *
     * @param id the news ID (null for new news)
     * @param title the news title
     * @param content the news content
     * @param author the news author
     */
    public record NewsFormData(Long id, String title, String content, String author) {}
}
