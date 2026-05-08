package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.dashboard.api.WalletService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;

/**
 * Default implementation of {@link DashboardFacade}.
 *
 * This class coordinates the services involved in the dashboard subsystem and
 * exposes a single method that returns a consistent snapshot of the current state.
 */
public final class DefaultDashboardFacade implements DashboardFacade {

    private final WalletService walletService;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;
    private final BudgetSettings budgetSettings;

    /**
     * Creates a new dashboard facade with the required collaborating services.
     *
     * @param walletService the service exposing wallets and the current wallet history
     * @param categoryService the service exposing the aggregated values by category
     * @param budgetMonitor the component evaluating the current budget status
     * @param budgetSettings the user-defined budget configuration
     */
    public DefaultDashboardFacade(
            final WalletService walletService,
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
        final List<CashTransaction> recentTransactions = this.walletService.getCurrentTransactions();
        final Map<String, BigDecimal> categorySummaries = this.categoryService.getCategorySummaries();

        final BigDecimal totalBalance = recentTransactions.stream()
                .map(CashTransaction::getAsset)
                .map(Asset::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal currentExpenseValue = categorySummaries.values().stream()
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BudgetStatus budgetStatus = this.budgetMonitor.getBudgetStatus(currentExpenseValue, this.budgetSettings);

        final BigDecimal friendLoanGivenTotal = recentTransactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.FRIEND_LOAN)
                .map(CashTransaction::getAsset)
                .map(Asset::amount)
                .filter(amount -> amount.signum() < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal friendLoanReceivedTotal = recentTransactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.FRIEND_LOAN)
                .map(CashTransaction::getAsset)
                .map(Asset::amount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal friendLoanNetBalance = friendLoanGivenTotal.subtract(friendLoanReceivedTotal);

        final BigDecimal bankLoanTotal = recentTransactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.BANK_LOAN)
                .map(CashTransaction::getAsset)
                .map(Asset::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Map<String, BigDecimal> topCategories = categorySummaries.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(
                        Comparator.comparing(BigDecimal::abs)).reversed())
                .limit(4)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        return new DefaultDashboardSnapshot(
                totalBalance,
                recentTransactions,
                categorySummaries,
                budgetStatus,
                friendLoanGivenTotal,
                friendLoanReceivedTotal,
                friendLoanNetBalance,
                bankLoanTotal,
                topCategories
        );
    }
}
