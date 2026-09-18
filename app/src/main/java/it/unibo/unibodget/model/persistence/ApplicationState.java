package it.unibo.unibodget.model.persistence;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Immutable snapshot of the persistent application state.
 *
 * It contains:
 * - all cash wallets
 * - the currently selected wallet identifier
 * - the shared custom categories available across wallets
 */
public final class ApplicationState {

    private final List<CashAccount> cashWallets;
    private final UUID currentCashWalletId;
    private final List<Category> customCategories;

    public ApplicationState(
            final List<CashAccount> cashWallets,
            final UUID currentCashWalletId,
            final List<Category> customCategories) {
        this.cashWallets = List.copyOf(Objects.requireNonNull(cashWallets));
        this.currentCashWalletId = currentCashWalletId;
        this.customCategories = List.copyOf(Objects.requireNonNull(customCategories));
    }

    public static ApplicationState empty() {
        return new ApplicationState(List.of(), null, List.of());
    }

    public List<CashAccount> getCashWallets() {
        return cashWallets;
    }

    public UUID getCurrentCashWalletId() {
        return currentCashWalletId;
    }

    public List<Category> getCustomCategories() {
        return customCategories;
    }
}