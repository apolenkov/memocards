package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for search and filter controls in the deck grid.
 * Provides search field, hide known checkbox, and reset progress button.
 */
public final class DeckSearchControls extends Composite<HorizontalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckSearchControls.class);

    // UI Components
    private final TextField searchField;
    private final Checkbox hideKnownCheckbox;
    private final Button resetProgressButton;

    // Callbacks
    private transient Consumer<String> searchCallback;
    private transient Consumer<Boolean> filterCallback;
    private transient Runnable resetCallback;

    // Event Registrations
    private Registration searchFieldListenerRegistration;
    private Registration hideKnownCheckboxListenerRegistration;
    private Registration resetProgressButtonListenerRegistration;

    /**
     * Creates a new DeckSearchControls component.
     */
    public DeckSearchControls() {
        this.searchField = new TextField();
        this.hideKnownCheckbox = new Checkbox();
        this.resetProgressButton = new Button();
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout controls = new HorizontalLayout();

        configureSearchField();
        configureHideKnownCheckbox();
        configureResetProgressButton();
        createLayout(controls);

        return controls;
    }

    /**
     * Configures the search field with placeholder, prefix icon, and value change listener.
     */
    private void configureSearchField() {
        searchField.setPlaceholder(getTranslation(DeckConstants.DECK_SEARCH_CARDS));
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
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
        hideKnownCheckboxListenerRegistration = hideKnownCheckbox.addValueChangeListener(e -> {
            if (filterCallback != null) {
                filterCallback.accept(e.getValue());
            }
        });
    }

    /**
     * Configures the reset progress button.
     */
    private void configureResetProgressButton() {
        resetProgressButton.setText(getTranslation(DeckConstants.DECK_RESET_PROGRESS));
        resetProgressButton.setIcon(VaadinIcon.ROTATE_LEFT.create());
        resetProgressButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        resetProgressButtonListenerRegistration = resetProgressButton.addClickListener(e -> {
            LOGGER.info("Reset progress button clicked");
            NotificationHelper.showSuccessBottom(getTranslation(DeckConstants.DECK_PROGRESS_RESET));
            if (resetCallback != null) {
                resetCallback.run();
            }
        });
    }

    /**
     * Creates the search controls layout.
     * Groups search and filters in a compact, centered layout.
     *
     * @param container the container to configure and populate
     */
    private void createLayout(final HorizontalLayout container) {
        // Create compact horizontal layout
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        container.setSpacing(true);

        // Add search field with fixed width
        searchField.setWidth("250px");
        container.add(searchField);

        // Add compact filter controls
        HorizontalLayout filters = new HorizontalLayout();
        filters.setSpacing(true);
        filters.setAlignItems(FlexComponent.Alignment.CENTER);
        filters.add(hideKnownCheckbox, resetProgressButton);
        container.add(filters);
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
     * Sets the reset callback.
     *
     * @param callback the callback to execute when reset button is clicked
     */
    public void setResetCallback(final Runnable callback) {
        this.resetCallback = callback;
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
        if (resetProgressButtonListenerRegistration != null) {
            resetProgressButtonListenerRegistration.remove();
            resetProgressButtonListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
