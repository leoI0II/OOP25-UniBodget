package it.unibo.unibodget.model.dashboard.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of transaction history filtering and sorting.
 */
public final class DefaultTransactionHistoryFilterService {

    /**
     * Creates a transaction history filter service.
     */
    public DefaultTransactionHistoryFilterService() {
    }

    /**
     * Filters and sorts the given transactions according to the provided criteria.
     *
     * @param transactions the transactions to process
     * @param criteria the active filter criteria
     * @return the filtered and sorted transactions
     */
    public List<CashTransaction> filter(
            final List<CashTransaction> transactions,
            final TransactionFilterCriteria criteria) {
        Objects.requireNonNull(transactions);
        Objects.requireNonNull(criteria);

        return transactions.stream()
                .filter(transaction -> matchesCategoryName(transaction, criteria))
                .filter(transaction -> matchesCategoryType(transaction, criteria))
                .filter(transaction -> matchesFromDate(transaction, criteria))
                .filter(transaction -> matchesToDate(transaction, criteria))
                .filter(transaction -> matchesKeyword(transaction, criteria))
                .sorted(comparatorFor(criteria.getSortOrder()))
                .toList();
    }

    private boolean matchesCategoryName(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getCategoryName()
                .map(categoryName -> {
                    final String normalizedFilter = categoryName.trim().toLowerCase();
                    final String normalizedCategory = transaction.getCategory()
                            .getName()
                            .trim()
                            .toLowerCase();
                    return normalizedCategory.contains(normalizedFilter);
                })
                .orElse(true);
    }

    private boolean matchesCategoryType(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getCategoryType()
                .map(categoryType -> transaction.getCategory().getType() == categoryType)
                .orElse(true);
    }

    private boolean matchesFromDate(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getFromDate()
                .map(fromDate -> !transaction.getDate().isBefore(fromDate))
                .orElse(true);
    }

    private boolean matchesToDate(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getToDate()
                .map(toDate -> !transaction.getDate().isAfter(toDate))
                .orElse(true);
    }

    private boolean matchesKeyword(
            final CashTransaction transaction,
            final TransactionFilterCriteria criteria) {
        return criteria.getKeyword()
                .map(keyword -> {
                    final String normalizedKeyword = keyword.toLowerCase();
                    final String description = transaction.getDescription() == null
                            ? ""
                            : transaction.getDescription().toLowerCase();
                    final String notes = transaction.getNotes() == null
                            ? ""
                            : transaction.getNotes().toLowerCase();
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
                    transaction -> transaction.getAsset().amount().abs(),
                    Comparator.reverseOrder()
            );
        };
    }
}