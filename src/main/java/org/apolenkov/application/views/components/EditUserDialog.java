package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.HashSet;
import java.util.Set;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.user.AdminUserService;

public class EditUserDialog extends Dialog {

    public interface OnSaved {
        void handle(User saved);
    }

    public EditUserDialog(AdminUserService service, User user, OnSaved onSaved) {
        setHeaderTitle(getTranslation("user.edit.title"));
        FormLayout form = new FormLayout();
        EmailField email = new EmailField(getTranslation("auth.email"));
        email.setWidthFull();
        email.setRequiredIndicatorVisible(true);
        email.setHelperText(getTranslation("auth.email.helper"));
        TextField name = new TextField(getTranslation("auth.name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);
        name.setHelperText(getTranslation("auth.name.helper"));
        TextField password = new TextField(getTranslation("user.password.newOptional"));
        password.setWidthFull();
        password.setHelperText(getTranslation("user.password.helper"));
        form.add(email, name, password);

        email.setValue(user.getEmail() == null ? "" : user.getEmail());
        name.setValue(user.getName() == null ? "" : user.getName());

        Button save = new Button(getTranslation("dialog.save"), e -> {
            email.setInvalid(false);
            name.setInvalid(false);
            password.setInvalid(false);

            String vEmail = email.getValue() == null ? "" : email.getValue().trim();
            String vName = name.getValue() == null ? "" : name.getValue().trim();
            String rawPassword = password.getValue() == null ? "" : password.getValue();

            boolean ok = true;

            // Email required + simple format check
            if (vEmail.isEmpty()) {
                email.setErrorMessage(getTranslation("auth.validation.emailRequired"));
                email.setInvalid(true);
                ok = false;
            } else if (!vEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
                email.setInvalid(true);
                ok = false;
            }

            // Name required
            if (vName.isEmpty()) {
                name.setErrorMessage(getTranslation("auth.validation.nameRequired"));
                name.setInvalid(true);
                ok = false;
            }

            // Password optional, but if provided – enforce policy
            if (!rawPassword.isBlank()) {
                boolean longEnough = rawPassword.length() >= 8;
                boolean hasLetter = rawPassword.matches(".*[A-Za-z].*");
                boolean hasDigit = rawPassword.matches(".*\\d.*");
                if (!(longEnough && hasLetter && hasDigit)) {
                    password.setErrorMessage(getTranslation("auth.validation.passwordPolicy"));
                    password.setInvalid(true);
                    ok = false;
                }
            }

            if (!ok) {
                Notification.show(getTranslation("auth.validation.fixErrors"));
                return;
            }

            try {
                Set<String> roles = new HashSet<>(user.getRoles());
                if (roles.isEmpty()) roles.add(SecurityConstants.ROLE_USER);
                String adminEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();
                User saved = service.updateUserWithAudit(adminEmail, user.getId(), vEmail, vName, roles, rawPassword);
                onSaved.handle(saved);
                close();
            } catch (IllegalArgumentException ex) {
                password.setErrorMessage(ex.getMessage());
                password.setInvalid(true);
                Notification.show(ex.getMessage());
            } catch (Exception ex) {
                Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
            }
        });
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
        add(form);
        setWidth("520px");
    }
}
