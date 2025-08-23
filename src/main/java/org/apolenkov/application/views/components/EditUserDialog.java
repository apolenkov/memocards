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

/**
 * Dialog component for editing existing user accounts.
 * Provides administrators with the ability to modify user information
 * including email, name, roles, and password with comprehensive validation.
 */
public class EditUserDialog extends Dialog {

    /**
     * Callback interface for handling successful user updates.
     * Provides a mechanism for the parent component to respond
     * when a user is successfully updated through the dialog.
     */
    public interface OnSaved {
        /**
         * Called when a user is successfully saved.
         *
         * @param saved the updated user object with new values
         */
        void handle(User saved);
    }

    private final transient AdminUserService service;
    private final transient User user;
    private final transient OnSaved onSaved;
    private EmailField email;
    private TextField name;
    private TextField password;

    /**
     * Creates a new EditUserDialog for the specified user.
     * Initializes the dialog with the user's current information
     * and sets up the form layout with appropriate validation rules.
     *
     * @param service the admin user service for performing updates
     * @param user the user object to edit
     * @param onSaved callback to execute when the user is successfully saved
     */
    public EditUserDialog(AdminUserService service, User user, OnSaved onSaved) {
        this.service = service;
        this.user = user;
        this.onSaved = onSaved;

        setHeaderTitle(getTranslation("user.edit.title"));
        createButtons();
        add(createFormLayout());
        addClassName("dialog-md");
    }

    /**
     * Creates the form layout with all input fields.
     * Sets up form fields with proper validation, placeholders,
     * and helper text to guide administrators during user editing.
     *
     * @return a configured FormLayout containing all input fields
     */
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

    /**
     * Creates and configures the dialog buttons.
     * Sets up save and cancel buttons with appropriate styling
     * and event handlers for form submission and dialog closure.
     */
    private void createButtons() {
        Button save = new Button(getTranslation("dialog.save"), e -> handleSave());
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    /**
     * Handles the save button click event.
     * Performs validation on all form fields and, if successful,
     * attempts to save the updated user information. Displays appropriate
     * error messages for validation failures.
     */
    private void handleSave() {
        clearValidationErrors();
        populateFieldValues();

        if (validateAllFields()) {
            performSave();
        } else {
            Notification.show(getTranslation("auth.validation.fixErrors"));
        }
    }

    /**
     * Clears all validation error states from form fields.
     * Resets the visual error indicators on all input fields
     * before performing new validation.
     */
    private void clearValidationErrors() {
        email.setInvalid(false);
        name.setInvalid(false);
        password.setInvalid(false);
    }

    /**
     * Populates form fields with current user data.
     * Fills the email and name fields with the user's existing
     * information, handling null values gracefully.
     */
    private void populateFieldValues() {
        email.setValue(user.getEmail() == null ? "" : user.getEmail());
        name.setValue(user.getName() == null ? "" : user.getName());
    }

    /**
     * Validates all form fields for correctness.
     * Performs comprehensive validation on email, name, and password
     * fields, setting appropriate error states and messages.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        boolean emailValid = validateEmail();
        boolean nameValid = validateName();
        boolean passwordValid = validatePassword();

        return emailValid && nameValid && passwordValid;
    }

    /**
     * Validates the email field for format and presence.
     * Ensures the email field contains a valid email address
     * and is not empty.
     *
     * @return true if the email is valid, false otherwise
     */
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

    /**
     * Validates the name field for presence.
     * Ensures the name field is not empty and contains
     * meaningful content.
     *
     * @return true if the name is valid, false otherwise
     */
    private boolean validateName() {
        String vName = name.getValue() == null ? "" : name.getValue().trim();

        if (vName.isEmpty()) {
            name.setErrorMessage(getTranslation("auth.validation.nameRequired"));
            name.setInvalid(true);
            return false;
        }

        return true;
    }

    /**
     * Validates the password field for strength requirements.
     * If a password is provided, ensures it meets the minimum
     * security requirements. Empty passwords are allowed for
     * optional password changes.
     *
     * @return true if the password is valid or empty, false otherwise
     */
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

    /**
     * Performs the actual save operation for the updated user.
     * Collects validated form data and calls the admin service
     * to update the user with audit trail information. Handles
     * both successful updates and error conditions.
     */
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
