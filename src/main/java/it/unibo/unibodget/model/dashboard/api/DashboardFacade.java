package it.unibo.unibodget.model.dashboard.api;

import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.wallet.CashAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade exposing the dashboard read model and shared category data required by
 * dashboard-related user flows.
 */
public interface DashboardFacade {

    /**
     * Loads the current dashboard snapshot.
     *
     * @return the immutable snapshot representing the current dashboard state
     */
    DashboardSnapshot loadDashboard();

    /**
     * Returns the shared category catalog used by dashboard flows such as
     * transaction creation.
     *
     * @return the shared category catalog
     */
    CategoryCatalog getCategoryCatalog();

    /**
     * Returns all available cash accounts.
     *
     * @return the list of all cash accounts
     */
    List<CashAccount> getAllCashAccounts();

    /**
     * Returns the currently selected cash account, if present.
     *
     * @return an optional containing the currently selected cash account
     */
    Optional<CashAccount> getCurrentSelectedCashAccount();

    /**
     * Computes the aggregated balance converted into the base currency.
     *
     * @return the total balance asset
     */
    Asset getAggregatedBalance();

    /**
     * Selects the cash account matching the specified identifier.
     *
     * @param id
     *            the identifier of the wallet to select
     */
    void selectWallet(UUID id);
}
