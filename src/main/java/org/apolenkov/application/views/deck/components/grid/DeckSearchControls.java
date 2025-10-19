package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.Locale;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Component for search and filter controls in the deck grid.
 * Provides search field, filter combobox, and add flashcard button.
 */
public final class DeckSearchControls extends Composite<HorizontalLayout> {

    // UI Components
    private final TextField searchField;
    private final DeckToolbarFilter filterComboBox;

    // Configuration
    private final int searchDebounceMs;

    // Callbacks
    private transient Consumer<String> searchCallback;

    // Event Registrations
    private Registration searchFieldListenerRegistration;

    /**
     * Creates a new DeckSearchControls component.
     *
     * @param searchDebounceTimeout debouncing timeout in milliseconds for search field
     */
    public DeckSearchControls(final int searchDebounceTimeout) {
        this.searchField = new TextField();
        this.filterComboBox = new DeckToolbarFilter();
        this.searchDebounceMs = searchDebounceTimeout;
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout controls = new HorizontalLayout();

        configureSearchField();
        createLayout(controls);

        return controls;
    }

    /**
     * Configures the search field with placeholder, prefix icon, and value change listener.
     * Uses debouncing to reduce server calls during typing.
     */
    private void configureSearchField() {
        searchField.setPlaceholder(getTranslation(DeckConstants.DECK_SEARCH_CARDS));
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.TIMEOUT);
        searchField.setValueChangeTimeout(searchDebounceMs);
        searchField.setWidthFull();
        searchField.addClassName(DeckConstants.DECK_SEARCH_FIELD_CLASS);

        searchFieldListenerRegistration = searchField.addValueChangeListener(e -> {
            if (searchCallback != null) {
                searchCallback.accept(e.getValue());
            }
        });
    }

    /**
     * Creates the search controls layout.
     * Layout: [Search Field] [Filter ComboBox]
     *
     * @param container the container to configure and populate
     */
    private void createLayout(final HorizontalLayout container) {
        // Configure container layout - same structure as DeckToolbar
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setSpacing(true);
        container.addClassName(DeckConstants.DECK_TOOLBAR_CLASS);

        // Create inner layout for proper spacing like in DecksView
        HorizontalLayout innerLayout = new HorizontalLayout();
        innerLayout.setWidthFull();
        innerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        innerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        innerLayout.setSpacing(true);

        // Search field grows
        searchField.addClassName(DeckConstants.DECK_TOOLBAR_SEARCH_CLASS);

        // Layout: Search Field + Filter ComboBox only
        innerLayout.add(searchField, filterComboBox);
        container.add(innerLayout);
    }

    /**
     * Sets the search callback.
     *
     * @param callback the callback to execute when search value changes
     */
    public void setSearchCallback(final Consumer<String> callback) {
        this.searchCallback = callback;
    }

    /**
     * Gets the current search query.
     *
     * @return the search query
     */
    public String getSearchQuery() {
        return searchField.getValue() != null
                ? searchField.getValue().toLowerCase(Locale.ROOT).trim()
                : "";
    }

    /**
     * Adds a listener for filter value changes.
     *
     * @param callback the callback to execute when filter value changes
     * @return registration for removing the listener
     */
    public Registration addFilterChangeListener(
            final java.util.function.Consumer<org.apolenkov.application.views.deck.components.grid.FilterOption>
                    callback) {
        return filterComboBox.addFilterChangeListener(callback);
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (searchFieldListenerRegistration != null) {
            searchFieldListenerRegistration.remove();
            searchFieldListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
