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
                .setAutoWidth(true);
        add(grid);
    }

    private HorizontalLayout buildActions(User user) {
        Map<String, String> labelToRole = new LinkedHashMap<>();
        labelToRole.put("USER", SecurityConstants.ROLE_USER);
        labelToRole.put("ADMIN", SecurityConstants.ROLE_ADMIN);

        CheckboxGroup<String> rolesBox = new CheckboxGroup<>();
        rolesBox.setItems(labelToRole.keySet());
        rolesBox.setWidth("260px");
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
                    String adminEmail = getUI().map(ui -> ui.getSession().getAttribute("SPRING_SECURITY_CONTEXT"))
                            .map(ctx -> org.springframework.security.core.context.SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName())
                            .orElse("unknown");
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

        Button makeAdmin = new Button(getTranslation("admin.users.actions.makeAdmin"), e -> {
            Set<String> roles = new HashSet<>(user.getRoles());
            roles.add(SecurityConstants.ROLE_ADMIN);
            adminUserService.updateRoles(user.getId(), roles);
            refresh();
        });

        Button makeUser = new Button(getTranslation("admin.users.actions.makeUser"), e -> {
            Set<String> roles = new HashSet<>(user.getRoles());
            roles.remove(SecurityConstants.ROLE_ADMIN);
            roles.add(SecurityConstants.ROLE_USER);
            adminUserService.updateRoles(user.getId(), roles);
            refresh();
        });

        return new HorizontalLayout(rolesBox, save, makeAdmin, makeUser);
    }

    private void refresh() {
        grid.setItems(adminUserService.listAll());
    }
}
