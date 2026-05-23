package it.unibo.unibodget.app;

import it.unibo.unibodget.controller.dashboard.impl.DefaultDashboardController;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.FriendLoanSummaryService;
import it.unibo.unibodget.model.dashboard.api.WalletInsightService;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetMonitor;
import it.unibo.unibodget.model.dashboard.impl.DefaultCategoryService;
import it.unibo.unibodget.model.dashboard.impl.DefaultDashboardFacade;
import it.unibo.unibodget.model.dashboard.impl.DefaultFriendLoanSummaryService;
import it.unibo.unibodget.model.dashboard.impl.DefaultTotalCashBalanceService;
import it.unibo.unibodget.model.dashboard.impl.DefaultWalletInsightService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.transactions.base.CashTransactionFactory;
import it.unibo.unibodget.view.dashboard.impl.DefaultDashboardView;

/**
 * Bootstrap class wiring together the in-memory dashboard dependencies used by
 * the demo application.
 */
final class InMemoryDashboardBootstrap {

    private final Settings settings;
    private final CashAccountService cashAccountService;
    private final CategoryCatalog categoryCatalog;
    private final CategoryService categoryService;
    private final BudgetMonitor budgetMonitor;
    private final FriendLoanSummaryService friendLoanSummaryService;
    private final WalletInsightService walletInsightService;
    private final DashboardFacade dashboardFacade;
    private final DefaultTotalCashBalanceService totalCashBalanceService;
    private final CashTransactionFactory cashTransactionFactory;
    private final DefaultDashboardView dashboardView;
    private final DefaultDashboardController dashboardController;

    /**
     * Creates and wires the in-memory dashboard application graph.
     */
    private InMemoryDashboardBootstrap() {
        this.settings = new Settings();
        ThemeManager.setTheme(settings.getTheme());

        this.cashAccountService = new CashAccountService();
        this.categoryCatalog = new CategoryCatalog();
        this.categoryService = new DefaultCategoryService();
        this.budgetMonitor = new DefaultBudgetMonitor();
        this.friendLoanSummaryService = new DefaultFriendLoanSummaryService();
        this.walletInsightService = new DefaultWalletInsightService();
        this.cashTransactionFactory = new CashTransactionFactory();

        this.dashboardFacade = new DefaultDashboardFacade(
                cashAccountService,
                categoryService,
                budgetMonitor,
                friendLoanSummaryService,
                walletInsightService,
                categoryCatalog
        );

        this.totalCashBalanceService = new DefaultTotalCashBalanceService(
                cashAccountService,
                new InMemoryCashBalanceConverter()
        );

        this.dashboardView = new DefaultDashboardView();
        this.dashboardController = new DefaultDashboardController(
                dashboardView,
                dashboardFacade,
                cashAccountService,
                totalCashBalanceService,
                settings,
                cashTransactionFactory
        );

        dashboardController.init();
    }

    /**
     * Creates the main application shell backed by the in-memory bootstrap.
     *
     * @return the configured main shell
     */
    static MainAppShell createShell() {
        return new MainAppShell(new InMemoryDashboardBootstrap());
    }

    /**
     * Returns the dashboard view managed by this bootstrap.
     *
     * @return the dashboard view
     */
    DefaultDashboardView dashboardView() {
        return dashboardView;
    }

    /**
     * Shows the correct initial screen depending on whether any wallet already
     * exists.
     *
     * @param navigator
     *            the application navigator
     */
    void showFirstBootIfNeeded(final AppNavigator navigator) {
        if (cashAccountService.getWallets().isEmpty()) {
            navigator.showFirstBoot();
        } else {
            navigator.showDashboard();
        }
    }

    /**
     * Runs the first-boot wallet creation flow.
     *
     * @param navigator
     *            the application navigator
     */
    void runFirstBoot(final AppNavigator navigator) {
        FirstBootDialogs.askWallet().ifPresent(wallet -> {
            cashAccountService.addWallet(FirstBootDialogs.walletFrom(wallet));
            navigator.showDashboard();
        });

        if (cashAccountService.getWallets().isEmpty()) {
            navigator.showMessage("No wallet created", "Create a wallet to continue.");
        }
    }
}