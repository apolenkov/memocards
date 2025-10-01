package org.apolenkov.application.views.deck.components.decks;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.shared.utils.LayoutHelper;

/**
 * Reusable toolbar component for deck management operations.
 * Provides search functionality and deck creation button with consistent styling
 * and event handling for deck listing views.
 */
public final class DeckToolbar extends HorizontalLayout {

    // UI Components
    private final TextField searchField;
    private final Button addButton;

    /**
     * Creates a new DeckToolbar with default configuration.
     * Initializes search field and add button with proper styling and event handling.
     */
    public DeckToolbar() {
        this.searchField = new TextField();
        this.addButton = new Button();
    }

    /**
     * Configures the search input field.
     * Defers translation and configuration to avoid this-escape in constructor.
     */
    private void configureSearchField() {
        searchField.setPlaceholder(getTranslation("home.search.placeholder"));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("deck-toolbar__search");
    }

    /**
     * Configures the add deck button.
     * Defers translation and configuration to avoid this-escape in constructor.
     */
    private void configureAddButton() {
        addButton.setText(getTranslation("home.addDeck"));
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName("deck-toolbar__add-button");
    }

    /**
     * Configures the toolbar layout with proper styling.
     * Applies consistent spacing, alignment, and CSS classes.
     */
    private void configureLayout() {
        setWidthFull();
        addClassName(DeckConstants.DECK_TOOLBAR_CLASS);
    }

    /**
     * Adds components to the toolbar layout.
     * Uses LayoutHelper for consistent search row creation.
     */
    private void addComponents() {
        HorizontalLayout searchRow = LayoutHelper.createSearchRow(searchField, addButton);
        add(searchRow);
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureLayout();
        configureSearchField();
        configureAddButton();
        addComponents();
    }

    /**
     * Adds a listener for search value changes.
     * Provides access to search field value change events for filtering operations.
     *
     * @param listener the consumer for search value changes
     * @return registration for removing the listener
     */
    public Registration addSearchListener(final Consumer<String> listener) {
        return searchField.addValueChangeListener(e -> listener.accept(e.getValue()));
    }

    /**
     * Adds a listener for add button clicks.
     * Provides access to add button click events for deck creation operations.
     *
     * @param listener the event listener for add button clicks
     * @return registration for removing the listener
     */
    public Registration addAddClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return addButton.addClickListener(listener);
    }
}
