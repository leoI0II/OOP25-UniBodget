package it.unibo.unibodget.model.dashboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibo.unibodget.model.dashboard.api.MovementHistoryService;
import it.unibo.unibodget.model.dashboard.support.TransactionObserver;

/**
 * Default implementation of {@link MovementHistoryService}.
 * This implementation stores a local list of transaction descriptions
 * and notifies registered observers whenever the list changes.
 */
public final class DefaultMovementHistoryService implements MovementHistoryService {

    private final List<String> transactions;
    private final List<TransactionObserver> observers;

    /**
     * Creates an empty movement history service.
     */
    public DefaultMovementHistoryService() {
        this(List.of());
    }

    /**
     * Creates a movement history service initialized with the provided transactions.
     *
     * @param initialTransactions the initial transaction descriptions
     */
    public DefaultMovementHistoryService(final List<String> initialTransactions) {
        this.transactions = new ArrayList<>(initialTransactions);
        this.observers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRecentTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTransaction(final String transactionDescription) {
        transactions.add(transactionDescription);
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeTransaction(final String transactionDescription) {
        final boolean removed = transactions.remove(transactionDescription);
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObserver(final TransactionObserver observer) {
        observers.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(final TransactionObserver observer) {
        observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyObservers() {
        observers.forEach(TransactionObserver::update);
    }
}