package it.unibo.unibodget.view.dashboard.state;

import java.time.LocalDate;

/**
 * Immutable raw filter input collected from the dashboard transaction-filter UI.
 *
 * @param categoryName optional category name
 * @param categoryType optional category type name
 * @param fromDate optional inclusive start date
 * @param toDate optional inclusive end date
 * @param keyword optional keyword searched in description and notes
 * @param sortOrder required sort-order token
 */
public record TransactionFilterInput(
        String categoryName,
        String categoryType,
        LocalDate fromDate,
        LocalDate toDate,
        String keyword,
        String sortOrder) {
}