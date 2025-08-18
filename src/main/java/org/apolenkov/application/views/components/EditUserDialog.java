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

    private final transient AdminUserService service;
    private final transient User user;
    private final transient OnSaved onSaved;
    private EmailField email;
    private TextField name;
    private TextField password;

    public EditUserDialog(AdminUserService service, User user, OnSaved onSaved) {
        this.service = service;
        this.user = user;
        this.onSaved = onSaved;

        setHeaderTitle(getTranslation("user.edit.title"));
        createForm();
        createButtons();
        add(createFormLayout());
        setWidth("520px");
    }

    private FormLayout createFormLayout() {
        FormLayout form = new FormLayout();
        email = new EmailField(getTranslation("auth.email"));
        email.setPlaceholder(getTranslation("auth.email.placeholder"));
        email.setWidthFull();
        email.setRequiredIndicatorVisible(true);
        email.setHelperText(getTranslation("auth.email.helper"));

        name = new TextField(getTranslation("auth.name"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);
        name.setHelperText(getTranslation("auth.name.helper"));

        password = new TextField(getTranslation("user.password.newOptional"));
        password.setWidthFull();
        password.setHelperText(getTranslation("user.password.helper"));

        form.add(email, name, password);
        return form;
    }

    private void createForm() {
        // This will be called by createFormLayout, keeping the existing structure
    }

    private void createButtons() {
        Button save = new Button(getTranslation("dialog.save"), e -> handleSave());
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    private void handleSave() {
        clearValidationErrors();
        populateFieldValues();

        if (validateAllFields()) {
            performSave();
        } else {
            Notification.show(getTranslation("auth.validation.fixErrors"));
        }
    }

    private void clearValidationErrors() {
        email.setInvalid(false);
        name.setInvalid(false);
        password.setInvalid(false);
    }

    private void populateFieldValues() {
        email.setValue(user.getEmail() == null ? "" : user.getEmail());
        name.setValue(user.getName() == null ? "" : user.getName());
    }

    private boolean validateAllFields() {
        boolean emailValid = validateEmail();
        boolean nameValid = validateName();
        boolean passwordValid = validatePassword();

        return emailValid && nameValid && passwordValid;
    }

    private boolean validateEmail() {
        String vEmail = email.getValue() == null ? "" : email.getValue().trim();

        if (vEmail.isEmpty()) {
            email.setErrorMessage(getTranslation("auth.validation.emailRequired"));
            email.setInvalid(true);
            return false;
        }

        if (!vEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
            email.setInvalid(true);
            return false;
        }

        return true;
    }

    private boolean validateName() {
        String vName = name.getValue() == null ? "" : name.getValue().trim();

        if (vName.isEmpty()) {
            name.setErrorMessage(getTranslation("auth.validation.nameRequired"));
            name.setInvalid(true);
            return false;
        }

        return true;
    }

    private boolean validatePassword() {
        String rawPassword = password.getValue() == null ? "" : password.getValue();

        if (rawPassword.isBlank()) {
            return true; // Password is optional
        }

        boolean longEnough = rawPassword.length() >= 8;
        boolean hasLetter = rawPassword.matches(".*[A-Za-z].*");
        boolean hasDigit = rawPassword.matches(".*\\d.*");

        if (!(longEnough && hasLetter && hasDigit)) {
            password.setErrorMessage(getTranslation("auth.validation.passwordPolicy"));
            password.setInvalid(true);
            return false;
        }

        return true;
    }

    private void performSave() {
        try {
            String vEmail = email.getValue().trim();
            String vName = name.getValue().trim();
            String rawPassword = password.getValue() == null ? "" : password.getValue();

            Set<String> roles = new HashSet<>(user.getRoles());
            if (roles.isEmpty()) {
                roles.add(SecurityConstants.ROLE_USER);
            }

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
    }
}
