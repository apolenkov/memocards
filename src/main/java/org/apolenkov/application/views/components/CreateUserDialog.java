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

    private final transient AdminUserService service;
    private final transient OnSaved onSaved;
    private final LinkedHashMap<String, String> labelToRole = new LinkedHashMap<>();

    private EmailField email;
    private TextField name;
    private PasswordField password;
    private CheckboxGroup<String> rolesBox;

    public CreateUserDialog(AdminUserService service, OnSaved onSaved) {
        this.service = service;
        this.onSaved = onSaved;

        initializeDialog();
        createFormFields();
        setupButtons();
    }

    private void initializeDialog() {
        setHeaderTitle(getTranslation("user.create.title"));
        setWidth("520px");

        labelToRole.put(getTranslation("admin.users.role.USER"), SecurityConstants.ROLE_USER);
        labelToRole.put(getTranslation("admin.users.role.ADMIN"), SecurityConstants.ROLE_ADMIN);
    }

    private void createFormFields() {
        FormLayout form = new FormLayout();

        email = createEmailField();
        name = createNameField();
        password = createPasswordField();
        rolesBox = createRolesCheckbox();

        form.add(email, name, password, rolesBox);
        add(form);
    }

    private EmailField createEmailField() {
        EmailField field = new EmailField(getTranslation("auth.email"));
        field.setPlaceholder(getTranslation("auth.email.placeholder"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("auth.email.helper"));
        return field;
    }

    private TextField createNameField() {
        TextField field = new TextField(getTranslation("auth.name"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("auth.name.helper"));
        return field;
    }

    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField(getTranslation("auth.password"));
        field.setPlaceholder(getTranslation("auth.password.placeholder"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("user.password.helper"));
        return field;
    }

    private CheckboxGroup<String> createRolesCheckbox() {
        CheckboxGroup<String> checkbox = new CheckboxGroup<>();
        checkbox.setLabel(getTranslation("admin.users.columns.roles"));
        checkbox.setItems(labelToRole.keySet());
        checkbox.setValue(Set.of(getTranslation("admin.users.role.USER")));
        return checkbox;
    }

    private void setupButtons() {
        Button save = new Button(getTranslation("dialog.save"), e -> handleSave());
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    private void handleSave() {
        clearValidationErrors();

        if (!validateAllFields()) {
            Notification.show(getTranslation("auth.validation.fixErrors"));
            return;
        }

        performSaveOperation();
    }

    private void clearValidationErrors() {
        email.setInvalid(false);
        name.setInvalid(false);
        password.setInvalid(false);
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        isValid &= validateEmail();
        isValid &= validateName();
        isValid &= validatePassword();
        return isValid;
    }

    private boolean validateEmail() {
        String vEmail = getTrimmedValue(email.getValue());
        if (vEmail.isEmpty() || !vEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
            email.setInvalid(true);
            return false;
        }
        return true;
    }

    private boolean validateName() {
        String vName = getTrimmedValue(name.getValue());
        if (vName.length() < 2) {
            name.setErrorMessage(getTranslation("auth.validation.nameMin2"));
            name.setInvalid(true);
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String vPwd = password.getValue() == null ? "" : password.getValue();
        if (vPwd.length() < 8 || !vPwd.matches(".*[A-Za-z].*") || !vPwd.matches(".*\\d.*")) {
            password.setErrorMessage(getTranslation("auth.validation.passwordPolicy"));
            password.setInvalid(true);
            return false;
        }
        return true;
    }

    private void performSaveOperation() {
        try {
            Set<String> roles = convertSelectedRoles();
            if (roles.isEmpty()) {
                Notification.show(getTranslation("admin.users.error.rolesRequired"));
                return;
            }

            String adminEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            User saved = service.createUserWithAudit(
                    adminEmail,
                    getTrimmedValue(email.getValue()),
                    getTrimmedValue(name.getValue()),
                    password.getValue(),
                    roles);

            if (onSaved != null) {
                onSaved.handle(saved);
            }
            close();
        } catch (Exception ex) {
            Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
        }
    }

    private Set<String> convertSelectedRoles() {
        Set<String> roles = new HashSet<>();
        if (rolesBox.getValue() != null) {
            for (String label : rolesBox.getValue()) {
                String role = labelToRole.get(label);
                if (role != null) {
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    private String getTrimmedValue(String value) {
        return value == null ? "" : value.trim();
    }
}
