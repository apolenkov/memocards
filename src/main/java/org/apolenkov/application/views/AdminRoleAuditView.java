package org.apolenkov.application.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.apolenkov.application.domain.port.RoleAuditRepository;

@Route(value = "admin/role-audit", layout = MainLayout.class)
@PageTitle("Role Audit")
@RolesAllowed("ADMIN")
public class AdminRoleAuditView extends VerticalLayout {

    public AdminRoleAuditView(RoleAuditRepository repo) {
        setSizeFull();
        Grid<RoleAuditRepository.RoleAuditRecord> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(RoleAuditRepository.RoleAuditRecord::adminEmail).setHeader("Admin");
        grid.addColumn(r -> String.valueOf(r.userId())).setHeader("User ID");
        grid.addColumn(r -> String.join(",", r.rolesBefore())).setHeader("Before");
        grid.addColumn(r -> String.join(",", r.rolesAfter())).setHeader("After");
        grid.addColumn(r -> r.at().toString()).setHeader("At");
        grid.setItems(repo.listAll());
        add(grid);
    }
}
