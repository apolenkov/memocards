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
        if (filterOption == null) {
            throw new IllegalArgumentException("FilterOption cannot be null");
        }
    }

    /**
     * Creates a CardFilter with default ALL option if filterOption is null.
     *
     * @param searchQueryValue search query
     * @param filterOptionValue filter option (can be null)
     * @return CardFilter with validated filterOption
     */
    public static CardFilter of(final String searchQueryValue, final FilterOption filterOptionValue) {
        return new CardFilter(searchQueryValue, filterOptionValue != null ? filterOptionValue : FilterOption.ALL);
    }
}
