package org.apolenkov.application.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.domain.port.RoleAuditRepository;

/**
 * Administrative view for displaying role change audit logs.
 * Shows comprehensive audit trail of role modifications with before/after states.
 */
@Route(value = "admin/audit", layout = PublicLayout.class)
@RouteAlias(value = "admin/role-audit", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminRoleAuditView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Creates administrative audit interface for role changes with grid layout and data columns.
     *
     * @param repo the repository for role audit operations
     * @throws IllegalArgumentException if repo is null
     */
    public AdminRoleAuditView(RoleAuditRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("RoleAuditRepository cannot be null");
        }

        setSizeFull();
        Grid<RoleAuditRepository.RoleAuditRecord> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(RoleAuditRepository.RoleAuditRecord::adminEmail)
                .setHeader(getTranslation("admin.audit.columns.admin"));
        grid.addColumn(r -> String.valueOf(r.userId())).setHeader(getTranslation("admin.audit.columns.userId"));
        grid.addColumn(r -> String.join(", ", r.rolesBefore())).setHeader(getTranslation("admin.audit.columns.before"));
        grid.addColumn(r -> String.join(", ", r.rolesAfter())).setHeader(getTranslation("admin.audit.columns.after"));
        grid.addColumn(new com.vaadin.flow.data.renderer.LocalDateTimeRenderer<>(
                        RoleAuditRepository.RoleAuditRecord::at,
                        () -> java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                                        java.time.format.FormatStyle.MEDIUM)
                                .withLocale(getLocale())))
                .setHeader(getTranslation("admin.audit.columns.at"));
        grid.setItems(repo.listAll());
        add(grid);
    }

    /**
     * Gets localized page title for admin role audit view.
     *
     * @return the localized page title for the admin role audit view
     */
    @Override
    public String getPageTitle() {
        return getTranslation("admin.audit.page.title");
    }
}
