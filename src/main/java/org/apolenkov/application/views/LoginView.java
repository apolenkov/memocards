package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.AuthFacade;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "login", layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private static final String COMPONENT_WIDTH = "420px";

    private static final class LoginModel {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public LoginView(AuthFacade authFacade) {
        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.addClassName("login-form");

        // Create binder and model first
        Binder<LoginModel> binder = new Binder<>(LoginModel.class);
        LoginModel model = new LoginModel();
        binder.setBean(model);

        TextField email = FormHelper.createRequiredTextField(
                getTranslation("auth.email"), getTranslation("auth.email.placeholder"));
        email.setWidth(COMPONENT_WIDTH);

        PasswordField password = new PasswordField(getTranslation("auth.login.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidth(COMPONENT_WIDTH);
        password.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(getTranslation("auth.login.submit"), e -> {
            if (binder.validate().isOk()) {
                try {
                    authFacade.authenticateAndPersist(model.getEmail(), model.getPassword());
                    getUI().ifPresent(ui -> ui.navigate(""));
                } catch (Exception ex) {
                    NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
                }
            }
        });
        submit.setWidth(COMPONENT_WIDTH);

        Button forgot =
                ButtonHelper.createTertiaryButton(getTranslation("auth.login.forgotPassword"), e -> getUI().ifPresent(
                                ui -> ui.navigate("forgot-password")));
        forgot.setWidth(COMPONENT_WIDTH);

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> getUI().ifPresent(ui -> ui.navigate("")));
        backToHome.setWidth(COMPONENT_WIDTH);

        // Bind fields to model
        binder.forField(email)
                .asRequired(getTranslation("vaadin.validation.email.required"))
                .bind(LoginModel::getEmail, LoginModel::setEmail);
        binder.forField(password)
                .asRequired(getTranslation("vaadin.validation.password.required"))
                .bind(LoginModel::getPassword, LoginModel::setPassword);

        wrapper.add(email, password, submit, forgot, backToHome);
        add(wrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
            return;
        }
        boolean hasError =
                event.getLocation().getQueryParameters().getParameters().containsKey("error");
        if (hasError) NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("auth.login");
    }
}
