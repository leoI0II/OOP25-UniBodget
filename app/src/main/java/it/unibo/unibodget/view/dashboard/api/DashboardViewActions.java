package it.unibo.unibodget.view.dashboard.api;

import java.util.UUID;

import it.unibo.unibodget.view.dashboard.state.DashboardDestination;
import it.unibo.unibodget.view.dashboard.state.TransactionFilterInput;

/**
 * Action callbacks exposed by the dashboard controller to the dashboard view.
 *
 * <p>
 * The view must remain passive and delegate user interactions through this
 * interface. The controller implements this contract and decides how each user
 * action is handled.
 * </p>
 *
 * <p>
 * Default methods are used for newly introduced actions so that older view
 * implementations can remain source-compatible and binary-compatible without
 * being forced to implement every new callback immediately. [web:477][web:617]
 * </p>
 */
public interface DashboardViewActions {

    /**
     * Called when the dashboard view is shown and must load its initial data.
     */
    void onViewOpened();

    /**
     * Called when a wallet is selected from the sidebar.
     *
     * @param walletId
     *            the identifier of the selected wallet; must not be
     *            {@code null}
     */
    void onWalletSelected(UUID walletId);

    /**
     * Called when navigation to a dashboard destination is requested.
     *
     * @param destination
     *            the requested destination; must not be {@code null}
     */
    void onNavigationRequested(DashboardDestination destination);

    /**
     * Called when the settings area is requested directly.
     */
    void onSettingsRequested();

    /**
     * Called when the transaction filter input changes.
     *
     * @param input
     *            the new transaction filter input; must not be {@code null}
     */
    void onTransactionFiltersChanged(TransactionFilterInput input);

    /**
     * Called when transaction export is requested.
     */
    void onExportTransactionsRequested();

    /**
     * Called when wallet creation is requested.
     */
    void onCreateWalletRequested();

    /**
     * Called when transaction creation is requested.
     */
    void onCreateTransactionRequested();

    /**
     * Called when budget editing is requested.
     */
    void onEditBudgetRequested();

    /**
     * Called when editing of a transaction row is requested.
     *
     * <p>
     * This default implementation is intentionally empty to preserve backward
     * compatibility with existing implementations. [web:477][web:617]
     * </p>
     *
     * @param transactionRowId
     *            the identifier of the transaction represented by the selected
     *            row
     */
    default void onEditTransactionRequested(final UUID transactionRowId) {
    }

    /**
     * Called when deletion of a transaction row is requested.
     *
     * <p>
     * This default implementation is intentionally empty to preserve backward
     * compatibility with existing implementations. [web:477][web:617]
     * </p>
     *
     * @param transactionRowId
     *            the identifier of the transaction represented by the selected
     *            row
     */
    default void onDeleteTransactionRequested(final UUID transactionRowId) {
    }

    /**
     * Called when category management is requested.
     *
     * <p>
     * This default implementation is intentionally empty to preserve backward
     * compatibility with existing implementations. [web:477][web:617]
     * </p>
     */
    default void onManageCategoriesRequested() {
    }
}