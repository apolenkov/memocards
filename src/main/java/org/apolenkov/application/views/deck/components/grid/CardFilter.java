package org.apolenkov.application.views.deck.components.grid;

import org.apolenkov.application.domain.model.FilterOption;

/**
 * Filter for card data provider.
 * Supports search query and filter option (known/unknown/all).
 *
 * @param searchQuery search query (can be null or empty)
 * @param filterOption filter option for known/unknown status
 */
public record CardFilter(String searchQuery, FilterOption filterOption) {

    /**
     * Compact constructor with validation.
     */
    public CardFilter {
        filterOption = filterOption != null ? filterOption : FilterOption.ALL;
    }
}
