package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "forgot-password", layout = PublicLayout.class)
@AnonymousAllowed
public class ForgotPasswordView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private static final String COMPONENT_WIDTH = "420px";

    private static final class ForgotPasswordModel {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    private final PasswordResetService passwordResetService;

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.addClassName("forgot-password-form");

        // Create binder and model
        Binder<ForgotPasswordModel> binder = new Binder<>(ForgotPasswordModel.class);
        ForgotPasswordModel model = new ForgotPasswordModel();
        binder.setBean(model);

        H2 title = new H2(getTranslation("auth.forgotPassword.title"));
        title.addClassName("forgot-password-form__title");

        TextField email = FormHelper.createRequiredTextField(
                getTranslation("auth.email"), getTranslation("auth.email.placeholder"));
        email.setWidth(COMPONENT_WIDTH);

        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation("auth.forgotPassword.submit"), e -> handleSubmit(model.getEmail()));
        submit.setWidth(COMPONENT_WIDTH);

        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation("auth.forgotPassword.backToLogin"), e -> getUI().ifPresent(ui -> ui.navigate("login")));
        backToLogin.setWidth(COMPONENT_WIDTH);

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> getUI().ifPresent(ui -> ui.navigate("")));
        backToHome.setWidth(COMPONENT_WIDTH);

        // Bind fields to model
        binder.forField(email)
                .asRequired(getTranslation("vaadin.validation.email.required"))
                .bind(ForgotPasswordModel::getEmail, ForgotPasswordModel::setEmail);

        wrapper.add(title, email, submit, backToLogin, backToHome);
        add(wrapper);
    }

    private void handleSubmit(String email) {
        if (email == null || email.trim().isEmpty()) {
            NotificationHelper.showError(getTranslation("auth.forgotPassword.emailRequired"));
            return;
        }

        try {
            var tokenOpt = passwordResetService.createPasswordResetToken(email.trim());
            if (tokenOpt.isPresent()) {
                // In a real application, you would send this token via email
                // For now, we'll show it in a notification (this is just for demo)
                String token = tokenOpt.get();

                NotificationHelper.showSuccess(getTranslation("auth.forgotPassword.tokenCreated"));

                // Navigate to reset password page
                getUI().ifPresent(ui -> ui.navigate("reset-password/" + token));
            } else {
                NotificationHelper.showInfo(getTranslation("auth.forgotPassword.emailNotFound"));
            }
        } catch (Exception ex) {
            NotificationHelper.showError(getTranslation("auth.forgotPassword.error"));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("auth.forgotPassword.title");
    }
}
