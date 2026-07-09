package it.unibo.unibodget.model.dashboard.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link TransactionHistoryFilterService}.
 *
 * <p>This implementation applies all active filters cumulatively and then
 * sorts the resulting list according to the specified sort order.</p>
 */
public final class DefaultTransactionHistoryFilterService {

    /**
     * Creates a transaction history filter service.
     */
    public DefaultTransactionHistoryFilterService() {
    }

    /**
     * {@inheritDoc}
     */
    public List<CashTransaction> filter(
            final List<CashTransaction> transactions,
            final TransactionFilterCriteria criteria) {
        Objects.requireNonNull(transactions);
        Objects.requireNonNull(criteria);

        return transactions.stream()
                .filter(transaction -> matchesCategoryName(transaction, criteria))
                .filter(transaction -> matchesCategoryType(transaction, criteria))
                .filter(transaction -> matchesDateRange(transaction, criteria))
                .filter(transaction -> matchesKeyword(transaction, criteria))
                .sorted(comparatorFor(criteria.getSortOrder()))
                .toList();
    }

    private boolean matchesCategoryName(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getCategoryName()
                .map(categoryName -> transaction.getCategory()
                        .getName()
                        .equalsIgnoreCase(categoryName))
                .orElse(true);
    }

    private boolean matchesCategoryType(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getCategoryType()
                .map(categoryType -> transaction.getCategory().getType() == categoryType)
                .orElse(true);
    }

    private boolean matchesDateRange(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        final boolean matchesFrom = criteria.getFromDate()
                .map(fromDate -> !transaction.getDate().isBefore(fromDate))
                .orElse(true);

        final boolean matchesTo = criteria.getToDate()
                .map(toDate -> !transaction.getDate().isAfter(toDate))
                .orElse(true);

        return matchesFrom && matchesTo;
    }

    private boolean matchesKeyword(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getKeyword()
                .map(keyword -> {
                    final String normalizedKeyword = keyword.toLowerCase();
                    final String description = safeLowerCase(transaction.getDescription());
                    final String notes = safeLowerCase(transaction.getNotes());

                    return description.contains(normalizedKeyword)
                            || notes.contains(normalizedKeyword);
                })
                .orElse(true);
    }

    private Comparator<CashTransaction> comparatorFor(final TransactionSortOrder sortOrder) {
        return switch (sortOrder) {
            case NEWEST_FIRST -> Comparator.comparing(CashTransaction::getDate).reversed();
            case OLDEST_FIRST -> Comparator.comparing(CashTransaction::getDate);
            case HIGHEST_AMOUNT_FIRST -> Comparator.comparing(
                    (CashTransaction transaction) -> transaction.getAsset().amount().abs()
            ).reversed();
        };
    }

    private String safeLowerCase(final String value) {
        return value == null ? "" : value.toLowerCase();
    }
}