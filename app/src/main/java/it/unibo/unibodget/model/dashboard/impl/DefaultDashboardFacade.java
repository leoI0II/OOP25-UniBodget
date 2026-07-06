package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.service.WalletService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.Transaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Default implementation of {@link DashboardFacade}.
 *
 * <p>This class coordinates the services involved in the dashboard subsystem and
 * exposes a single method that returns a consistent snapshot of the current state.</p>
 */
public final class DefaultDashboardFacade implements DashboardFacade {

    private final WalletService<CashTransaction, CashAccount> walletService;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;
    private final BudgetSettings budgetSettings;

    /**
     * Creates a new dashboard facade with the required collaborating services.
     *
     * @param walletService
     *            the service exposing wallets and the current wallet history
     * @param categoryService
     *            the service exposing the aggregated values by category
     * @param budgetMonitor
     *            the component evaluating the current budget status
     * @param budgetSettings
     *            the user-defined budget configuration
     */
    public DefaultDashboardFacade(
            final WalletService<CashTransaction, CashAccount> walletService,
            final CategoryService categoryService,
            final BudgetMonitor budgetMonitor,
            final BudgetSettings budgetSettings) {
        this.walletService = Objects.requireNonNull(walletService);
        this.categoryService = Objects.requireNonNull(categoryService);
        this.budgetMonitor = Objects.requireNonNull(budgetMonitor);
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DashboardSnapshot loadDashboard() {
        final List<Transaction> recentTransactions = List.copyOf(walletService.getCurrentTransactions());
        final BigDecimal totalBalance = walletService.getCurrentWallet()
                .map(wallet -> wallet.getBalance().amount())
                .orElse(BigDecimal.ZERO);
        final Map<String, BigDecimal> categorySummaries = categoryService.getCategorySummaries();
        final BudgetStatus budgetStatus = budgetMonitor.getBudgetStatus(totalBalance, budgetSettings);

        return new DefaultDashboardSnapshot(
                totalBalance,
                recentTransactions,
                categorySummaries,
                budgetStatus);
    }
}