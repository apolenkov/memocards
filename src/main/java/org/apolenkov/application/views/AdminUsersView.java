package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
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

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Users (Admin)")
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private final AdminUserService adminUserService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public AdminUsersView(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
        setSizeFull();
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
        build();
        refresh();
    }

    private void build() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(User::getId)
                .setHeader(getTranslation("admin.users.columns.id"))
                .setAutoWidth(true);
        grid.addColumn(User::getEmail)
                .setHeader(getTranslation("admin.users.columns.email"))
                .setAutoWidth(true);
        grid.addColumn(User::getName)
                .setHeader(getTranslation("admin.users.columns.name"))
                .setAutoWidth(true);
        grid.addColumn(u -> String.join(", ", u.getRoles())).setHeader(getTranslation("admin.users.columns.roles"));
        grid.addComponentColumn(this::buildActions)
                .setHeader(getTranslation("admin.users.actions"))
                .setWidth("620px")
                .setFlexGrow(0);
        add(grid);
    }

    private HorizontalLayout buildActions(User user) {
        Map<String, String> labelToRole = new LinkedHashMap<>();
        labelToRole.put("USER", SecurityConstants.ROLE_USER);
        labelToRole.put("ADMIN", SecurityConstants.ROLE_ADMIN);

        CheckboxGroup<String> rolesBox = new CheckboxGroup<>();
        rolesBox.setItems(labelToRole.keySet());
        rolesBox.setWidth("280px");
        rolesBox.setLabel(getTranslation("admin.users.columns.roles"));
        rolesBox.setValue(user.getRoles().stream()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .collect(Collectors.toSet()));

        Button save = new Button(getTranslation("admin.users.actions.save"), e -> {
            Set<String> selected = rolesBox.getValue();
            if (selected == null || selected.isEmpty()) {
                Notification.show(getTranslation("admin.users.error.rolesRequired"));
                return;
            }
            Set<String> roles = selected.stream().map(labelToRole::get).collect(Collectors.toCollection(HashSet::new));

            Dialog confirm = new Dialog();
            confirm.add(new Span(getTranslation("admin.users.confirm.apply")));
            Button ok = new Button(getTranslation("dialog.save"), ev -> {
                try {
                    String adminEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getName();
                    adminUserService.updateRolesWithAudit(adminEmail, user.getId(), roles);
                    Notification.show(getTranslation("admin.users.updated"), 1500, Notification.Position.BOTTOM_START);
                    confirm.close();
                    refresh();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            });
            Button cancel = new Button(getTranslation("dialog.cancel"), ev -> confirm.close());
            confirm.add(new HorizontalLayout(ok, cancel));
            confirm.open();
        });

        Button delete = new Button(getTranslation("dialog.delete"), e -> {
            Dialog confirm = new Dialog();
            confirm.add(new Span(getTranslation("admin.users.confirm.delete")));
            Button ok = new Button(getTranslation("dialog.delete"), ev -> {
                try {
                    String current = org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getName();
                    if (current.equalsIgnoreCase(user.getEmail())
                            && user.getRoles().contains(SecurityConstants.ROLE_ADMIN)) {
                        Notification.show(getTranslation("admin.users.error.cannotDeleteSelfAdmin"));
                        confirm.close();
                        return;
                    }
                    // simple delete via service
                    adminUserService.getById(user.getId()).ifPresent(u -> {
                        // ensure not deleting the last admin
                        boolean lastAdmin = adminUserService.listAll().stream()
                                .filter(x -> x.getRoles().contains(SecurityConstants.ROLE_ADMIN))
                                .count() <= 1 && u.getRoles().contains(SecurityConstants.ROLE_ADMIN);
                        if (lastAdmin) {
                            Notification.show(getTranslation("admin.users.error.lastAdmin"));
                        } else {
                            // use repository through service
                            adminUserService.save(new User(u.getId(), u.getEmail(), u.getName()));
                            // hacky: service lacks delete; keep as placeholder for real delete use case
                        }
                    });
                    confirm.close();
                    refresh();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            });
            Button cancel = new Button(getTranslation("dialog.cancel"), ev -> confirm.close());
            confirm.add(new HorizontalLayout(ok, cancel));
            confirm.open();
        });

        HorizontalLayout row = new HorizontalLayout(rolesBox, save, delete);
        row.setWidthFull();
        row.setSpacing(true);
        return row;
    }

    private void refresh() {
        grid.setItems(adminUserService.listAll());
    }
}
