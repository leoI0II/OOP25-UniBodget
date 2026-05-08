package it.unibo.unibodget.model.dashboard.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import it.unibo.unibodget.model.dashboard.support.WalletObserver;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Service responsible for managing wallets and the history of the current wallet.
 *
 * <p>The service exposes the available wallets, the currently selected wallet,
 * and the operations used to inspect and update the history of that wallet.</p>
 */
public interface WalletService {

    /**
     * Returns the wallets available to the user.
     *
     * @return the available wallets
     */
    List<CashAccount> getWallets();

    /**
     * Returns the currently selected wallet.
     *
     * @return the current wallet, if present
     */
    Optional<CashAccount> getCurrentWallet();

    /**
     * Adds a wallet to the collection.
     *
     * @param wallet
     *            the wallet to add
     */
    void addWallet(CashAccount wallet);

    /**
     * Removes a wallet by identifier.
     *
     * @param walletId
     *            the identifier of the wallet to remove
     * @return {@code true} if the wallet was removed, {@code false} otherwise
     */
    boolean removeWallet(UUID walletId);

    /**
     * Selects the wallet to be used as current dashboard context.
     *
     * @param walletId
     *            the identifier of the wallet to select
     * @return {@code true} if the wallet was selected, {@code false} otherwise
     */
    boolean selectWallet(UUID walletId);

    /**
     * Returns the transactions associated with the current wallet.
     *
     * @return the current wallet transactions
     */
    List<CashTransaction> getCurrentTransactions();

    /**
     * Adds a transaction to the current wallet history.
     *
     * @param transaction
     *            the transaction to add
     */
    void addTransaction(CashTransaction transaction);

    /**
     * Removes a transaction from the current wallet history.
     *
     * @param transaction
     *            the transaction to remove
     * @return {@code true} if the transaction was removed, {@code false} otherwise
     */
    boolean removeTransaction(CashTransaction transaction);

    /**
     * Replaces a transaction in the current wallet history.
     *
     * @param oldTransaction
     *            the transaction to replace
     * @param newTransaction
     *            the replacement transaction
     */
    void replaceTransaction(CashTransaction oldTransaction, CashTransaction newTransaction);

    /**
     * Removes all transactions from the current wallet history.
     */
    void clearCurrentWalletHistory();

    /**
     * Registers an observer interested in wallet changes.
     *
     * @param observer
     *            the observer to register
     */
    void addObserver(WalletObserver observer);

    /**
     * Removes a previously registered observer.
     *
     * @param observer
     *            the observer to remove
     */
    void removeObserver(WalletObserver observer);

    /**
     * Notifies all registered observers that the wallet state has changed.
     */
    void notifyObservers();
}
