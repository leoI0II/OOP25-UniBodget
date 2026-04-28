package it.unibo.unibodget.model.dashboard.support;

public interface TransactionObserver {
    /**
     * Performs the action associated with a movement history update.
     */
    void update();
}
