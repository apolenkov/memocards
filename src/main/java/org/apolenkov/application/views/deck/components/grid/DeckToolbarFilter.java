package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Reusable filter combobox component for deck toolbars.
 * Provides consistent filtering options across different deck components.
 */
public final class DeckToolbarFilter extends Composite<ComboBox<FilterOption>> {

    private final ComboBox<FilterOption> filterComboBox;

    /**
     * Creates a new DeckToolbarFilter component.
     */
    public DeckToolbarFilter() {
        this.filterComboBox = new ComboBox<>();
    }

    @Override
    protected ComboBox<FilterOption> initContent() {
        configureFilterComboBox();
        return filterComboBox;
    }

    /**
     * Configures the filter combobox with options and styling.
     */
    private void configureFilterComboBox() {
        filterComboBox.setItems(FilterOption.ALL, FilterOption.KNOWN_ONLY, FilterOption.UNKNOWN_ONLY);
        filterComboBox.setValue(FilterOption.UNKNOWN_ONLY); // Default: hide known cards
        filterComboBox.setPlaceholder(getTranslation(DeckConstants.DECK_FILTER_LABEL));
        filterComboBox.addClassName("deck-filter-combobox");

        // Item label generator with translations
        filterComboBox.setItemLabelGenerator(option -> switch (option) {
            case ALL -> getTranslation(DeckConstants.DECK_FILTER_ALL);
            case KNOWN_ONLY -> getTranslation(DeckConstants.DECK_FILTER_KNOWN);
            case UNKNOWN_ONLY -> getTranslation(DeckConstants.DECK_FILTER_UNKNOWN);
        });
    }

    /**
     * Adds a listener for filter value changes.
     *
     * @param callback the callback to execute when filter value changes
     * @return registration for removing the listener
     */
    public Registration addFilterChangeListener(final Consumer<FilterOption> callback) {
        return filterComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null && callback != null) {
                callback.accept(e.getValue());
            }
        });
    }
}
