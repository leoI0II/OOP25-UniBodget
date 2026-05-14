package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.dashboard.impl.FriendLoanSummary;
import it.unibo.unibodget.model.dashboard.impl.WalletInsight;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Immutable snapshot of the information required by the dashboard view.
 *
 * <p>
 * A snapshot represents a coherent view of the currently selected wallet,
 * including identity, balance, recent transactions, category summaries, budget
 * configuration, and budget status.</p>
 */
public interface DashboardSnapshot {

    /**
     * Returns the name of the currently selected wallet.
     *
     * @return the current wallet name
     */
    String getWalletName();

    /**
     * Returns the textual representation of the current wallet base currency.
     *
     * @return the wallet currency
     */
    String getWalletCurrency();

    /**
     * Returns the total balance of the current wallet.
     *
     * @return the total balance
     */
    BigDecimal getTotalBalance();

    /**
     * Returns the recent transactions associated with the current wallet.
     *
     * @return the recent transactions
     */
    List<Transaction> getRecentTransactions();

    /**
     * Returns the aggregated amounts by category.
     *
     * @return the category summaries
     */
    Map<String, BigDecimal> getCategorySummaries();

    /**
     * Returns the configured budget limit of the current wallet.
     *
     * @return the budget limit
     */
    BigDecimal getBudgetLimit();

    /**
     * Returns the configured warning threshold of the current wallet.
     *
     * @return the warning threshold
     */
    BigDecimal getWarningThreshold();

    /**
     * Returns the current budget status.
     *
     * @return the budget status
     */
    BudgetStatus getBudgetStatus();

    /**
     * Returns the friend-loan summaries associated with the current wallet.
     *
     * @return the friend-loan summaries
     */
    List<FriendLoanSummary> getFriendLoanSummaries();

    /**
     * Returns the wallet insights associated with the current wallet.
     *
     * <p>
     * This method is defined as a default method to preserve compatibility with
     * existing implementations of the interface.</p>
     *
     * @return the wallet insights, or an empty list when not available
     */
    default List<WalletInsight> getWalletInsights() {
        return List.of();
    }
}
