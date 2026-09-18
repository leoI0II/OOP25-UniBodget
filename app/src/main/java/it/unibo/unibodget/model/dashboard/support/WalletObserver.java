package it.unibo.unibodget.model.dashboard.support;

/**
 * Observer notified when the wallet state changes.
 * <p>Concrete implementations define the action to perform after a change
 * in the selected wallet or in the transaction history associated with it.</p>
 */
@FunctionalInterface
public interface WalletObserver {

    /**
     * Performs the action associated with a wallet update.
     */
    void update();
}
