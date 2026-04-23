package it.unibo.unibodget.model.dashboard.support;

public interface TransactionObservable {
    void addObserver(TransactionObserver observer);
    void removeObserver(TransactionObserver observer);
    void notifyObservers();
}