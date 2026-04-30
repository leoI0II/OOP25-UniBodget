package it.unibo.unibodget.model.dashboard.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Represents an immutable aggregated view of the dashboard state.
 * The snapshot contains the information needed by the presentation
 * layer without exposing the internal details of the domain model.
 */
public interface DashboardSnapshot {

    /**
     * Returns the total balance currently shown by the dashboard.
     *
     * @return the total balance
     */
    BigDecimal getTotalBalance();

    /**
     * Returns the textual representation of recent transactions.
     *
     * @return the recent transactions shown by the dashboard
     */
    List<Transaction> getRecentTransactions();

    /**
     * Returns the aggregated amounts grouped by category.
     *
     * @return a map from category names to aggregated amounts
     */
    Map<String, BigDecimal> getCategorySummaries();

    /**
     * Returns the current budget status.
     *
     * @return the budget status
     */
    BudgetStatus getBudgetStatus();
}