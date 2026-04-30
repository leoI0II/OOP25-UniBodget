package it.unibo.unibodget.model.dashboard.support;


/**
 * Observable component of the dashboard observer mechanism.
 *
 * Implementations of this interface maintain a collection of
 * {@link TransactionObserver}s and notify them whenever the observed
 * transaction-related state changes.
 */
public interface TransactionObservable {

     /**
     * Registers an observer interested in updates of the observed state.
     * @param observer the observer to register
     */
    void addObserver(TransactionObserver observer);

    /**
     * Removes a previously registered observer.
     *
     * @param observer the observer to remove
     */
    void removeObserver(TransactionObserver observer);

    /**
     * Notifies all registered observers that the observed state has changed.
     */
    void notifyObservers();
}   