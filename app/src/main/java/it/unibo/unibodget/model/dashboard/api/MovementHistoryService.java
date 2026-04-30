package it.unibo.unibodget.model.dashboard.api;

import java.util.List;

import it.unibo.unibodget.model.dashboard.support.TransactionObservable;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Provides access to the movement history shown in the dashboard.
 *
 * <p>This service exposes the transactions currently tracked by the dashboard
 * and acts as an observable source so that registered views can be notified
 * whenever the collection changes.</p>
 */
public interface MovementHistoryService extends TransactionObservable {

    /**
     * Returns the transactions currently visible in the dashboard.
     *
     * @return the tracked transactions
     */
    List<Transaction> getRecentTransactions();

    /**
     * Adds a new transaction to the tracked history and notifies observers.
     *
     * @param transaction
     *            the transaction to add
     */
    void addTransaction(Transaction transaction);

    /**
     * Removes a transaction from the tracked history and notifies observers.
     *
     * @param transaction
     *            the transaction to remove
     * @return {@code true} if the transaction was removed, {@code false}
     *         otherwise
     */
    boolean removeTransaction(Transaction transaction);

    /**
     * Replaces an existing transaction with a new one and notifies observers
     * if the replacement succeeds.
     *
     * @param oldTransaction
     *            the transaction to replace
     * @param newTransaction
     *            the replacement transaction
     * @return {@code true} if the replacement succeeded, {@code false}
     *         otherwise
     */
    boolean replaceTransaction(Transaction oldTransaction, Transaction newTransaction);

    /**
     * Removes all tracked transactions and notifies observers.
     */
    void clear();
}