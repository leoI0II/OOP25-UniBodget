package it.unibo.unibodget.model.dashboard.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import it.unibo.unibodget.model.dashboard.api.WalletService;
import it.unibo.unibodget.model.dashboard.support.WalletObserver;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Default implementation of {@link WalletService}.
 *
 * <p>This service stores the wallets available to the user, keeps track of the
 * currently selected wallet, and delegates transaction updates to the history
 * owned by that wallet.</p>
 */
public final class DefaultWalletService implements WalletService {

    private final List<CashAccount> wallets = new ArrayList<>();
    private final List<WalletObserver> observers = new ArrayList<>();
    private CashAccount currentWallet;

    /**
     * Creates an empty wallet service.
     */
    public DefaultWalletService() {
    }

    /**
     * Creates a wallet service initialized with the provided wallets.
     *
     * @param initialWallets
     *            the initial wallets
     */
    public DefaultWalletService(final List<CashAccount> initialWallets) {
        this.wallets.addAll(Objects.requireNonNull(initialWallets));
        this.currentWallet = this.wallets.isEmpty() ? null : this.wallets.get(0);
    }

    @Override
    public List<CashAccount> getWallets() {
        return List.copyOf(wallets);
    }

    @Override
    public Optional<CashAccount> getCurrentWallet() {
        return Optional.ofNullable(currentWallet);
    }

    @Override
    public void addWallet(final CashAccount wallet) {
        wallets.add(Objects.requireNonNull(wallet));
        if (currentWallet == null) {
            currentWallet = wallet;
        }
        notifyObservers();
    }

    @Override
    public boolean removeWallet(final UUID walletId) {
        final UUID nonNullWalletId = Objects.requireNonNull(walletId);
        final Optional<CashAccount> walletToRemove = wallets.stream()
                .filter(wallet -> wallet.getId().equals(nonNullWalletId))
                .findFirst();

        if (walletToRemove.isEmpty()) {
            return false;
        }

        final boolean removed = wallets.remove(walletToRemove.get());
        if (removed && Objects.equals(currentWallet, walletToRemove.get())) {
            currentWallet = wallets.isEmpty() ? null : wallets.get(0);
        }
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    @Override
    public boolean selectWallet(final UUID walletId) {
        final UUID nonNullWalletId = Objects.requireNonNull(walletId);
        final Optional<CashAccount> selectedWallet = wallets.stream()
                .filter(wallet -> wallet.getId().equals(nonNullWalletId))
                .findFirst();

        if (selectedWallet.isEmpty()) {
            return false;
        }

        currentWallet = selectedWallet.get();
        notifyObservers();
        return true;
    }

    @Override
    public List<CashTransaction> getCurrentTransactions() {
        return List.copyOf(getRequiredCurrentWallet().getHistory().getTransactions());
    }

    @Override
    public void addTransaction(final CashTransaction transaction) {
        getRequiredCurrentWallet().addTransaction(Objects.requireNonNull(transaction));
        notifyObservers();
    }

    @Override
    public boolean removeTransaction(final CashTransaction transaction) {
        final boolean removed = getRequiredCurrentWallet()
                .getHistory()
                .removeTransaction(Objects.requireNonNull(transaction));
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    @Override
    public void replaceTransaction(final CashTransaction oldTransaction, final CashTransaction newTransaction) {
        final boolean replaced = getRequiredCurrentWallet()
                .getHistory()
                .replaceTransaction(
                        Objects.requireNonNull(oldTransaction),
                        Objects.requireNonNull(newTransaction));
        if (replaced) {
            notifyObservers();
        }
    }

    @Override
    public void clearCurrentWalletHistory() {
        getRequiredCurrentWallet().getHistory().clear();
        notifyObservers();
    }

    @Override
    public void addObserver(final WalletObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    @Override
    public void removeObserver(final WalletObserver observer) {
        observers.remove(Objects.requireNonNull(observer));
    }

    @Override
    public void notifyObservers() {
        observers.forEach(WalletObserver::update);
    }

    /**
     * Returns the current wallet, throwing an exception if no wallet is selected.
     *
     * @return the current wallet
     */
    private CashAccount getRequiredCurrentWallet() {
        return Optional.ofNullable(currentWallet)
                .orElseThrow(() -> new IllegalStateException("No current wallet selected."));
    }
}