package org.apolenkov.application.domain.model;

/**
 * Filter option for card filtering by known/unknown status.
 * Used across domain, service, and UI layers for consistent filtering logic.
 */
public enum FilterOption {
    /**
     * Show all cards regardless of known status.
     */
    ALL,

    /**
     * Show only cards marked as known by user.
     */
    KNOWN_ONLY,

    /**
     * Show only cards not yet marked as known by user.
     */
    UNKNOWN_ONLY
}
