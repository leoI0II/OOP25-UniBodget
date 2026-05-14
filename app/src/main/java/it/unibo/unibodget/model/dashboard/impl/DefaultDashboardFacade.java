package it.unibo.unibodget.model.dashboard.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetStatus;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.DashboardSnapshot;
import it.unibo.unibodget.model.dashboard.api.FriendLoanSummaryService;
import it.unibo.unibodget.model.dashboard.api.WalletInsightService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.transactions.base.Transaction;
import it.unibo.unibodget.model.wallet.CashAccount;

/**
 * Default implementation of {@link DashboardFacade}.
 *
 * <p>
 * This class coordinates the services involved in the dashboard subsystem and
 * exposes a single method that returns a consistent snapshot of the current
 * dashboard state for the selected cash wallet.</p>
 */
public final class DefaultDashboardFacade implements DashboardFacade {

    private final CashAccountService walletService;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;
    private final FriendLoanSummaryService friendLoanSummaryService;
    private final WalletInsightService walletInsightService;

    /**
     * Creates a new dashboard facade with the required collaborating services.
     *
     * @param walletService the service exposing cash wallets and the current
     * wallet history
     * @param categoryService the service exposing aggregated values by category
     * @param budgetMonitor the component evaluating the current budget status
     * @param friendLoanSummaryService the service computing friend-loan
     * summaries
     * @param walletInsightService the service computing dashboard insights
     */
    public DefaultDashboardFacade(
            final CashAccountService walletService,
            final CategoryService categoryService,
            final BudgetMonitor budgetMonitor,
            final FriendLoanSummaryService friendLoanSummaryService,
            final WalletInsightService walletInsightService) {
        this.walletService = Objects.requireNonNull(walletService);
        this.categoryService = Objects.requireNonNull(categoryService);
        this.budgetMonitor = Objects.requireNonNull(budgetMonitor);
        this.friendLoanSummaryService = Objects.requireNonNull(friendLoanSummaryService);
        this.walletInsightService = Objects.requireNonNull(walletInsightService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DashboardSnapshot loadDashboard() {
        final CashAccount currentWallet = walletService.getCurrentWallet()
                .orElseThrow(() -> new IllegalStateException("No wallet is currently selected."));

        final List<CashTransaction> currentTransactions = walletService.getCurrentTransactions();
        categoryService.recomputeFromTransactions(currentTransactions);

        final DefaultBudgetSettings settings = currentWallet.getBudgetSettings();

        final BigDecimal monthlyBudgetUsage = computeCurrentMonthBudgetUsage(currentTransactions);
        final BudgetStatus budgetStatus
                = budgetMonitor.getBudgetStatus(monthlyBudgetUsage, settings);

        final List<FriendLoanSummary> friendLoanSummaries
                = friendLoanSummaryService.summarize(currentTransactions);

        final List<WalletInsight> walletInsights
                = walletInsightService.computeInsights(currentTransactions);

        return new DefaultDashboardSnapshot(
                currentWallet.getName(),
                currentWallet.getBaseCurrency().toString(),
                currentWallet.getBalance().amount(),
                currentTransactions.stream()
                        .map(Transaction.class::cast)
                        .toList(),
                categoryService.getCategorySummaries(),
                settings.getLimitValue(),
                settings.getWarningThreshold(),
                budgetStatus,
                friendLoanSummaries,
                walletInsights
        );
    }

    /**
     * Computes the amount contributing to the current monthly budget.
     *
     * <p>
     * Only transactions in the current month and current year whose category
     * type is {@link CategoryType#EXPENSE} or {@link CategoryType#FRIEND_LOAN}
     * are counted. Each matching transaction contributes its absolute
     * amount.</p>
     *
     * @param transactions the transactions to inspect
     * @return the total amount contributing to the current monthly budget
     */
    private BigDecimal computeCurrentMonthBudgetUsage(final List<CashTransaction> transactions) {
        final LocalDate now = LocalDate.now();

        return transactions.stream()
                .filter(transaction -> transaction.getDate().getYear() == now.getYear())
                .filter(transaction -> transaction.getDate().getMonth() == now.getMonth())
                .filter(transaction -> {
                    final CategoryType type = transaction.getCategory().getType();
                    return type == CategoryType.EXPENSE || type == CategoryType.FRIEND_LOAN;
                })
                .map(transaction -> transaction.getAsset().amount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
