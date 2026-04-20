package it.unibo.unibodget.model.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Represents an ordered collection of transactions.
 *
 * Acts as a historical ledger, storing a chronological list
 * of {@link Transaction} objects (or subclasses such as InvestmentTransaction).
 * It provides basic operations for:
 * - adding a new transaction
 * - retrieving the full immutable history
 */
public class Historical<T extends Transaction> {

    private final List<T> history = new ArrayList<>();

    /**
     * Adds a new transaction to the historical ledger.
     *
     * @param transaction the transaction to add; must not be null
     */
    public void addTransaction(T transaction) {
        history.add(Objects.requireNonNull(transaction));
    }

    /**
     * Returns an immutable view of the transaction history.
     *
     * @return an unmodifiable list containing all recorded transactions
     */
    public List<T> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
