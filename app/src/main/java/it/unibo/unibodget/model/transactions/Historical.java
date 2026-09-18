package it.unibo.unibodget.model.transactions;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.transactions.base.AbstractTransaction;

/**
 * Represents an ordered collection of transactions.
 *
 * <p>
 * Acts as a historical ledger, storing a chronological list of
 * {@link AbstractTransaction} objects. It provides basic operations for:
 * <ul>
 * <li>adding a new transaction</li>
 * <li>removing or replacing an existing transaction</li>
 * <li>retrieving the full immutable history</li>
 * <li>clearing all recorded transactions</li>
 * </ul>
 *
 * @param <T> the concrete transaction type stored in this ledger
 */
public class Historical<T extends AbstractTransaction> {

    @JsonProperty("transactions")
    private final List<T> history;

    /**
     * Constructs an empty historical ledger.
     */
    public Historical() {
        this.history = new ArrayList<>();
    }

    /**
     * Creates a new ledger pre-populated with the given transactions.
     *
     * @param history the initial list of transactions; must not be {@code null}
     * @throws NullPointerException if {@code history} is {@code null}
     */
    public Historical(final List<T> history) {
        this.history = new ArrayList<>(Objects.requireNonNull(history));
    }

    /**
     * Adds a new transaction to the historical ledger.
     *
     * @param transaction the transaction to add; must not be {@code null}
     * @throws NullPointerException if {@code transaction} is {@code null}
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
     *
     * <p>
     * Uses {@link Object#equals} to locate the transaction.
     *
     * @param transaction the transaction to remove
     * @return {@code true} if the transaction was found and removed,
     * {@code false} otherwise
     */
    public boolean removeTransaction(final T transaction) {
        return history.remove(transaction);
    }

    /**
     * Replaces an existing transaction with a new one, preserving its position
     * in the ledger.
     *
     * <p>
     * Uses {@link Object#equals} to locate {@code oldTransaction}.
     *
     * @param oldTransaction the transaction to replace; must not be
     * {@code null}
     * @param newTransaction the replacement transaction; must not be
     * {@code null}
     * @return {@code true} if the replacement succeeded, {@code false} if
     * {@code oldTransaction} was not found in the ledger
     * @throws NullPointerException if either argument is {@code null}
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(history);
    }

    /**
     * Returns all transactions that occurred within the given calendar month.
     *
     * @param month the month to filter by
     * @return an unmodifiable list of transactions that occurred in the
     * specified month
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
