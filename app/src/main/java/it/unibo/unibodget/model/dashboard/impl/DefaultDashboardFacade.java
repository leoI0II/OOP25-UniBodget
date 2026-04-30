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
import it.unibo.unibodget.model.dashboard.api.MovementHistoryService;
import it.unibo.unibodget.model.transactions.base.Transaction;

/**
 * Default implementation of {@link DashboardFacade}.
 * This class coordinates the retrieval of dashboard data from the
 * underlying services and exposes it as a unified snapshot.
 */
public final class DefaultDashboardFacade implements DashboardFacade {

    private final MovementHistoryService movementHistoryService;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;
    private final BudgetSettings budgetSettings;
    /**
     * Creates a facade using the provided collaborating services.
     *
     * @param movementHistoryService the service responsible for recent transactions
     * @param categoryService the service responsible for category summaries
     * @param budgetMonitor the service responsible for budget evaluation
     */
    public DefaultDashboardFacade(
            final MovementHistoryService movementHistoryService,
            final CategoryService categoryService,
            final BudgetMonitor budgetMonitor,
            final BudgetSettings budgetSettings) {
        this.movementHistoryService = Objects.requireNonNull(movementHistoryService);
        this.categoryService = Objects.requireNonNull(categoryService);
        this.budgetMonitor = Objects.requireNonNull(budgetMonitor);
        this.budgetSettings = Objects.requireNonNull(budgetSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DashboardSnapshot loadDashboard() {
        final List<Transaction> recentTransactions = movementHistoryService.getRecentTransactions();
        final Map<String, BigDecimal> summaries = categoryService.getCategorySummaries();
       final BigDecimal totalBalance = summaries.values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
        final BudgetStatus budgetStatus = budgetMonitor.getBudgetStatus(totalBalance, budgetSettings);

        return new DefaultDashboardSnapshot(
                totalBalance,
                recentTransactions,
                summaries,
                budgetStatus
        );
    }
}