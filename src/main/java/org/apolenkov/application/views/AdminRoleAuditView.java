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
 *
 * <p>Shows comprehensive audit trail of role modifications with before/after states.</p>
 */
@Route(value = "admin/audit", layout = PublicLayout.class)
@RouteAlias(value = "admin/role-audit", layout = PublicLayout.class)
@RolesAllowed("ROLE_ADMIN")
public class AdminRoleAuditView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Constructs a new AdminRoleAuditView with the required repository dependency.
     *
     * <p>This constructor initializes the administrative audit interface for role changes.
     * It sets up the layout, creates the audit grid, and configures all columns with
     * appropriate headers and data renderers.</p>
     *
     * <p>The initialization process includes:</p>
     * <ul>
     *   <li><strong>Layout Setup:</strong> Configures full-size layout for optimal data display</li>
     *   <li><strong>Grid Creation:</strong> Creates data grid with striped row styling</li>
     *   <li><strong>Column Configuration:</strong> Sets up all audit data columns</li>
     *   <li><strong>Data Population:</strong> Loads audit records from the repository</li>
     * </ul>
     *
     * <p><strong>Grid Columns:</strong></p>
     * <ul>
     *   <li><strong>Administrator:</strong> Email of the admin who made the change</li>
     *   <li><strong>User ID:</strong> ID of the user whose roles were modified</li>
     *   <li><strong>Roles Before:</strong> Previous role assignments (comma-separated)</li>
     *   <li><strong>Roles After:</strong> New role assignments (comma-separated)</li>
     *   <li><strong>Timestamp:</strong> When the change occurred (localized format)</li>
     * </ul>
     *
     * <p><strong>Grid Features:</strong></p>
     * <ul>
     *   <li>Striped rows for better readability</li>
     *   <li>Localized date/time formatting</li>
     *   <li>Sortable columns for data analysis</li>
     *   <li>Responsive layout for different screen sizes</li>
     * </ul>
     *
     * @param repo the repository for role audit operations
     * @throws IllegalArgumentException if repo is null
     * @see RoleAuditRepository#listAll()
     * @see GridVariant#LUMO_ROW_STRIPES
     * @see com.vaadin.flow.data.renderer.LocalDateTimeRenderer
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
     * Gets the page title for this view.
     *
     * <p>This method implements the {@link HasDynamicTitle} interface to provide
     * a dynamic page title that reflects the current view's purpose. The title
     * is retrieved from the internationalization system to ensure proper
     * localization.</p>
     *
     * <p><strong>Title Behavior:</strong></p>
     * <ul>
     *   <li><strong>Dynamic:</strong> Title changes based on current locale</li>
     *   <li><strong>Localized:</strong> Uses i18n message keys for translation</li>
     *   <li><strong>Administrative:</strong> Clearly indicates this is an admin view</li>
     * </ul>
     *
     * <p><strong>Message Key:</strong> Uses "admin.audit.page.title" which should
     * be defined in the application's message bundles for all supported languages.</p>
     *
     * @return the localized page title for the admin role audit view
     * @see HasDynamicTitle#getPageTitle()
     * @see #getTranslation(String)
     */
    @Override
    public String getPageTitle() {
        return getTranslation("admin.audit.page.title");
    }
}
