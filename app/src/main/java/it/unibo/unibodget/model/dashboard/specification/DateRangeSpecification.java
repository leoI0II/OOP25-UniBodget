package it.unibo.unibodget.model.dashboard.specification;

import java.time.LocalDate;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Specification matching transactions occurring within an optional start and end date (inclusive).
 */
public final class DateRangeSpecification implements TransactionSpecification {

    private final LocalDate fromDate;
    private final LocalDate toDate;

    /**
     * Creates a date range specification.
     *
     * @param fromDate the inclusive lower bound date, or {@code null} if unbounded
     * @param toDate the inclusive upper bound date, or {@code null} if unbounded
     */
    public DateRangeSpecification(final LocalDate fromDate, final LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public boolean isSatisfiedBy(final CashTransaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        final LocalDate txDate = transaction.getDate();
        if (txDate == null) {
            return false;
        }
        final boolean matchesFrom = fromDate == null || !txDate.isBefore(fromDate);
        final boolean matchesTo = toDate == null || !txDate.isAfter(toDate);
        return matchesFrom && matchesTo;
    }
}
