package org.apolenkov.application.views.deck.components.list;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.LayoutHelper;

/**
 * Reusable toolbar component for deck management operations.
 * Provides search functionality and deck creation button with consistent styling
 * and event handling for deck listing views.
 */
public final class DeckToolbar extends Composite<HorizontalLayout> {

    // UI Components
    private final TextField searchField;
    private final Button addButton;

    // Configuration
    private final int searchDebounceMs;

    /**
     * Creates a new DeckToolbar.
     *
     * @param searchDebounceTimeout debouncing timeout in milliseconds for search field
     */
    public DeckToolbar(final int searchDebounceTimeout) {
        this.searchField = new TextField();
        this.addButton = new Button();
        this.searchDebounceMs = searchDebounceTimeout;
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.addClassName(DeckConstants.DECK_TOOLBAR_CLASS);

        configureSearchField();
        configureAddButton();

        HorizontalLayout searchRow = LayoutHelper.createSearchRow(searchField, addButton);
        toolbar.add(searchRow);
        return toolbar;
    }

    /**
     * Configures the search input field.
     * Uses debouncing to reduce server calls during typing.
     */
    private void configureSearchField() {
        searchField.setPlaceholder(getTranslation(DeckConstants.HOME_SEARCH_PLACEHOLDER));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        searchField.setValueChangeTimeout(searchDebounceMs);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName(DeckConstants.DECK_TOOLBAR_SEARCH_CLASS);
    }

    /**
     * Configures the add deck button.
     */
    private void configureAddButton() {
        addButton.setText(getTranslation(DeckConstants.HOME_ADD_DECK));
        addButton.setIcon(VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName(DeckConstants.DECK_TOOLBAR_ADD_BUTTON_CLASS);
        // Add tooltip for mobile users
        addButton.getElement().setAttribute("title", getTranslation(DeckConstants.HOME_ADD_DECK));
    }

    /**
     * Adds a listener for search value changes.
     *
     * @param listener the consumer for search value changes
     * @return registration for removing the listener
     */
    public Registration addSearchListener(final Consumer<String> listener) {
        return searchField.addValueChangeListener(e -> listener.accept(e.getValue()));
    }

    /**
     * Adds a listener for add button clicks.
     *
     * @param listener the event listener for add button clicks
     * @return registration for removing the listener
     */
    public Registration addAddClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        return addButton.addClickListener(listener);
    }
}
