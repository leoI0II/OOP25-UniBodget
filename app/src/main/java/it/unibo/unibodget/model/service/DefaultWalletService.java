package it.unibo.unibodget.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import it.unibo.unibodget.model.dashboard.support.WalletObserver;
import it.unibo.unibodget.model.transactions.base.AbstractTransaction;
import it.unibo.unibodget.model.wallet.AbstractWallet;

/**
 * Default implementation of {@link WalletService}.
 *
 * <p>This service stores the wallets available to the user, keeps track of the
 * currently selected wallet, and delegates transaction and observer operations
 * to the appropriate wallet or history.</p>
 *
 * @param <T> the type of transaction managed by the wallets
 * @param <W> the concrete wallet type, must extend {@link AbstractWallet}
 */
public class DefaultWalletService<T extends AbstractTransaction, W extends AbstractWallet<T>> implements WalletService<T, W> {

    private final List<W> wallets = new ArrayList<>();
    private final List<WalletObserver> observers = new ArrayList<>();
    private W currentWallet;

    /**
     * Creates an empty wallet service.
     */
    public DefaultWalletService() { }

    /**
     * Creates a wallet service initialized with the provided wallets.
     *
     * @param initialWallets
     *            the initial wallets
     */
    public DefaultWalletService(final List<W> initialWallets) {
        this.wallets.addAll(Objects.requireNonNull(initialWallets));
        this.currentWallet = this.wallets.isEmpty() ? null : this.wallets.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<W> getWallets() {
        return List.copyOf(wallets);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<W> getCurrentWallet() {
        return Optional.ofNullable(currentWallet);
    }

    /** {@inheritDoc} */
    @Override
    public void addWallet(final W wallet) {
        wallets.add(Objects.requireNonNull(wallet));
        if (currentWallet == null) {
            currentWallet = wallet;
        }
        notifyObservers();
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeWallet(final UUID walletId) {
        final UUID nonNullWalletId = Objects.requireNonNull(walletId);
        final Optional<W> walletToRemove = wallets.stream()
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

    /** {@inheritDoc} */
    @Override
    public boolean selectWallet(final UUID walletId) {
        final UUID nonNullWalletId = Objects.requireNonNull(walletId);
        final Optional<W> selectedWallet = wallets.stream()
                .filter(wallet -> wallet.getId().equals(nonNullWalletId))
                .findFirst();

        if (selectedWallet.isEmpty()) {
            return false;
        }

        currentWallet = selectedWallet.get();
        notifyObservers();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<T> getCurrentTransactions() {
        return List.copyOf(getRequiredCurrentWallet().getHistory().getTransactions());
    }

    /** {@inheritDoc} */
    @Override
    public void addTransaction(final T transaction) {
        getRequiredCurrentWallet().addTransaction(Objects.requireNonNull(transaction));
        notifyObservers();
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeTransaction(final T transaction) {
        final boolean removed = getRequiredCurrentWallet()
                .getHistory()
                .removeTransaction(Objects.requireNonNull(transaction));
        if (removed) {
            notifyObservers();
        }
        return removed;
    }

    /** {@inheritDoc} */
    @Override
    public void replaceTransaction(final T oldTransaction, final T newTransaction) {
        final boolean replaced = getRequiredCurrentWallet()
                .getHistory()
                .replaceTransaction(
                        Objects.requireNonNull(oldTransaction),
                        Objects.requireNonNull(newTransaction));
        if (replaced) {
            notifyObservers();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearCurrentWalletHistory() {
        getRequiredCurrentWallet().getHistory().clear();
        notifyObservers();
    }

    /** {@inheritDoc} */
    @Override
    public void addObserver(final WalletObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    /** {@inheritDoc} */
    @Override
    public void removeObserver(final WalletObserver observer) {
        observers.remove(Objects.requireNonNull(observer));
    }

    /** {@inheritDoc} */
    @Override
    public void notifyObservers() {
        observers.forEach(WalletObserver::update);
    }

    /**
     * Returns the current wallet, throwing an exception if no wallet is selected.
     *
     * @return the currently selected wallet
     * @throws IllegalStateException if no wallet has been selected yet
     */
    private W getRequiredCurrentWallet() {
        return Optional.ofNullable(currentWallet)
                .orElseThrow(() -> new IllegalStateException("No current wallet selected."));
    }
}
