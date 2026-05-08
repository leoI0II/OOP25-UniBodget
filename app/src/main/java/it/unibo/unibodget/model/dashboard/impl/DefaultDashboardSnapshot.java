package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Default immutable implementation of {@link DashboardSnapshot}.
 */
public final class DefaultDashboardSnapshot implements DashboardSnapshot {

    private final BigDecimal totalBalance;
    private final List<Transaction> recentTransactions;
    private final Map<String, BigDecimal> categorySummaries;
    private final BudgetStatus budgetStatus;

    private final BigDecimal friendLoanGivenTotal;
    private final BigDecimal friendLoanReceivedTotal;
    private final BigDecimal friendLoanNetBalance;
    private final BigDecimal bankLoanTotal;
    private final Map<String, BigDecimal> topCategories;

    /**
     * Creates a new dashboard snapshot.
     *
     * @param totalBalance the total balance shown in the dashboard
     * @param recentTransactions the recent transactions shown in the dashboard
     * @param categorySummaries the aggregated values by category
     * @param budgetStatus the current budget status
     * @param friendLoanGivenTotal the total amount lent to friends
     * @param friendLoanReceivedTotal the total amount borrowed from friends
     * @param friendLoanNetBalance the net balance of friend loans
     * @param bankLoanTotal the total amount associated with bank loans
     * @param topCategories the top category summaries
     */
    public DefaultDashboardSnapshot(
            final BigDecimal totalBalance,
            final List<CashTransaction> recentTransactions,
            final Map<String, BigDecimal> categorySummaries,
            final BudgetStatus budgetStatus,
            final BigDecimal friendLoanGivenTotal,
            final BigDecimal friendLoanReceivedTotal,
            final BigDecimal friendLoanNetBalance,
            final BigDecimal bankLoanTotal,
            final Map<String, BigDecimal> topCategories) {
        this.totalBalance = totalBalance;
        this.recentTransactions = List.copyOf(recentTransactions);
        this.categorySummaries = Map.copyOf(categorySummaries);
        this.budgetStatus = budgetStatus;
        this.friendLoanGivenTotal = friendLoanGivenTotal;
        this.friendLoanReceivedTotal = friendLoanReceivedTotal;
        this.friendLoanNetBalance = friendLoanNetBalance;
        this.bankLoanTotal = bankLoanTotal;
        this.topCategories = Map.copyOf(topCategories);
    }

    @Override
    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    @Override
    public List<Transaction> getRecentTransactions() {
        return Collections.unmodifiableList(recentTransactions);
    }

    @Override
    public Map<String, BigDecimal> getCategorySummaries() {
        return Collections.unmodifiableMap(categorySummaries);
    }

    @Override
    public BudgetStatus getBudgetStatus() {
        return budgetStatus;
    }

    @Override
    public BigDecimal getFriendLoanGivenTotal() {
        return friendLoanGivenTotal;
    }

    @Override
    public BigDecimal getFriendLoanReceivedTotal() {
        return friendLoanReceivedTotal;
    }

    @Override
    public BigDecimal getFriendLoanNetBalance() {
        return friendLoanNetBalance;
    }

    @Override
    public BigDecimal getBankLoanTotal() {
        return bankLoanTotal;
    }

    @Override
    public Map<String, BigDecimal> getTopCategories() {
        return Collections.unmodifiableMap(topCategories);
    }
}
