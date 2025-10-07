package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.springframework.stereotype.Component;

/**
 * UI component responsible for managing logout confirmation dialog.
 * Handles dialog creation, layout, and user interaction for logout operations.
 */
@Component
@UIScope
public class TopMenuLogoutDialog extends Composite<Dialog> {

    private final transient TopMenuAuthService authService;

    /**
     * Creates a new TopMenuLogoutDialog with required dependencies.
     *
     * @param authenticationService service for authentication operations
     */
    public TopMenuLogoutDialog(final TopMenuAuthService authenticationService) {
        this.authService = authenticationService;
    }

    /**
     * Initializes and configures the content of the logout dialog.
     * Creates a new Dialog instance, applies styling, sets up the layout,
     * and configures the dialog's behavior before returning it.
     *
     * @return the configured Dialog instance ready for display
     */
    @Override
    protected Dialog initContent() {
        Dialog dialog = new Dialog();
        dialog.addClassName(CoreConstants.DIALOG_SM_CLASS);

        VerticalLayout layout = createDialogLayout();
        dialog.add(layout);

        configureDialogBehavior(dialog);
        return dialog;
    }

    /**
     * Opens a confirmation dialog for user logout.
     * Creates and displays a confirmation dialog to prevent accidental logouts.
     * Upon confirmation, performs the logout operation using the authentication service
     * and redirects to the home page.
     */
    public void openLogoutDialog() {
        UI.getCurrent().add(this);
        getContent().open();
    }

    /**
     * Creates the main layout for the logout dialog.
     *
     * @return the configured vertical layout
     */
    private VerticalLayout createDialogLayout() {
        VerticalLayout layout = new VerticalLayout();

        String confirmMessage = getTranslation(CoreConstants.AUTH_LOGOUT_CONFIRM_KEY);
        layout.add(new H3(confirmMessage));
        layout.add(new Span(confirmMessage));

        HorizontalLayout buttonsLayout = createButtonsLayout();
        layout.add(buttonsLayout);

        return layout;
    }

    /**
     * Creates the buttons layout for the logout dialog.
     *
     * @return the configured horizontal layout with buttons
     */
    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = createConfirmButton();
        Button cancelButton = createCancelButton();

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates the confirm button for logout action.
     *
     * @return the configured confirm button
     */
    private Button createConfirmButton() {
        return ButtonHelper.createConfirmButton(getTranslation(CoreConstants.DIALOG_CONFIRM_KEY), e -> {
            authService.performLogout();
            getContent().close();
            // Navigation to home page will be handled by the auth service
        });
    }

    /**
     * Creates the cancel button for dialog closure.
     *
     * @return the configured cancel button
     */
    private Button createCancelButton() {
        return ButtonHelper.createCancelButton(getTranslation(CoreConstants.DIALOG_CANCEL_KEY), e -> getContent()
                .close());
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
