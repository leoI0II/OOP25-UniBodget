package it.unibo.unibodget.model.dashboard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.MovementHistoryService;
import it.unibo.unibodget.model.dashboard.support.TransactionObserver;
import it.unibo.unibodget.model.transactions.Historical;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Default implementation of {@link MovementHistoryService}.
 *
 * <p>This service delegates transaction storage operations to the domain
 * {@link Historical} component and adds observer notifications required by the
 * dashboard subsystem.</p>
 */
public final class DefaultMovementHistoryService implements MovementHistoryService {

    private final Historical<Transaction> history;
    private final List<TransactionObserver> observers;

    /**
     * Creates a new movement history service backed by the provided domain
     * transaction history.
     *
     * @param history
     *            the transaction history used as storage
     */
    public DefaultMovementHistoryService(final Historical<Transaction> history) {
        this.history = Objects.requireNonNull(history);
        this.observers = new ArrayList<>();
    }

    @Override
    public List<Transaction> getRecentTransactions() {
        return history.getTransactions();
    }

    @Override
    public void addTransaction(final Transaction transaction) {
        history.addTransaction(Objects.requireNonNull(transaction));
        notifyObservers();
    }

    @Override
    public boolean removeTransaction(final Transaction transaction) {
        final boolean removed = history.removeTransaction(Objects.requireNonNull(transaction));
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    @Override
    public boolean replaceTransaction(
            final Transaction oldTransaction,
            final Transaction newTransaction) {
        final boolean replaced = history.replaceTransaction(
                Objects.requireNonNull(oldTransaction),
                Objects.requireNonNull(newTransaction)
        );
        if (replaced) {
            notifyObservers();
        }
        return replaced;
    }

    @Override
    public void clear() {
        history.clear();
        notifyObservers();
    }

    @Override
    public void addObserver(final TransactionObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    @Override
    public void removeObserver(final TransactionObserver observer) {
        observers.remove(Objects.requireNonNull(observer));
    }

    @Override
    public void notifyObservers() {
        observers.forEach(TransactionObserver::update);
    }
}