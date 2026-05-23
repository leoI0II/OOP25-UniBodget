package it.unibo.unibodget.model.dashboard.impl;

/**
 * Identifies the supported sort orders for transaction history.
 */
public enum TransactionSortOrder {

    /**
     * Sorts transactions by date from newest to oldest.
     */
    NEWEST_FIRST,

    /**
     * Sorts transactions by date from oldest to newest.
     */
    OLDEST_FIRST,

    /**
     * Sorts transactions by absolute amount from highest to lowest.
     */
    HIGHEST_AMOUNT_FIRST
}