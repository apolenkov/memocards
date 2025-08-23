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

/**
 * Dialog component for creating new user accounts.
 *
 * <p>This dialog provides administrators with the ability to create new
 * user accounts with comprehensive validation and role assignment.
 * It ensures all new users meet security requirements and have
 * appropriate access permissions.</p>
 *
 * <p>The dialog features:</p>
 * <ul>
 *   <li>Required fields for email, name, and password</li>
 *   <li>Real-time validation with user feedback</li>
 *   <li>Role selection with checkbox group interface</li>
 *   <li>Password strength requirements enforcement</li>
 *   <li>Comprehensive error handling and validation</li>
 * </ul>
 *
 * <p>All user creation operations are tracked with administrator
 * identification for audit and security purposes.</p>
 */
public class CreateUserDialog extends Dialog {

    /**
     * Callback interface for handling successful user creation.
     *
     * <p>Provides a mechanism for the parent component to respond
     * when a new user is successfully created through the dialog.</p>
     */
    public interface OnSaved {
        /**
         * Called when a user is successfully created.
         *
         * @param saved the newly created user object
         */
        void handle(User saved);
    }

    private final transient AdminUserService service;
    private final transient OnSaved onSaved;
    private final LinkedHashMap<String, String> labelToRole = new LinkedHashMap<>();

    private EmailField email;
    private TextField name;
    private PasswordField password;
    private CheckboxGroup<String> rolesBox;

    /**
     * Creates a new CreateUserDialog with required dependencies.
     *
     * <p>Initializes the dialog with form fields, role mapping, and
     * event handlers for user creation workflow.</p>
     *
     * @param service the admin user service for performing user creation
     * @param onSaved callback to execute when the user is successfully created
     */
    public CreateUserDialog(AdminUserService service, OnSaved onSaved) {
        this.service = service;
        this.onSaved = onSaved;

        initializeDialog();
        createFormFields();
        setupButtons();
    }

    /**
     * Initializes the dialog configuration and role mapping.
     *
     * <p>Sets up the dialog header, styling, and creates the mapping
     * between display labels and internal role constants.</p>
     */
    private void initializeDialog() {
        setHeaderTitle(getTranslation("user.create.title"));
        addClassName("dialog-md");

        labelToRole.put(getTranslation("admin.users.role.USER"), SecurityConstants.ROLE_USER);
        labelToRole.put(getTranslation("admin.users.role.ADMIN"), SecurityConstants.ROLE_ADMIN);
    }

    /**
     * Creates and configures all form input fields.
     *
     * <p>Sets up email, name, password, and role selection fields
     * with appropriate validation rules and helper text.</p>
     */
    private void createFormFields() {
        FormLayout form = new FormLayout();

        email = createEmailField();
        name = createNameField();
        password = createPasswordField();
        rolesBox = createRolesCheckbox();

        form.add(email, name, password, rolesBox);
        add(form);
    }

