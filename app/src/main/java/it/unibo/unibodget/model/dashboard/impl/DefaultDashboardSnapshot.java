package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Default immutable implementation of {@link DashboardSnapshot}.
 */
public final class DefaultDashboardSnapshot implements DashboardSnapshot {

    private final BigDecimal totalBalance;
    private final List<Transaction> recentTransactions;
    private final Map<String, BigDecimal> categorySummaries;
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
            final BigDecimal totalBalance,
            final List<Transaction> recentTransactions,
            final Map<String, BigDecimal> categorySummaries,
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
    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getRecentTransactions() {
        return Collections.unmodifiableList(recentTransactions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, BigDecimal> getCategorySummaries() {
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