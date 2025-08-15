package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.user.AdminUserService;

public class CreateUserDialog extends Dialog {

    public interface OnSaved {
        void handle(User saved);
    }

    public CreateUserDialog(AdminUserService service, OnSaved onSaved) {
        setHeaderTitle(getTranslation("user.create.title"));
        FormLayout form = new FormLayout();
        EmailField email = new EmailField(getTranslation("auth.email"));
        email.setWidthFull();
        email.setRequiredIndicatorVisible(true);
        email.setHelperText(getTranslation("auth.email.helper"));
        TextField name = new TextField(getTranslation("auth.name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);
        name.setHelperText(getTranslation("auth.name.helper"));
        PasswordField password = new PasswordField(getTranslation("auth.password"));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);
        password.setHelperText(getTranslation("user.password.helper"));

        LinkedHashMap<String, String> labelToRole = new LinkedHashMap<>();
        labelToRole.put(getTranslation("admin.users.role.USER"), SecurityConstants.ROLE_USER);
        labelToRole.put(getTranslation("admin.users.role.ADMIN"), SecurityConstants.ROLE_ADMIN);
        CheckboxGroup<String> rolesBox = new CheckboxGroup<>();
        rolesBox.setLabel(getTranslation("admin.users.columns.roles"));
        rolesBox.setItems(labelToRole.keySet());
        rolesBox.setValue(java.util.Set.of(getTranslation("admin.users.role.USER")));
        form.add(email, name, password, rolesBox);

        Button save = new Button(getTranslation("dialog.save"), e -> {
            email.setInvalid(false);
            name.setInvalid(false);
            password.setInvalid(false);
            String vEmail = email.getValue() == null ? "" : email.getValue().trim();
            String vName = name.getValue() == null ? "" : name.getValue().trim();
            String vPwd = password.getValue() == null ? "" : password.getValue();
            boolean ok = true;
            if (vEmail.isEmpty() || !vEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
                email.setInvalid(true);
                ok = false;
            }
            if (vName.isEmpty() || vName.length() < 2) {
                name.setErrorMessage(getTranslation("auth.validation.nameMin2"));
                name.setInvalid(true);
                ok = false;
            }
            if (vPwd.length() < 8 || !vPwd.matches(".*[A-Za-z].*") || !vPwd.matches(".*\\d.*")) {
                password.setErrorMessage(getTranslation("auth.validation.passwordPolicy"));
                password.setInvalid(true);
                ok = false;
            }
            if (!ok) {
                Notification.show(getTranslation("auth.validation.fixErrors"));
                return;
            }
            try {
                Set<String> roles = new HashSet<>();
                if (rolesBox.getValue() == null || rolesBox.getValue().isEmpty()) {
                    Notification.show(getTranslation("admin.users.error.rolesRequired"));
                    return;
                }
                for (String label : rolesBox.getValue()) {
                    String role = labelToRole.get(label);
                    if (role != null) roles.add(role);
                }
                String adminEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();
                User saved = service.createUserWithAudit(adminEmail, vEmail, vName, vPwd, roles);
                if (onSaved != null) onSaved.handle(saved);
                close();
            } catch (Exception ex) {
                Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
            }
        });
        Button cancel = new Button(getTranslation("dialog.cancel"), e -> close());
        getFooter().add(cancel, save);
        add(form);
        setWidth("520px");
    }
}
