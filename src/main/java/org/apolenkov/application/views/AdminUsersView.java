package org.apolenkov.application.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.model.User;
import org.apolenkov.application.service.user.AdminUserService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.GridHelper;
import org.apolenkov.application.views.utils.TextHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Administrative view for managing user accounts and roles.
 *
 * <p>This view provides administrators with a comprehensive interface for managing
 * user accounts, including viewing user information, modifying user roles, editing
 * user details, and deleting user accounts. It implements a full user management
 * system with proper security controls and audit logging.</p>
 *
 * <p>The view includes the following features:</p>
 * <ul>
 *   <li><strong>User Management:</strong> View, edit, and delete user accounts</li>
 *   <li><strong>Role Management:</strong> Modify user roles with real-time updates</li>
 *   <li><strong>Data Grid:</strong> Tabular display of all users with sortable columns</li>
 *   <li><strong>Inline Editing:</strong> Direct role modification through checkboxes</li>
 *   <li><strong>Security Controls:</strong> Prevents deletion of last admin or self</li>
 *   <li><strong>Audit Logging:</strong> Tracks all role changes for compliance</li>
 *   <li><strong>Session Management:</strong> Updates current user session when modified</li>
 * </ul>
 *
 * <p><strong>Security Features:</strong></p>
 * <ul>
 *   <li>Requires ROLE_ADMIN authentication</li>
 *   <li>Prevents self-deletion of administrators</li>
 *   <li>Prevents deletion of the last administrator</li>
 *   <li>Audit logging of all role changes</li>
 *   <li>Session invalidation for security</li>
 * </ul>
 *
 * <p><strong>Route Configuration:</strong></p>
 * <ul>
 *   <li>Route: "/admin/users"</li>
 *   <li>Uses PublicLayout for consistent styling</li>
 *   <li>Restricted to administrative users only</li>
 * </ul>
 *
 * @see User
 * @see AdminUserService
 * @see PublicLayout
 * @see HasDynamicTitle
 * @see Route
 * @see RolesAllowed
 * @see VerticalLayout
 */
