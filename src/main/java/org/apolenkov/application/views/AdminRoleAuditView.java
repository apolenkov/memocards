package org.apolenkov.application.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.domain.port.RoleAuditRepository;

@Route(value = "admin/audit", layout = PublicLayout.class)
@RouteAlias(value = "admin/role-audit", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminRoleAuditView extends VerticalLayout implements HasDynamicTitle {

    public AdminRoleAuditView(RoleAuditRepository repo) {
        setSizeFull();
        Grid<RoleAuditRepository.RoleAuditRecord> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(RoleAuditRepository.RoleAuditRecord::adminEmail)
                .setHeader(getTranslation("admin.audit.columns.admin"));
        grid.addColumn(r -> String.valueOf(r.userId())).setHeader(getTranslation("admin.audit.columns.userId"));
        grid.addColumn(r -> String.join(", ", r.rolesBefore())).setHeader(getTranslation("admin.audit.columns.before"));
        grid.addColumn(r -> String.join(", ", r.rolesAfter())).setHeader(getTranslation("admin.audit.columns.after"));
        grid.addColumn(new com.vaadin.flow.data.renderer.LocalDateTimeRenderer<RoleAuditRepository.RoleAuditRecord>(
                        RoleAuditRepository.RoleAuditRecord::at,
                        () -> java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                                        java.time.format.FormatStyle.MEDIUM)
                                .withLocale(getLocale())))
                .setHeader(getTranslation("admin.audit.columns.at"));
        grid.setItems(repo.listAll());
        add(grid);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("admin.audit.page.title");
    }
}
