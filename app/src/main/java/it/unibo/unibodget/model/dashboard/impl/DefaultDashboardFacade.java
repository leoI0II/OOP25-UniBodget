package it.unibo.unibodget.model.dashboard.impl;

import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.dashboard.api.MovementHistoryService;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link DashboardFacade}.
 * <p>
 * This class coordinates the retrieval of dashboard data from the
 * underlying services and exposes it as a unified snapshot.
 */
public final class DefaultDashboardFacade implements DashboardFacade {

    private final MovementHistoryService movementHistoryService;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;

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
            final BudgetMonitor budgetMonitor) {
        this.movementHistoryService = movementHistoryService;
        this.categoryService = categoryService;
        this.budgetMonitor = budgetMonitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DashboardSnapshot loadDashboard() {
        final List<String> recentTransactions = movementHistoryService.getRecentTransactions();
        final Map<String, Double> summaries = categoryService.getCategorySummaries();
        final double totalBalance = summaries.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        final String budgetStatus = budgetMonitor.getBudgetStatus(totalBalance, 1000.0);

        return new DefaultDashboardSnapshot(
                totalBalance,
                recentTransactions,
                summaries,
                budgetStatus
        );
    }
}