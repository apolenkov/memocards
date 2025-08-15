package org.apolenkov.application.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "admin/users", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminUsersView extends VerticalLayout implements HasDynamicTitle {

    private final AdminUserService adminUserService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public AdminUsersView(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

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

        H2 title = new H2(getTranslation("admin.users.page.title"));
        Button createBtn = new Button(getTranslation("user.create.title"), e -> {
            new org.apolenkov.application.views.components.CreateUserDialog(adminUserService, saved -> refresh())
                    .open();
        });
        content.add(title, createBtn);

        add(content);
        build(content);
        refresh();
    }

    private void build(VerticalLayout content) {
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
        grid.addColumn(u -> u.getRoles().stream().map(this::roleToLabel).collect(Collectors.joining(", ")))
                .setHeader(getTranslation("admin.users.columns.roles"));
        grid.addComponentColumn(this::buildActions)
                .setHeader(getTranslation("admin.users.columns.actions"))
                .setWidth("620px")
                .setFlexGrow(0);
        content.add(grid);
    }

    private HorizontalLayout buildActions(User user) {
        Map<String, String> labelToRole = new LinkedHashMap<>();
        labelToRole.put(getTranslation("admin.users.role.USER"), SecurityConstants.ROLE_USER);
        labelToRole.put(getTranslation("admin.users.role.ADMIN"), SecurityConstants.ROLE_ADMIN);

        CheckboxGroup<String> rolesBox = new CheckboxGroup<>();
        rolesBox.setItems(labelToRole.keySet());
        rolesBox.setWidth("280px");
        rolesBox.setLabel(getTranslation("admin.users.columns.roles"));
        rolesBox.setValue(user.getRoles().stream().map(this::roleToLabel).collect(Collectors.toSet()));

        Button save = new Button(VaadinIcon.CHECK.create(), e -> {
            Set<String> selected = rolesBox.getValue();
            if (selected == null || selected.isEmpty()) {
                Notification.show(getTranslation("admin.users.error.rolesRequired"));
                return;
            }
            Set<String> roles = selected.stream().map(labelToRole::get).collect(Collectors.toCollection(HashSet::new));

            Dialog confirm = new Dialog();
            confirm.add(new Span(getTranslation("admin.users.confirm.apply")));
            Button ok = new Button(VaadinIcon.CHECK.create(), ev -> {
                try {
                    String adminEmail = SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getName();
                    adminUserService.updateRolesWithAudit(adminEmail, user.getId(), roles);
                    adminUserService.getById(user.getId()).ifPresent(this::updateCurrentSessionIfSelf);
                    Notification.show(getTranslation("admin.users.updated"), 1500, Notification.Position.BOTTOM_START);
                    confirm.close();
                    refresh();
                } catch (Exception ex) {
                    Notification.show(getTranslation("dialog.saveFailed", ex.getMessage()));
                }
            });
            Button cancel = new Button(VaadinIcon.CLOSE_SMALL.create(), ev -> confirm.close());
            confirm.add(new HorizontalLayout(ok, cancel));
            confirm.open();
        });

        Button edit = new Button(VaadinIcon.EDIT.create(), e -> {
            new org.apolenkov.application.views.components.EditUserDialog(adminUserService, user, saved -> {
                        updateCurrentSessionIfSelf(saved);
                        refresh();
                    })
                    .open();
        });

        Button delete = new Button(VaadinIcon.TRASH.create(), e -> {
            Dialog confirm = new Dialog();
            confirm.add(new Span(getTranslation("admin.users.confirm.delete")));
            Button ok = new Button(VaadinIcon.TRASH.create(), ev -> {
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
                    adminUserService.getById(user.getId()).ifPresent(u -> {
                        boolean lastAdmin = adminUserService.listAll().stream()
                                                .filter(x -> x.getRoles().contains(SecurityConstants.ROLE_ADMIN))
                                                .count()
                                        <= 1
                                && u.getRoles().contains(SecurityConstants.ROLE_ADMIN);
                        if (lastAdmin) {
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
            });
            Button cancel = new Button(VaadinIcon.CLOSE_SMALL.create(), ev -> confirm.close());
            confirm.add(new HorizontalLayout(ok, cancel));
            confirm.open();
        });

        // Add minimal icon styles and tooltips
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        save.getElement().setAttribute("title", getTranslation("dialog.save"));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        edit.getElement().setAttribute("title", getTranslation("dialog.edit"));
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        delete.getElement().setAttribute("title", getTranslation("dialog.delete"));

        HorizontalLayout buttons = new HorizontalLayout(save, edit, delete);
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

    private String roleToLabel(String role) {
        String normalized = role;
        if (normalized == null) return "";
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        if ("ADMIN".equalsIgnoreCase(normalized)) return getTranslation("admin.users.role.ADMIN");
        return getTranslation("admin.users.role.USER");
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
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("admin.users.page.title");
    }
}
