package it.unibo.unibodget.model.dashboard.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;

/**
 * Default immutable implementation of {@link DashboardSnapshot}.
 */
public final class DefaultDashboardSnapshot implements DashboardSnapshot {

    private final double totalBalance;
    private final List<String> recentTransactions;
    private final Map<String, Double> categorySummaries;
    private final BudgetStatus budgetStatus;

    /**
     * Creates a new dashboard snapshot.
     *
     * @param totalBalance the total balance shown in the dashboard
     * @param recentTransactions the recent transactions shown in the dashboard
     * @param categorySummaries the aggregated values by category
     * @param budgetStatus the current budget status
     */
    public DefaultDashboardSnapshot(
            final double totalBalance,
            final List<String> recentTransactions,
            final Map<String, Double> categorySummaries,
            final BudgetStatus budgetStatus) {
        this.totalBalance = totalBalance;
        this.recentTransactions = List.copyOf(recentTransactions);
        this.categorySummaries = Map.copyOf(categorySummaries);
        this.budgetStatus = budgetStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTotalBalance() {
        return totalBalance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRecentTransactions() {
        return Collections.unmodifiableList(recentTransactions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getCategorySummaries() {
        return Collections.unmodifiableMap(categorySummaries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BudgetStatus getBudgetStatus() {
        return budgetStatus;
    }
}