package it.unibo.unibodget.view.dashboard.api;

import java.util.UUID;

import it.unibo.unibodget.view.dashboard.state.DashboardDestination;
import it.unibo.unibodget.view.dashboard.state.TransactionFilterInput;

/**
 * Callback contract implemented by the dashboard controller.
 *
 * <p>
 * The view delegates all user interactions to this interface.
 * </p>
 */
public interface DashboardViewActions {

    /**
     * Called when the dashboard view is first shown.
     */
    void onViewOpened();

    /**
     * Called when the user selects a wallet from the sidebar.
     *
     * @param walletId
     *            the selected wallet identifier
     */
    void onWalletSelected(UUID walletId);

    /**
     * Called when the user requests navigation to another screen.
     *
     * @param destination
     *            the selected destination
     */
    void onNavigationRequested(DashboardDestination destination);

    /**
     * Called when the user explicitly requests the settings screen.
     */
    void onSettingsRequested();

    /**
     * Called when transaction filters are changed in the UI.
     *
     * @param input
     *            the new filter input
     */
    void onTransactionFiltersChanged(TransactionFilterInput input);

    /**
     * Called when the user requests transaction export.
     */
    void onExportTransactionsRequested();

    /**
     * Called when the user requests the creation of a new wallet.
     */
    void onCreateWalletRequested();

    /**
     * Called when the user requests the creation of a new transaction.
     */
    void onCreateTransactionRequested();

    /**
     * Called when the user requests editing the monthly budget of the currently
     * selected wallet.
     */
    void onEditBudgetRequested();
}