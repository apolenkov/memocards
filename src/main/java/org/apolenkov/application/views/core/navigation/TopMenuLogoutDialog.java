package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.springframework.stereotype.Component;

/**
 * Factory for creating logout confirmation dialogs.
 * Creates new dialog instances to avoid component reuse between different UI trees.
 * Uses singleton scope as it only provides factory methods without storing component state.
 */
@Component
public class TopMenuLogoutDialog {

    private final TopMenuAuthService authService;

    /**
     * Creates a new TopMenuLogoutDialog with required dependencies.
     *
     * @param authenticationService service for authentication operations
     */
    public TopMenuLogoutDialog(final TopMenuAuthService authenticationService) {
        this.authService = authenticationService;
    }

    /**
     * Opens a confirmation dialog for user logout.
     * Creates and displays a NEW dialog instance to prevent accidental logouts.
     * Upon confirmation, performs the logout operation using the authentication service
     * and redirects to the home page.
     */
    public void openLogoutDialog() {
        Dialog dialog = createLogoutDialog();
        UI.getCurrent().add(dialog);
        dialog.open();
    }

    /**
     * Creates a new logout confirmation dialog.
     * Each invocation creates a fresh dialog instance to avoid component reuse issues.
     *
     * @return new configured Dialog instance
     */
    private Dialog createLogoutDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName(CoreConstants.DIALOG_SM_CLASS);

        VerticalLayout layout = createDialogLayout(dialog);
        dialog.add(layout);

        configureDialogBehavior(dialog);
        return dialog;
    }

    /**
     * Creates the main layout for the logout dialog.
     *
     * @param dialog the dialog instance for button handlers
     * @return the configured vertical layout
     */
    private VerticalLayout createDialogLayout(final Dialog dialog) {
        VerticalLayout layout = new VerticalLayout();

        String confirmMessage = getTranslation(CoreConstants.AUTH_LOGOUT_CONFIRM_KEY);
        layout.add(new H3(confirmMessage));
        layout.add(new Span(confirmMessage));

        HorizontalLayout buttonsLayout = createButtonsLayout(dialog);
        layout.add(buttonsLayout);

        return layout;
    }

    /**
     * Creates the buttons layout for the logout dialog.
     *
     * @param dialog the dialog instance for button handlers
     * @return the configured horizontal layout with buttons
     */
    private HorizontalLayout createButtonsLayout(final Dialog dialog) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = createConfirmButton(dialog);
        Button cancelButton = createCancelButton(dialog);

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates the confirm button for logout action.
     *
     * @param dialog the dialog instance to close after confirmation
     * @return the configured confirm button
     */
    private Button createConfirmButton(final Dialog dialog) {
        return ButtonHelper.createConfirmButton(getTranslation(CoreConstants.DIALOG_CONFIRM_KEY), e -> {
            authService.performLogout();
            dialog.close();
            // Navigation to home page will be handled by the auth service
        });
    }

    /**
     * Creates the cancel button for dialog closure.
     *
     * @param dialog the dialog instance to close
     * @return the configured cancel button
     */
    private Button createCancelButton(final Dialog dialog) {
        return ButtonHelper.createCancelButton(getTranslation(CoreConstants.DIALOG_CANCEL_KEY), e -> dialog.close());
    }

    /**
     * Configures dialog behavior for closing.
     *
     * @param dialog the dialog to configure
     */
    private void configureDialogBehavior(final Dialog dialog) {
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
    }

    /**
     * Gets translation for the specified key using current UI locale.
     *
     * @param key the translation key
     * @param params optional parameters
     * @return translated text
     */
    private String getTranslation(final String key, final Object... params) {
        return UI.getCurrent().getTranslation(key, params);
    }
}
