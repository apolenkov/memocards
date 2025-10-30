package org.apolenkov.application.views.shared.components;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.List;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Reusable menu button component with context menu.
 * Displays a button with ellipsis icon that opens a context menu with configurable items.
 *
 * <p>Features:
 * <ul>
 *   <li>WCAG AAA compliant touch target (44x44px minimum)</li>
 *   <li>Context menu with icons and labels</li>
 *   <li>Callback-based event handling</li>
 *   <li>Mobile-friendly design</li>
 * </ul>
 */
public final class MenuButton extends Composite<Button> {

    private final Button button;
    private final ContextMenu contextMenu;

    /**
     * Creates a new MenuButton with specified menu items.
     *
     * @param menuItems list of menu items to display
     */
    public MenuButton(final List<MenuItem> menuItems) {
        this.button = new Button();
        this.contextMenu = new ContextMenu();
        configure(menuItems);
    }

    @Override
    protected Button initContent() {
        return button;
    }

    /**
     * Configures the button and context menu.
     *
     * @param menuItems list of menu items
     */
    private void configure(final List<MenuItem> menuItems) {
        // Configure button
        button.setIcon(VaadinIcon.ELLIPSIS_DOTS_V.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClassName("menu-button");
        button.getElement().setAttribute("aria-label", getTranslation(DeckConstants.MENU_ACTIONS_ARIA_LABEL));

        // Attach context menu to button
        contextMenu.setTarget(button);
        contextMenu.setOpenOnClick(true);
        contextMenu.getElement().setAttribute("theme", "deck-actions-menu-theme");

        // Add menu items
        if (menuItems != null) {
            menuItems.forEach(this::addMenuItem);
        }
    }

    /**
     * Adds a menu item to the context menu.
     *
     * @param menuItem the menu item to add
     */
    private void addMenuItem(final MenuItem menuItem) {
        com.vaadin.flow.component.contextmenu.MenuItem item = contextMenu.addItem(menuItem.label());

        // Add icon if provided
        if (menuItem.icon() != null) {
            Icon icon = menuItem.icon().create();
            item.addComponentAsFirst(icon);
        }

        // Add click listener
        if (menuItem.action() != null) {
            item.addClickListener(e -> menuItem.action().accept(null));
        }

        // Add theme variant if provided
        if (menuItem.themeVariant() != null) {
            item.addClassName(menuItem.themeVariant());
        }
    }

    /**
     * Record representing a menu item.
     *
     * @param label the display label for the menu item
     * @param icon the icon to display (optional)
     * @param action the action to execute when clicked
     * @param themeVariant optional CSS class for styling (e.g., "error" for delete actions)
     */
    public record MenuItem(String label, VaadinIcon icon, Consumer<Void> action, String themeVariant) {

        /**
         * Creates a menu item without theme variant.
         *
         * @param labelValue the display label
         * @param iconValue the icon
         * @param actionValue the action callback
         * @return new MenuItem instance
         */
        public static MenuItem of(
                final String labelValue, final VaadinIcon iconValue, final Consumer<Void> actionValue) {
            return new MenuItem(labelValue, iconValue, actionValue, null);
        }

        /**
         * Creates a menu item with theme variant.
         *
         * @param labelValue the display label
         * @param iconValue the icon
         * @param actionValue the action callback
         * @param themeValue the theme variant class
         * @return new MenuItem instance
         */
        public static MenuItem withTheme(
                final String labelValue,
                final VaadinIcon iconValue,
                final Consumer<Void> actionValue,
                final String themeValue) {
            return new MenuItem(labelValue, iconValue, actionValue, themeValue);
        }
    }
}