@Route(value = "admin/users", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminUsersView extends VerticalLayout implements HasDynamicTitle {

    private static final String TITLE_ATTRIBUTE = "title";
    private static final String ROLE_USER_TRANSLATION_KEY = "admin.users.role.USER";
    private static final String ROLE_ADMIN_TRANSLATION_KEY = "admin.users.role.ADMIN";

    private final transient AdminUserService adminUserService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    /**
     * Constructs a new AdminUsersView with the required service dependency.
     *
     * <p>This constructor initializes the administrative user management interface.
     * It sets up the layout, creates the user grid, and configures all interactive
     * elements including buttons, forms, and data display.</p>
     *
     * <p>The initialization process includes:</p>
     * <ul>
     *   <li><strong>Security Validation:</strong> Ensures current user has admin privileges</li>
     *   <li><strong>Layout Setup:</strong> Configures padding, spacing, and CSS classes</li>
     *   <li><strong>Page Title:</strong> Creates localized page heading</li>
     *   <li><strong>Create Button:</strong> Button for creating new user accounts</li>
     *   <li><strong>User Grid:</strong> Data grid with user information and actions</li>
     *   <li><strong>Data Population:</strong> Loads user data from the service</li>
     * </ul>
     *
     * <p><strong>Security Guards:</strong></p>
     * <ul>
     *   <li>Explicit role checking to prevent unauthorized access</li>
     *   <li>Redirect to access denied page for non-administrators</li>
     *   <li>Handles security context exceptions gracefully</li>
     * </ul>
     *
     * @param adminUserService the service for administrative user operations
     * @throws IllegalArgumentException if adminUserService is null
     * @see AdminUserService
     * @see CreateUserDialog
     * @see SecurityContextHolder
     */
    public AdminUsersView(AdminUserService adminUserService) {
        if (adminUserService == null) {
            throw new IllegalArgumentException("AdminUserService cannot be null");
        }

        this.adminUserService = adminUserService;

        setPadding(false);
        setSpacing(false);
        addClassName("admin-view");

        // Guard: explicit redirect for non-admins to avoid blank page in dev mode
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication();
            boolean isAdmin =
                    auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!isAdmin) {
                getUI().ifPresent(ui -> ui.navigate("access-denied"));
                return;
            }
        } catch (Exception ignored) {
            getUI().ifPresent(ui -> ui.navigate("access-denied"));
            return;
        }

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        H2 title = TextHelper.createPageTitle(getTranslation("admin.users.page.title"));
        Button createBtn = ButtonHelper.createPlusButton(e ->
                new org.apolenkov.application.views.components.CreateUserDialog(adminUserService, saved -> refresh())
                        .open());
        createBtn.setText(getTranslation("user.create.title"));
        content.add(title, createBtn);

        add(content);
        build(content);
        refresh();
    }

    /**
     * Builds the user management interface components.
     *
     * <p>This method configures the user data grid with all necessary columns
     * and features. It sets up the grid structure, adds columns for user data,
     * and configures the actions column for user management operations.</p>
     *
     * <p>The grid configuration includes:</p>
     * <ul>
     *   <li><strong>Common Features:</strong> Standard grid functionality and styling</li>
     *   <li><strong>Data Columns:</strong> ID, email, name, and roles display</li>
     *   <li><strong>Actions Column:</strong> Edit, delete, and role management buttons</li>
     *   <li><strong>Responsive Layout:</strong> Column sizing and alignment</li>
     * </ul>
     *
     * @param content the content layout to add the grid to
     * @see GridHelper
     * @see Grid
     * @see User
     */
    private void build(VerticalLayout content) {
        GridHelper.addCommonFeatures(grid);
        GridHelper.addTextColumn(grid, getTranslation("admin.users.columns.id"), u -> String.valueOf(u.getId()), 1);
        GridHelper.addTextColumn(grid, getTranslation("admin.users.columns.email"), User::getEmail, 2);
        GridHelper.addTextColumn(grid, getTranslation("admin.users.columns.name"), User::getName, 2);
        GridHelper.addTextColumn(
                grid,
                getTranslation("admin.users.columns.roles"),
                u -> u.getRoles().stream().map(this::roleToLabel).collect(Collectors.joining(", ")),
                2);
        GridHelper.addActionsColumn(grid, getTranslation("admin.users.columns.actions"), this::buildActionsArray);
        content.add(grid);
    }

    /**
     * Creates an array of action buttons for a user row.
     *
     * <p>This method creates the action buttons that appear in each user row
     * of the grid. It includes buttons for saving role changes, editing user
     * details, and deleting user accounts.</p>
     *
     * <p>The action buttons include:</p>
     * <ul>
     *   <li><strong>Save Button:</strong> Saves role changes with confirmation</li>
     *   <li><strong>Edit Button:</strong> Opens user editing dialog</li>
     *   <li><strong>Delete Button:</strong> Deletes user account with confirmation</li>
     * </ul>
     *
     * @param user the user for whom to create action buttons
     * @return an array of action buttons
     * @see Button
     * @see CheckboxGroup
     * @see createRolesCheckbox(User)
     * @see createSaveButton(User, CheckboxGroup)
     * @see createEditButton(User)
     * @see createDeleteButton(User)
     */
    private Button[] buildActionsArray(User user) {
        CheckboxGroup<String> rolesBox = createRolesCheckbox(user);
        Button saveButton = createSaveButton(user, rolesBox);
        Button editButton = createEditButton(user);
        Button deleteButton = createDeleteButton(user);

        return new Button[] {saveButton, editButton, deleteButton};
    }

    /**
     * Creates a horizontal layout with action buttons and role selection.
     *
     * <p>This method creates a more complex layout that includes both role
     * selection checkboxes and action buttons in a horizontal arrangement.
     * It provides better visual organization for the user management interface.</p>
     *
     * <p>The layout structure:</p>
     * <ul>
     *   <li><strong>Roles Section:</strong> Checkbox group for role selection</li>
     *   <li><strong>Actions Section:</strong> Horizontal button layout</li>
     *   <li><strong>Responsive Design:</strong> Flexible sizing and alignment</li>
     * </ul>
     *
     * @param user the user for whom to create the action layout
     * @return a horizontal layout containing roles and actions
     * @see HorizontalLayout
     * @see CheckboxGroup
     * @see Button
     * @see FlexComponent.JustifyContentMode
     */
    private HorizontalLayout buildActions(User user) {
        CheckboxGroup<String> rolesBox = createRolesCheckbox(user);
        Button saveButton = createSaveButton(user, rolesBox);
        Button editButton = createEditButton(user);
        Button deleteButton = createDeleteButton(user);

        HorizontalLayout buttons = new HorizontalLayout(saveButton, editButton, deleteButton);
        buttons.setWidthFull();
        buttons.setSpacing(true);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setAlignItems(Alignment.CENTER);

        HorizontalLayout row = new HorizontalLayout(rolesBox, buttons);
        row.setWidthFull();
        row.setSpacing(true);
        row.setAlignItems(Alignment.CENTER);
        row.setFlexGrow(0, rolesBox);
        row.setFlexGrow(1, buttons);
        return row;
    }

    /**
     * Creates a checkbox group for role selection.
     *
     * <p>This method creates a checkbox group that allows administrators to
     * modify user roles directly from the grid. It maps role constants to
     * localized labels and maintains the current user's role selection.</p>
     *
     * <p>The checkbox configuration includes:</p>
     * <ul>
     *   <li><strong>Role Mapping:</strong> Maps role constants to localized labels</li>
     *   <li><strong>Current Selection:</strong> Pre-selects user's current roles</li>
     *   <li><strong>Styling:</strong> Applies CSS classes for consistent appearance</li>
     *   <li><strong>Localization:</strong> Uses i18n keys for role labels</li>
     * </ul>
     *
     * @param user the user whose roles to display in the checkbox group
     * @return a configured checkbox group for role selection
     * @see CheckboxGroup
     * @see SecurityConstants
     * @see roleToLabel(String)
     * @see getTranslation(String)
     */
    private CheckboxGroup<String> createRolesCheckbox(User user) {
        Map<String, String> labelToRole = new LinkedHashMap<>();
        labelToRole.put(getTranslation(ROLE_USER_TRANSLATION_KEY), SecurityConstants.ROLE_USER);
        labelToRole.put(getTranslation(ROLE_ADMIN_TRANSLATION_KEY), SecurityConstants.ROLE_ADMIN);

        CheckboxGroup<String> rolesBox = new CheckboxGroup<>();
        rolesBox.setItems(labelToRole.keySet());
        rolesBox.addClassName("admin-users__roles-box");
        rolesBox.setLabel(getTranslation("admin.users.columns.roles"));
        rolesBox.setValue(user.getRoles().stream().map(this::roleToLabel).collect(Collectors.toSet()));
        return rolesBox;
    }

    /**
     * Creates a save button for role changes.
     *
     * <p>This method creates a save button that allows administrators to
     * save role changes for a user. The button includes proper styling,
     * tooltips, and click handlers for the save operation.</p>
     *
     * <p>The button features include:</p>
     * <ul>
     *   <li><strong>Icon:</strong> Check mark icon for save action</li>
     *   <li><strong>Styling:</strong> Primary and tertiary theme variants</li>
     *   <li><strong>Tooltip:</strong> Localized save button text</li>
     *   <li><strong>Click Handler:</strong> Opens save confirmation dialog</li>
     * </ul>
     *
     * @param user the user whose roles are being modified
     * @param rolesBox the checkbox group containing role selections
     * @return a configured save button
     * @see Button
     * @see VaadinIcon#CHECK
     * @see ButtonVariant
     * @see showSaveConfirmationDialog(User, CheckboxGroup)
     */
    private Button createSaveButton(User user, CheckboxGroup<String> rolesBox) {
        Button save = new Button(VaadinIcon.CHECK.create(), e -> showSaveConfirmationDialog(user, rolesBox));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        save.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("dialog.save"));
        return save;
    }

    /**
     * Creates an edit button for user modification.
     *
     * <p>This method creates an edit button that opens a dialog for editing
     * user details. The button includes proper styling, tooltips, and
     * click handlers for the edit operation.</p>
     *
     * <p>The button features include:</p>
     * <ul>
     *   <li><strong>Icon:</strong> Edit icon for edit action</li>
     *   <li><strong>Styling:</strong> Tertiary theme variant for subtle appearance</li>
     *   <li><strong>Tooltip:</strong> Localized edit button text</li>
     *   <li><strong>Click Handler:</strong> Opens user editing dialog</li>
     * </ul>
     *
     * @param user the user to edit
     * @return a configured edit button
     * @see Button
     * @see VaadinIcon#EDIT
     * @see ButtonVariant
     * @see EditUserDialog
     * @see updateCurrentSessionIfSelf(User)
     */
    private Button createEditButton(User user) {
        Button edit =
                new Button(VaadinIcon.EDIT.create(), e -> new org.apolenkov.application.views.components.EditUserDialog(
                                adminUserService, user, saved -> {
                                    updateCurrentSessionIfSelf(saved);
                                    refresh();
                                })
                        .open());
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        edit.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("dialog.edit"));
        return edit;
    }

    /**
     * Creates a delete button for user removal.
     *
     * <p>This method creates a delete button that allows administrators to
     * delete user accounts. The button includes proper styling, tooltips,
     * and click handlers for the delete operation.</p>
     *
     * <p>The button features include:</p>
     * <ul>
     *   <li><strong>Icon:</strong> Trash icon for delete action</li>
     *   <li><strong>Styling:</strong> Error theme variant to indicate destructive action</li>
     *   <li><strong>Tooltip:</strong> Localized delete button text</li>
     *   <li><strong>Click Handler:</strong> Opens delete confirmation dialog</li>
     * </ul>
     *
     * @param user the user to delete
     * @return a configured delete button
     * @see Button
     * @see VaadinIcon#TRASH
     * @see ButtonVariant
     * @see showDeleteConfirmationDialog(User)
     */
    private Button createDeleteButton(User user) {
        Button delete = new Button(VaadinIcon.TRASH.create(), e -> showDeleteConfirmationDialog(user));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        delete.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("dialog.delete"));
        return delete;
    }

    /**
     * Shows a confirmation dialog for saving role changes.
     *
     * <p>This method displays a confirmation dialog before applying role
     * changes to a user. It validates that at least one role is selected
     * and shows appropriate error messages if validation fails.</p>
     *
     * <p>The confirmation process includes:</p>
     * <ul>
     *   <li><strong>Validation:</strong> Ensures at least one role is selected</li>
     *   <li><strong>Role Mapping:</strong> Converts display labels to role constants</li>
     *   <li><strong>Confirmation Dialog:</strong> Shows confirmation before applying changes</li>
     *   <li><strong>Error Handling:</strong> Displays validation error messages</li>
     * </ul>
     *
     * @param user the user whose roles are being modified
     * @param rolesBox the checkbox group containing role selections
     * @see Dialog
     * @see Notification
     * @see saveUserRoles(User, Set, Dialog)
     * @see getTranslation(String)
     */
    private void showSaveConfirmationDialog(User user, CheckboxGroup<String> rolesBox) {
        Set<String> selected = rolesBox.getValue();
        if (selected == null || selected.isEmpty()) {
            Notification.show(getTranslation("admin.users.error.rolesRequired"));
            return;
        }

        Map<String, String> labelToRole = Map.of(
                getTranslation(ROLE_USER_TRANSLATION_KEY), SecurityConstants.ROLE_USER,
                getTranslation(ROLE_ADMIN_TRANSLATION_KEY), SecurityConstants.ROLE_ADMIN);
        Set<String> roles = selected.stream().map(labelToRole::get).collect(Collectors.toCollection(HashSet::new));

        Dialog confirm = new Dialog();
        confirm.add(new Span(getTranslation("admin.users.confirm.apply")));
        Button ok = new Button(VaadinIcon.CHECK.create(), ev -> saveUserRoles(user, roles, confirm));
        Button cancel = new Button(VaadinIcon.CLOSE_SMALL.create(), ev -> confirm.close());
        confirm.add(new HorizontalLayout(ok, cancel));
        confirm.open();
    }

    /**
     * Saves user role changes with audit logging.
     *
     * <p>This method applies the selected role changes to a user account
     * and logs the changes in the audit system. It also updates the current
     * user's session if they are modifying their own account.</p>
     *
     * <p>The save process includes:</p>
     * <ul>
     *   <li><strong>Audit Logging:</strong> Records role changes with admin details</li>
     *   <li><strong>Session Update:</strong> Updates current user session if modified</li>
     *   <li><strong>User Feedback:</strong> Shows success/error notifications</li>
     *   <li><strong>Data Refresh:</strong> Updates the grid with new data</li>
     * </ul>
     *
     * @param user the user whose roles are being modified
     * @param roles the new set of roles to assign
     * @param confirm the confirmation dialog to close after saving
     * @see AdminUserService#updateRolesWithAudit(String, Long, Set)
     * @see updateCurrentSessionIfSelf(User)
     * @see Notification
     * @see refresh()
     */
    private void saveUserRoles(User user, Set<String> roles, Dialog confirm) {
        try {
            String adminEmail =
                    SecurityContextHolder.getContext().getAuthentication().getName();
            adminUserService.updateRolesWithAudit(adminEmail, user.getId(), roles);
            adminUserService.getById(user.getId()).ifPresent(this::updateCurrentSessionIfSelf);
            Notification.show(getTranslation("admin.users.updated"), 1500, Notification.Position.BOTTOM_START);
            confirm.close();
            refresh();
        } catch (Exception ex) {
            Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
        }
    }

    /**
     * Shows a confirmation dialog for deleting a user.
     *
     * <p>This method displays a confirmation dialog before deleting a user
     * account. It provides a clear warning about the destructive action
     * and requires explicit user confirmation.</p>
     *
     * <p>The confirmation dialog includes:</p>
     * <ul>
     *   <li><strong>Warning Message:</strong> Clear indication of destructive action</li>
     *   <li><strong>Confirmation Buttons:</strong> OK and cancel options</li>
     *   <li><strong>Icon Usage:</strong> Appropriate icons for each action</li>
     *   <li><strong>User Safety:</strong> Prevents accidental deletions</li>
     * </ul>
     *
     * @param user the user to delete
     * @see Dialog
     * @see deleteUser(User, Dialog)
     * @see getTranslation(String)
     */
    private void showDeleteConfirmationDialog(User user) {
        Dialog confirm = new Dialog();
        confirm.add(new Span(getTranslation("admin.users.confirm.delete")));
        Button ok = new Button(VaadinIcon.TRASH.create(), ev -> deleteUser(user, confirm));
        Button cancel = new Button(VaadinIcon.CLOSE_SMALL.create(), ev -> confirm.close());
        confirm.add(new HorizontalLayout(ok, cancel));
        confirm.open();
    }

    /**
     * Deletes a user account with safety checks.
     *
     * <p>This method deletes a user account after performing several safety
     * checks to prevent system compromise. It includes checks for self-deletion
     * and last administrator deletion.</p>
     *
     * <p>The safety checks include:</p>
     * <ul>
     *   <li><strong>Self-Deletion Prevention:</strong> Prevents administrators from deleting themselves</li>
     *   <li><strong>Last Admin Protection:</strong> Prevents deletion of the last administrator</li>
     *   <li><strong>Error Handling:</strong> Shows appropriate error messages for blocked operations</li>
     *   <li><strong>Data Refresh:</strong> Updates the grid after successful deletion</li>
     * </ul>
     *
     * @param user the user to delete
     * @param confirm the confirmation dialog to close after deletion
     * @see AdminUserService#delete(Long)
     * @see isCurrentAdminUser(String, User)
     * @see isLastAdmin(User)
     * @see Notification
     * @see refresh()
     */
    private void deleteUser(User user, Dialog confirm) {
        try {
            String current =
                    SecurityContextHolder.getContext().getAuthentication().getName();

            if (isCurrentAdminUser(current, user)) {
                Notification.show(getTranslation("admin.users.error.cannotDeleteSelfAdmin"));
                confirm.close();
                return;
            }

            adminUserService.getById(user.getId()).ifPresent(u -> {
                if (isLastAdmin(u)) {
                    Notification.show(getTranslation("admin.users.error.lastAdmin"));
                } else {
                    adminUserService.delete(u.getId());
                }
            });
            confirm.close();
            refresh();
        } catch (Exception ex) {
            Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
        }
    }

    /**
     * Checks if the current user is trying to delete themselves.
     *
     * <p>This method determines whether the current authenticated user
     * is attempting to delete their own account. This is a safety check
     * to prevent administrators from accidentally removing themselves
     * from the system.</p>
     *
     * <p>The check includes:</p>
     * <ul>
     *   <li><strong>Email Comparison:</strong> Compares current user email with target user</li>
     *   <li><strong>Role Verification:</strong> Ensures the target user is an administrator</li>
     *   <strong>Case Insensitive:</strong> Uses case-insensitive email comparison</li>
     * </ul>
     *
     * @param currentEmail the email of the currently authenticated user
     * @param user the user being considered for deletion
     * @return true if the current user is trying to delete themselves, false otherwise
     * @see SecurityConstants#ROLE_ADMIN
     */
    private boolean isCurrentAdminUser(String currentEmail, User user) {
        return currentEmail.equalsIgnoreCase(user.getEmail()) && user.getRoles().contains(SecurityConstants.ROLE_ADMIN);
    }

    /**
     * Checks if a user is the last administrator in the system.
     *
     * <p>This method determines whether deleting a user would result in
     * the system having no administrators. This is a critical safety check
     * to prevent the system from becoming unmanageable.</p>
     *
     * <p>The check includes:</p>
     * <ul>
     *   <li><strong>Admin Count:</strong> Counts total administrators in the system</li>
     *   <li><strong>User Role Check:</strong> Verifies the target user is an administrator</li>
     *   <strong>Threshold Protection:</strong> Prevents deletion if count would drop below 1</li>
     * </ul>
     *
     * @param user the user being considered for deletion
     * @return true if this user is the last administrator, false otherwise
     * @see AdminUserService#listAll()
     * @see SecurityConstants#ROLE_ADMIN
     */
    private boolean isLastAdmin(User user) {
        return adminUserService.listAll().stream()
                                .filter(x -> x.getRoles().contains(SecurityConstants.ROLE_ADMIN))
                                .count()
                        <= 1
                && user.getRoles().contains(SecurityConstants.ROLE_ADMIN);
    }

    /**
     * Converts a role constant to a localized display label.
     *
     * <p>This method converts internal role constants (e.g., "ROLE_ADMIN")
     * to user-friendly, localized display labels. It handles the "ROLE_"
     * prefix removal and maps the normalized role names to appropriate
     * translation keys.</p>
     *
     * <p>The conversion process includes:</p>
     * <ul>
     *   <li><strong>Prefix Removal:</strong> Strips "ROLE_" prefix from role constants</li>
     *   <li><strong>Role Mapping:</strong> Maps normalized roles to translation keys</li>
     *   <strong>Localization:</strong> Returns localized display text</li>
     *   <strong>Fallback Handling:</strong> Provides default behavior for unknown roles</li>
     * </ul>
     *
     * @param role the role constant to convert (e.g., "ROLE_ADMIN")
     * @return the localized display label for the role
     * @see getTranslation(String)
     * @see ROLE_USER_TRANSLATION_KEY
     * @see ROLE_ADMIN_TRANSLATION_KEY
     */
    private String roleToLabel(String role) {
        String normalized = role;
        if (normalized == null) return "";
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        if ("ADMIN".equalsIgnoreCase(normalized)) return getTranslation(ROLE_ADMIN_TRANSLATION_KEY);
        return getTranslation(ROLE_USER_TRANSLATION_KEY);
    }

    /**
     * Refreshes the user data displayed in the grid.
     *
     * <p>This method updates the grid with fresh user data from the service.
     * It's called after create, update, and delete operations to maintain
     * data consistency and ensure the display reflects the current state.</p>
     *
     * @see AdminUserService#listAll()
     * @see Grid#setItems(Collection)
     */
    private void refresh() {
        grid.setItems(adminUserService.listAll());
    }

    /**
     * Updates the current user's session if they modified their own account.
     *
     * <p>This method checks if the current authenticated user modified their
     * own account and updates their session accordingly. This ensures that
     * role changes take effect immediately without requiring re-authentication.</p>
     *
     * <p>The session update process includes:</p>
     * <ul>
     *   <li><strong>Identity Check:</strong> Verifies the modified user is the current user</li>
     *   <li><strong>Authority Update:</strong> Updates authentication authorities with new roles</li>
     *   <strong>Session Refresh:</strong> Updates the security context with new authentication</li>
     *   <strong>Page Reload:</strong> Reloads the page to apply all changes</li>
     * </ul>
     *
     * <p><strong>Security Considerations:</strong></p>
     * <ul>
     *   <li>Only updates session for the current user</li>
     *   <li>Maintains authentication integrity</li>
     *   <li>Handles security context exceptions gracefully</li>
     * </ul>
     *
     * @param updated the updated user object
     * @see SecurityContextHolder
     * @see UsernamePasswordAuthenticationToken
     * @see SimpleGrantedAuthority
     * @see UI#getCurrent()
     */
    private void updateCurrentSessionIfSelf(User updated) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return;
            String currentEmail = auth.getName();
            if (!updated.getEmail().equalsIgnoreCase(currentEmail)) return;
            var newAuthorities =
                    updated.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
            var newAuth =
                    new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), newAuthorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            UI.getCurrent().getPage().reload();
        } catch (Exception ignored) {
            // Will skip
        }
    }

    /**
     * Gets the page title for this view.
     *
     * <p>This method implements the {@link HasDynamicTitle} interface to provide
     * a dynamic page title that reflects the current view's purpose. The title
     * is retrieved from the internationalization system to ensure proper
     * localization.</p>
     *
     * @return the localized page title for the admin users view
     * @see HasDynamicTitle#getPageTitle()
     * @see #getTranslation(String)
     */
    @Override
    public String getPageTitle() {
        return getTranslation("admin.users.page.title");
    }
}
