package it.unibo.unibodget.model.persistence;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.wallet.CashAccount;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable snapshot of the persistent application state.
 *
 * <p>It contains: - all cash wallets - the currently selected wallet identifier - the shared custom
 * categories available across wallets
 */
public final class ApplicationState {

    private final List<CashAccount> cashWallets;
    private final UUID currentCashWalletId;
    private final List<Category> customCategories;

    /**
     * Creates a new {@code ApplicationState}.
     *
     * @param cashWallets the list of all cash wallets
     * @param currentCashWalletId the identifier of the currently selected wallet
     * @param customCategories the list of shared custom categories
     */
    public ApplicationState(
            final List<CashAccount> cashWallets,
            final UUID currentCashWalletId,
            final List<Category> customCategories) {
        this.cashWallets = List.copyOf(Objects.requireNonNull(cashWallets));
        this.currentCashWalletId = currentCashWalletId;
        this.customCategories = List.copyOf(Objects.requireNonNull(customCategories));
    }

    /**
     * Returns an empty application state with no wallets, no current wallet, and no custom
     * categories.
     *
     * @return an empty {@code ApplicationState}
     */
    public static ApplicationState empty() {
        return new ApplicationState(List.of(), null, List.of());
    }

    /**
     * Returns the list of all cash wallets.
     *
     * @return the list of cash wallets
     */
    public List<CashAccount> getCashWallets() {
        return cashWallets;
    }

    /**
     * Returns the identifier of the currently selected wallet.
     *
     * @return the current wallet identifier, or {@code null} if no wallet is selected
     */
    public UUID getCurrentCashWalletId() {
        return currentCashWalletId;
    }

    /**
     * Returns the list of shared custom categories.
     *
     * @return the list of custom categories
     */
    public List<Category> getCustomCategories() {
        return customCategories;
    }
}
