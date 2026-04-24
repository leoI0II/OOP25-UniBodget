package it.unibo.unibodget.model.dashboard.api;

import it.unibo.unibodget.model.dashboard.support.TransactionObservable;

import java.util.List;

/**
 * Provides access to the movement history shown in the dashboard.
 * This service also acts as an observable source so that dashboard
 * views can be notified whenever the tracked movement collection changes.
 */
public interface MovementHistoryService extends TransactionObservable {

    /**
     * Returns the recent transactions currently visible in the dashboard.
     *
     * @return a list containing textual descriptions of recent transactions
     */
    List<String> getRecentTransactions();

    /**
     * Adds a new transaction description to the history and notifies observers.
     *
     * @param transactionDescription the transaction description to add
     */
    void addTransaction(String transactionDescription);

    /**
     * Removes a transaction description from the history and notifies observers.
     *
     * @param transactionDescription the transaction description to remove
     * @return {@code true} if the transaction was removed, {@code false} otherwise
     */
    boolean removeTransaction(String transactionDescription);
}