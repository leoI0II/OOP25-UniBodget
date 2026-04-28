package it.unibo.unibodget.model.dashboard.api;

import java.util.List;
import java.util.Map;

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
    double getTotalBalance();

    /**
     * Returns the textual representation of recent transactions.
     *
     * @return the recent transactions shown by the dashboard
     */
    List<String> getRecentTransactions();

    /**
     * Returns the aggregated amounts grouped by category.
     *
     * @return a map from category names to aggregated amounts
     */
    Map<String, Double> getCategorySummaries();

    /**
     * Returns the current budget status.
     *
     * @return the budget status
     */
    BudgetStatus getBudgetStatus();
}