package it.unibo.unibodget.model.dashboard.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.specification.TransactionSpecification;
import it.unibo.unibodget.model.dashboard.specification.TransactionSpecifications;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Service that filters and sorts cash transaction history.
 *
 * <p>
 * This service applies all active filter specifications and then
 * sorts the resulting list according to the specified sort order.
 * </p>
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
     * @param transactions the transactions to process; must not be {@code null}
     * @param criteria the active filter criteria; must not be {@code null}
     * @return the filtered and sorted transactions
     */
    public List<CashTransaction> filter(
            final List<CashTransaction> transactions,
            final TransactionFilterCriteria criteria) {
        Objects.requireNonNull(transactions, "transactions must not be null");
        Objects.requireNonNull(criteria, "criteria must not be null");

        return filter(
                transactions,
                TransactionSpecifications.fromCriteria(criteria),
                criteria.getSortOrder()
        );
    }

    /**
     * Filters and sorts the given transactions according to a composite {@link TransactionSpecification}.
     *
     * @param transactions the transactions to process; must not be {@code null}
     * @param specification the specification to evaluate; must not be {@code null}
     * @param sortOrder the sort order to apply; must not be {@code null}
     * @return the filtered and sorted transactions
     */
    public List<CashTransaction> filter(
            final List<CashTransaction> transactions,
            final TransactionSpecification specification,
            final TransactionSortOrder sortOrder) {
        Objects.requireNonNull(transactions, "transactions must not be null");
        Objects.requireNonNull(specification, "specification must not be null");
        Objects.requireNonNull(sortOrder, "sortOrder must not be null");

        return transactions.stream()
                .filter(specification::isSatisfiedBy)
                .sorted(comparatorFor(sortOrder))
                .toList();
    }

    private Comparator<CashTransaction> comparatorFor(final TransactionSortOrder sortOrder) {
        final Comparator<CashTransaction> byDateAsc = Comparator
                .comparing(CashTransaction::getDate)
                .thenComparing(
                        CashTransaction::getDescription,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                )
                .thenComparing(transaction -> transaction.getAsset().amount().abs());

        final Comparator<CashTransaction> byAmountDesc = Comparator
                .comparing(
                        (CashTransaction transaction) -> transaction.getAsset().amount().abs(),
                        Comparator.reverseOrder()
                )
                .thenComparing(CashTransaction::getDate, Comparator.reverseOrder())
                .thenComparing(
                        CashTransaction::getDescription,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                );

        return switch (sortOrder) {
            case NEWEST_FIRST -> byDateAsc.reversed();
            case OLDEST_FIRST -> byDateAsc;
            case HIGHEST_AMOUNT_FIRST -> byAmountDesc;
        };
    }
}
