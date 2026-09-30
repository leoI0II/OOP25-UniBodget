package it.unibo.unibodget.view.main;

import it.unibo.unibodget.controller.dashboard.impl.DefaultDashboardController;
import it.unibo.unibodget.model.categories.CategoryCatalog;
import it.unibo.unibodget.model.currency.engin.CurrencyConverter;
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
import javafx.scene.layout.Region;

import java.util.Objects;

/**
 * Wires together the dashboard MVC graph (facade + controller + view) so that
 * the dashboard can be embedded as a <em>content-only</em> panel inside the
 * shared FXML shell ({@code MainViewController} + shared sidebar).
 *
 * <p>Unlike the standalone {@code InMemoryDashboardBootstrap} (which builds a
 * self-contained {@code MainAppShell}), this module receives the shared
 * application services from the outside so that investments and dashboard
 * operate on the <strong>same</strong> cash wallets, currency converter and
 * settings.</p>
 */
public final class DashboardModule {

    private final DashboardFacade dashboardFacade;
    private final DefaultDashboardController dashboardController;
    private final DefaultDashboardView dashboardView;

    /**
     * Builds and wires the dashboard graph.
     *
     * @param converter          the shared currency converter
     * @param settings           the shared application settings
     * @param cashAccountService the shared cash-account service (same instance used elsewhere)
     */
    public DashboardModule(
            final CurrencyConverter converter,
            final Settings settings,
            final CashAccountService cashAccountService
    ) {
        Objects.requireNonNull(converter);
        Objects.requireNonNull(settings);
        Objects.requireNonNull(cashAccountService);

        final CategoryCatalog categoryCatalog = new CategoryCatalog();
        final CategoryService categoryService = new DefaultCategoryService();
        final BudgetMonitor budgetMonitor = new DefaultBudgetMonitor();
        final FriendLoanSummaryService friendLoanSummaryService = new DefaultFriendLoanSummaryService();
        final WalletInsightService walletInsightService = new DefaultWalletInsightService();
        final CashTransactionFactory cashTransactionFactory = new CashTransactionFactory();

        ThemeManager.setTheme(settings.getTheme());

        this.dashboardFacade = new DefaultDashboardFacade(
                cashAccountService,
                categoryService,
                budgetMonitor,
                friendLoanSummaryService,
                walletInsightService,
                categoryCatalog,
                converter,
                settings
        );

        final DefaultTotalCashBalanceService totalCashBalanceService =
                new DefaultTotalCashBalanceService(cashAccountService, converter);

        this.dashboardView = new DefaultDashboardView();
        this.dashboardController = new DefaultDashboardController(
                dashboardView,
                dashboardFacade,
                cashAccountService,
                totalCashBalanceService,
                settings,
                cashTransactionFactory
        );
        this.dashboardController.init();
    }

    /**
     * @return the dashboard view node to place into the shell content area
     */
    public Region getView() {
        return dashboardView;
    }

    /**
     * @return the dashboard read/command facade (use for reading sidebar data)
     */
    public DashboardFacade getFacade() {
        return dashboardFacade;
    }

    /**
     * @return the dashboard presenter (use for user actions; it re-renders the center)
     */
    public DefaultDashboardController getController() {
        return dashboardController;
    }
}
