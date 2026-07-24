package it.unibo.unibodget.model.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.time.YearMonth;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Represents an ordered collection of transactions.
 *
 * <p>
 * Acts as a historical ledger, storing a chronological list
 * of {@link Transaction} objects (or subclasses such as InvestmentTransaction).
 * It provides basic operations for:
 * - adding a new transaction
 * - retrieving the full immutable history
 */
public final class Historical<T extends Transaction> {

    @JsonProperty("transactions")
    private final List<T> history;
    
    public Historical() {
        this.history = new ArrayList<>();
    }

    public Historical(final List<T> history) {
        this.history = new ArrayList<>(Objects.requireNonNull(history));
    }

    /**
     * Adds a new transaction to the historical ledger.
     *
     * @param transaction the transaction to add; must not be null
     */
    public void addTransaction(final T transaction) {
        history.add(Objects.requireNonNull(transaction));
    }

    /**
     * Returns an immutable view of the transaction history.
     *
     * @return an unmodifiable list containing all recorded transactions
     */
    @JsonProperty("transactions")
    public List<T> getTransactions() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Removes a transaction from the ledger.
     * Uses {@link Object#equals} to locate the transaction.
     *
     * @param transaction the transaction to remove
     */
    public boolean removeTransaction(final T transaction) {
        return history.remove(transaction);
    }

    /**
     * Replaces an existing transaction with a new one, preserving its position in the ledger.
     * Uses {@link Object#equals} to locate {@code oldTransaction}.
     * Does nothing if {@code oldTransaction} is not found.
     *
     * @param oldTransaction the transaction to replace; must not be null
     * @param newTransaction the replacement transaction; must not be null
     */
    public boolean replaceTransaction(final T oldTransaction, final T newTransaction) {
        Objects.requireNonNull(oldTransaction);
        Objects.requireNonNull(newTransaction);
        final int index = history.indexOf(oldTransaction);
        if (index == -1) {
            return false;
        }
        history.set(index, newTransaction);
        return true;
    }

    /**
     * Removes all transactions from the ledger.
     */
    public void clear() {
        history.clear();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Historical<?> other = (Historical<?>) o;
        return Objects.equals(history, other.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(history);
    }

    /**
     * Returns all transactions that occurred within the given calendar month.
     * 
     * @param month the month to filter by
     * @return an unmodifiable list of transactions that occurred in the specified month
     */
    public List<T> filterByMonth(final YearMonth month) {
        Objects.requireNonNull(month, "month must not be null");
        final List<T> filtered = new ArrayList<>();
        for (final T transaction : history) {
            if (YearMonth.from(transaction.getDate()).equals(month)) {
                filtered.add(transaction);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

}
