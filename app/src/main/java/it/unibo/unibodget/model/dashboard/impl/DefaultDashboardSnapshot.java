package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Default immutable implementation of {@link DashboardSnapshot}.
 */
public final class DefaultDashboardSnapshot implements DashboardSnapshot {

    private final String walletName;
    private final String walletCurrency;
    private final BigDecimal totalBalance;
    private final List<Transaction> recentTransactions;
    private final Map<String, BigDecimal> categorySummaries;
    private final BigDecimal budgetLimit;
    private final BigDecimal warningThreshold;
    private final BudgetStatus budgetStatus;
    private final List<FriendLoanSummary> friendLoanSummaries;
    private final List<WalletInsight> walletInsights;

    /**
     * Creates a new dashboard snapshot.
     *
     * @param walletName
     *            the name of the currently selected wallet
     * @param walletCurrency
     *            the textual representation of the wallet base currency
     * @param totalBalance
     *            the total balance shown in the dashboard
     * @param recentTransactions
     *            the recent transactions shown in the dashboard
     * @param categorySummaries
     *            the aggregated values by category
     * @param budgetLimit
     *            the configured budget limit of the current wallet
     * @param warningThreshold
     *            the configured warning threshold of the current wallet
     * @param budgetStatus
     *            the current budget status
     * @param friendLoanSummaries
     *            the friend-loan summaries associated with the current wallet
     * @param walletInsights
     *            the dashboard insights associated with the current wallet
     */
    public DefaultDashboardSnapshot(
            final String walletName,
            final String walletCurrency,
            final BigDecimal totalBalance,
            final List<Transaction> recentTransactions,
            final Map<String, BigDecimal> categorySummaries,
            final BigDecimal budgetLimit,
            final BigDecimal warningThreshold,
            final BudgetStatus budgetStatus,
            final List<FriendLoanSummary> friendLoanSummaries,
            final List<WalletInsight> walletInsights) {
        this.walletName = Objects.requireNonNull(walletName);
        this.walletCurrency = Objects.requireNonNull(walletCurrency);
        this.totalBalance = Objects.requireNonNull(totalBalance);
        this.recentTransactions = List.copyOf(Objects.requireNonNull(recentTransactions));
        this.categorySummaries = Map.copyOf(Objects.requireNonNull(categorySummaries));
        this.budgetLimit = Objects.requireNonNull(budgetLimit);
        this.warningThreshold = Objects.requireNonNull(warningThreshold);
        this.budgetStatus = Objects.requireNonNull(budgetStatus);
        this.friendLoanSummaries = List.copyOf(Objects.requireNonNull(friendLoanSummaries));
        this.walletInsights = List.copyOf(Objects.requireNonNull(walletInsights));
    }

    @Override
    public String getWalletName() {
        return walletName;
    }

    @Override
    public String getWalletCurrency() {
        return walletCurrency;
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
    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    @Override
    public BigDecimal getWarningThreshold() {
        return warningThreshold;
    }

    @Override
    public BudgetStatus getBudgetStatus() {
        return budgetStatus;
    }

    @Override
    public List<FriendLoanSummary> getFriendLoanSummaries() {
        return Collections.unmodifiableList(friendLoanSummaries);
    }

    @Override
    public List<WalletInsight> getWalletInsights() {
        return Collections.unmodifiableList(walletInsights);
    }
}