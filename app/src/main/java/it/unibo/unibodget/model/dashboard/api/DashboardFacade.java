package it.unibo.unibodget.model.dashboard.api;

import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.wallet.CashAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade exposing the dashboard read model and shared category data required by dashboard-related
 * user flows.
 */
public interface DashboardFacade {

    /**
     * Loads the current dashboard snapshot.
     *
     * @return the immutable snapshot representing the current dashboard state
     */
    DashboardSnapshot loadDashboard();

    /**
     * Returns the shared category catalog used by dashboard flows such as transaction creation.
     *
     * @return the shared category catalog
     */
    CategoryCatalog getCategoryCatalog();

    /**
     * Returns all cash accounts.
     *
     * @return list of cash accounts
     */
    List<CashAccount> getAllCashAccounts();

    /**
     * Returns the currently selected cash account if any.
     *
     * @return optional containing the selected cash account, or empty
     */
    Optional<CashAccount> getCurrentSelectedCashAccount();

    /**
     * Returns the aggregated balance of all accounts.
     *
     * @return the aggregated asset balance
     */
    Asset getAggregatedBalance();

    /**
     * Selects a wallet by its unique identifier.
     *
     * @param id the wallet identifier
     */
    void selectWallet(UUID id);
}