    /**
     * Creates and configures the email input field.
     *
     * <p>Sets up email validation, required indicators, and helper text
     * to guide administrators during user creation.</p>
     *
     * @return a configured EmailField for user email input
     */
    private EmailField createEmailField() {
        EmailField field = new EmailField(getTranslation("auth.email"));
        field.setPlaceholder(getTranslation("auth.email.placeholder"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("auth.email.helper"));
        return field;
    }

    /**
     * Creates and configures the name input field.
     *
     * <p>Sets up name validation, required indicators, and helper text
     * for the user's display name.</p>
     *
     * @return a configured TextField for user name input
     */
    private TextField createNameField() {
        TextField field = new TextField(getTranslation("auth.name"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("auth.name.helper"));
        return field;
    }

    /**
     * Creates and configures the password input field.
     *
     * <p>Sets up password validation, required indicators, and helper text
     * explaining password strength requirements.</p>
     *
     * @return a configured PasswordField for user password input
     */
    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField(getTranslation("auth.password"));
        field.setPlaceholder(getTranslation("auth.password.placeholder"));
        field.setWidthFull();
        field.setRequiredIndicatorVisible(true);
        field.setHelperText(getTranslation("user.password.helper"));
        return field;
    }

    /**
     * Creates and configures the role selection checkbox group.
     *
     * <p>Sets up role selection with available user and admin roles,
     * defaulting to user role for new accounts.</p>
     *
     * @return a configured CheckboxGroup for role selection
     */
    private CheckboxGroup<String> createRolesCheckbox() {
        CheckboxGroup<String> checkbox = new CheckboxGroup<>();
        checkbox.setLabel(getTranslation("admin.users.columns.roles"));
        checkbox.setItems(labelToRole.keySet());
        checkbox.setValue(Set.of(getTranslation("admin.users.role.USER")));
        return checkbox;
    }

    /**
     * Creates and configures the dialog action buttons.
     *
     * <p>Sets up save and cancel buttons with appropriate event handlers
     * for form submission and dialog closure.</p>
     */
    private void setupButtons() {
        Button save = new Button(getTranslation("dialog.save"), e -> handleSave());
        Button cancel = new Button(getTranslation("common.cancel"), e -> close());
        getFooter().add(cancel, save);
    }

    /**
     * Handles the save button click event.
     *
     * <p>Performs validation on all form fields and, if successful,
     * attempts to create the new user. Displays appropriate error
     * messages for validation failures.</p>
     */
    private void handleSave() {
        clearValidationErrors();

        if (!validateAllFields()) {
            Notification.show(getTranslation("auth.validation.fixErrors"));
            return;
        }

        performSaveOperation();
    }

    /**
     * Clears all validation error states from form fields.
     *
     * <p>Resets the visual error indicators on all input fields
     * before performing new validation.</p>
     */
    private void clearValidationErrors() {
        email.setInvalid(false);
        name.setInvalid(false);
        password.setInvalid(false);
    }

    /**
     * Validates all form fields for correctness.
     *
     * <p>Performs comprehensive validation on email, name, and password
     * fields, setting appropriate error states and messages.</p>
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateAllFields() {
        boolean isValid = true;
        isValid &= validateEmail();
        isValid &= validateName();
        isValid &= validatePassword();
        return isValid;
    }

    /**
     * Validates the email field for format and presence.
     *
     * <p>Ensures the email field contains a valid email address
     * and is not empty.</p>
     *
     * @return true if the email is valid, false otherwise
     */
    private boolean validateEmail() {
        String vEmail = getTrimmedValue(email.getValue());
        if (vEmail.isEmpty() || !vEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
            email.setInvalid(true);
            return false;
        }
        return true;
    }

    /**
     * Validates the name field for minimum length requirements.
     *
     * <p>Ensures the name field contains at least 2 characters
     * for meaningful user identification.</p>
     *
     * @return true if the name meets length requirements, false otherwise
     */
    private boolean validateName() {
        String vName = getTrimmedValue(name.getValue());
        if (vName.length() < 2) {
            name.setErrorMessage(getTranslation("auth.validation.nameMin2"));
            name.setInvalid(true);
            return false;
        }
        return true;
    }

    /**
     * Validates the password field for strength requirements.
     *
     * <p>Ensures the password meets minimum security standards
     * including length, letter, and digit requirements.</p>
     *
     * @return true if the password meets strength requirements, false otherwise
     */
    private boolean validatePassword() {
        String vPwd = password.getValue() == null ? "" : password.getValue();
        if (vPwd.length() < 8 || !vPwd.matches(".*[A-Za-z].*") || !vPwd.matches(".*\\d.*")) {
            password.setErrorMessage(getTranslation("auth.validation.passwordPolicy"));
            password.setInvalid(true);
            return false;
        }
        return true;
    }

    /**
     * Performs the actual user creation operation.
     *
     * <p>Collects validated form data and calls the admin service
     * to create the new user with audit trail information. Handles
     * both successful creation and error conditions.</p>
     */
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

    /**
     * Converts selected role labels to internal role constants.
     *
     * <p>Maps the user-selected role display labels to the corresponding
     * internal role constants used by the security system.</p>
     *
     * @return a set of role constants for the selected roles
     */
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

    /**
     * Safely trims a string value, handling null cases.
     *
     * <p>Provides a utility method for safely trimming string values
     * while handling null inputs gracefully.</p>
     *
     * @param value the string value to trim
     * @return the trimmed string, or empty string if input is null
     */
    private String getTrimmedValue(String value) {
        return value == null ? "" : value.trim();
    }
}
