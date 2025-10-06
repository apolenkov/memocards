package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.function.UnaryOperator;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.springframework.stereotype.Component;

/**
 * Service responsible for managing logout confirmation dialog.
 * Handles dialog creation, layout, and user interaction for logout operations.
 */
@Component
@UIScope
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
     * Creates and displays a confirmation dialog to prevent accidental logouts.
     * Upon confirmation, performs the logout operation using the authentication service
     * and redirects to the home page.
     *
     * @param translationProvider function to get translations for dialog text
     */
    public void openLogoutDialog(final UnaryOperator<String> translationProvider) {
        Dialog dialog = createLogoutDialog(translationProvider);
        // UI context will be handled by the calling component
        dialog.open();
    }

    /**
     * Creates the logout confirmation dialog with title, message, and action buttons.
     *
     * @param translationProvider function to get translations for dialog text
     * @return the configured logout dialog
     */
    private Dialog createLogoutDialog(final UnaryOperator<String> translationProvider) {
        Dialog dialog = new Dialog();
        dialog.addClassName(CoreConstants.DIALOG_SM_CLASS);

        VerticalLayout layout = createDialogLayout(dialog, translationProvider);
        dialog.add(layout);

        configureDialogBehavior(dialog);
        return dialog;
    }

    /**
     * Creates the main layout for the logout dialog.
     *
     * @param dialog the dialog instance for cancel button reference
     * @param translationProvider function to get translations for dialog text
     * @return the configured vertical layout
     */
    private VerticalLayout createDialogLayout(final Dialog dialog, final UnaryOperator<String> translationProvider) {
        VerticalLayout layout = new VerticalLayout();

        String confirmMessage = translationProvider.apply(CoreConstants.AUTH_LOGOUT_CONFIRM_KEY);
        layout.add(new H3(confirmMessage));
        layout.add(new Span(confirmMessage));

        HorizontalLayout buttonsLayout = createButtonsLayout(dialog, translationProvider);
        layout.add(buttonsLayout);

        return layout;
    }

    /**
     * Creates the buttons layout for the logout dialog.
     *
     * @param dialog the dialog instance for cancel button reference
     * @param translationProvider function to get translations for dialog text
     * @return the configured horizontal layout with buttons
     */
    private HorizontalLayout createButtonsLayout(final Dialog dialog, final UnaryOperator<String> translationProvider) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = createConfirmButton(dialog, translationProvider);
        Button cancelButton = createCancelButton(dialog, translationProvider);

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates the confirm button for logout action.
     *
     * @param dialog the dialog instance to close after logout
     * @param translationProvider function to get translations for dialog text
     * @return the configured confirm button
     */
    private Button createConfirmButton(final Dialog dialog, final UnaryOperator<String> translationProvider) {
        return ButtonHelper.createConfirmButton(translationProvider.apply(CoreConstants.DIALOG_CONFIRM_KEY), e -> {
            authService.performLogout();
            dialog.close();
            // Navigation to home page will be handled by the auth service
        });
    }

    /**
     * Creates the cancel button for dialog closure.
     *
     * @param dialog the dialog instance to close
     * @param translationProvider function to get translations for dialog text
     * @return the configured cancel button
     */
    private Button createCancelButton(final Dialog dialog, final UnaryOperator<String> translationProvider) {
        return ButtonHelper.createCancelButton(
                translationProvider.apply(CoreConstants.DIALOG_CANCEL_KEY), e -> dialog.close());
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
}
