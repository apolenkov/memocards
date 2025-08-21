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

@Route(value = "admin/users", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminUsersView extends VerticalLayout implements HasDynamicTitle {

    private static final String TITLE_ATTRIBUTE = "title";
    private static final String ROLE_USER_TRANSLATION_KEY = "admin.users.role.USER";
    private static final String ROLE_ADMIN_TRANSLATION_KEY = "admin.users.role.ADMIN";

    private final transient AdminUserService adminUserService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public AdminUsersView(AdminUserService adminUserService) {
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

    private Button[] buildActionsArray(User user) {
        CheckboxGroup<String> rolesBox = createRolesCheckbox(user);
        Button saveButton = createSaveButton(user, rolesBox);
        Button editButton = createEditButton(user);
        Button deleteButton = createDeleteButton(user);

        return new Button[] {saveButton, editButton, deleteButton};
    }

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

    private Button createSaveButton(User user, CheckboxGroup<String> rolesBox) {
        Button save = new Button(VaadinIcon.CHECK.create(), e -> showSaveConfirmationDialog(user, rolesBox));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        save.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("dialog.save"));
        return save;
    }

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

    private Button createDeleteButton(User user) {
        Button delete = new Button(VaadinIcon.TRASH.create(), e -> showDeleteConfirmationDialog(user));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        delete.getElement().setAttribute(TITLE_ATTRIBUTE, getTranslation("dialog.delete"));
        return delete;
    }

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

    private void showDeleteConfirmationDialog(User user) {
        Dialog confirm = new Dialog();
        confirm.add(new Span(getTranslation("admin.users.confirm.delete")));
        Button ok = new Button(VaadinIcon.TRASH.create(), ev -> deleteUser(user, confirm));
        Button cancel = new Button(VaadinIcon.CLOSE_SMALL.create(), ev -> confirm.close());
        confirm.add(new HorizontalLayout(ok, cancel));
        confirm.open();
    }

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

    private boolean isCurrentAdminUser(String currentEmail, User user) {
        return currentEmail.equalsIgnoreCase(user.getEmail()) && user.getRoles().contains(SecurityConstants.ROLE_ADMIN);
    }

    private boolean isLastAdmin(User user) {
        return adminUserService.listAll().stream()
                                .filter(x -> x.getRoles().contains(SecurityConstants.ROLE_ADMIN))
                                .count()
                        <= 1
                && user.getRoles().contains(SecurityConstants.ROLE_ADMIN);
    }

    private String roleToLabel(String role) {
        String normalized = role;
        if (normalized == null) return "";
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        if ("ADMIN".equalsIgnoreCase(normalized)) return getTranslation(ROLE_ADMIN_TRANSLATION_KEY);
        return getTranslation(ROLE_USER_TRANSLATION_KEY);
    }

    private void refresh() {
        grid.setItems(adminUserService.listAll());
    }

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

    @Override
    public String getPageTitle() {
        return getTranslation("admin.users.page.title");
    }
}
