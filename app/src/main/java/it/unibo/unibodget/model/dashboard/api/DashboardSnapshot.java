package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Immutable read model containing the information required by the dashboard.
 *
 * A snapshot groups together the current total balance, the recent transactions,
 * the category summaries, the current budget status, and the derived values
 * used by advanced dashboard widgets.
 */
public interface DashboardSnapshot {

    /**
     * Returns the total balance shown in the dashboard.
     *
     * @return the total dashboard balance
     */
    BigDecimal getTotalBalance();

    /**
     * Returns the recent transactions shown in the dashboard.
     *
     * @return the recent transactions
     */
    List<Transaction> getRecentTransactions();

    /**
     * Returns the aggregated amounts grouped by category.
     *
     * @return the category summaries
     */
    Map<String, BigDecimal> getCategorySummaries();

    /**
     * Returns the current budget status.
     *
     * @return the current budget status
     */
    BudgetStatus getBudgetStatus();

    /**
     * Returns the total amount of money lent to friends.
     *
     * @return the total friend loans given
     */
    BigDecimal getFriendLoanGivenTotal();

    /**
     * Returns the total amount of money borrowed from friends.
     *
     * @return the total friend loans received
     */
    BigDecimal getFriendLoanReceivedTotal();

    /**
     * Returns the net balance of friend loans.
     * A positive value means that friends owe money to the user.
     * A negative value means that the user owes money to friends.
     *
     * @return the net friend loan balance
     */
    BigDecimal getFriendLoanNetBalance();

    /**
     * Returns the aggregated total amount associated with bank loans.
     *
     * @return the total bank loan amount
     */
    BigDecimal getBankLoanTotal();

    /**
     * Returns the top category summaries ordered by absolute value descending.
     *
     * @return the top category summaries
     */
    Map<String, BigDecimal> getTopCategories();
}
