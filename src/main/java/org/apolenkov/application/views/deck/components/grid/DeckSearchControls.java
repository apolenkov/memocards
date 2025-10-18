package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Component for search and filter controls in the deck grid.
 * Provides search field and hide known checkbox.
 */
public final class DeckSearchControls extends Composite<HorizontalLayout> {

    // UI Components
    private final TextField searchField;
    private final Checkbox hideKnownCheckbox;

    // Configuration
    private final int searchDebounceMs;

    // Callbacks
    private transient Consumer<String> searchCallback;
    private transient Consumer<Boolean> filterCallback;

    // Event Registrations
    private Registration searchFieldListenerRegistration;
    private Registration hideKnownCheckboxListenerRegistration;

    /**
     * Creates a new DeckSearchControls component.
     *
     * @param searchDebounceTimeout debouncing timeout in milliseconds for search field
     */
    public DeckSearchControls(final int searchDebounceTimeout) {
        this.searchField = new TextField();
        this.hideKnownCheckbox = new Checkbox();
        this.searchDebounceMs = searchDebounceTimeout;
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout controls = new HorizontalLayout();

        configureSearchField();
        configureHideKnownCheckbox();
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

        searchFieldListenerRegistration = searchField.addValueChangeListener(e -> {
            if (searchCallback != null) {
                searchCallback.accept(e.getValue());
            }
        });
    }

    /**
     * Configures the hide known checkbox with proper styling.
     */
    private void configureHideKnownCheckbox() {
        hideKnownCheckbox.setLabel(getTranslation(DeckConstants.DECK_HIDE_KNOWN));
        hideKnownCheckbox.setValue(true);
        hideKnownCheckbox.addClassName(DeckConstants.DECK_SEARCH_CHECKBOX_CLASS);

        hideKnownCheckboxListenerRegistration = hideKnownCheckbox.addValueChangeListener(e -> {
            if (filterCallback != null) {
                filterCallback.accept(e.getValue());
            }
        });
    }

    /**
     * Creates the search controls layout.
     * Groups search and filters in a compact layout.
     *
     * @param container the container to configure and populate
     */
    private void createLayout(final HorizontalLayout container) {
        // Configure container layout
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setSpacing(true);

        // Add search field with fixed width
        searchField.setWidth("250px");

        // Add components directly to container
        container.add(searchField, hideKnownCheckbox);
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
     * Sets the filter callback.
     *
     * @param callback the callback to execute when filter value changes
     */
    public void setFilterCallback(final Consumer<Boolean> callback) {
        this.filterCallback = callback;
    }

    /**
     * Gets the current search query.
     *
     * @return the search query
     */
    public String getSearchQuery() {
        return searchField.getValue() != null
                ? searchField.getValue().toLowerCase().trim()
                : "";
    }

    /**
     * Gets the current hide known filter value.
     *
     * @return true if hiding known cards
     */
    public boolean isHideKnown() {
        return Boolean.TRUE.equals(hideKnownCheckbox.getValue());
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
        if (hideKnownCheckboxListenerRegistration != null) {
            hideKnownCheckboxListenerRegistration.remove();
            hideKnownCheckboxListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
