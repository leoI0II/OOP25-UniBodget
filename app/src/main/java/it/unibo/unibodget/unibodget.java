package it.unibo.unibodget;

import java.math.BigDecimal;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.dashboard.api.BudgetMonitor;
import it.unibo.unibodget.model.dashboard.api.BudgetSettings;
import it.unibo.unibodget.model.dashboard.api.CategoryService;
import it.unibo.unibodget.model.dashboard.api.DashboardFacade;
import it.unibo.unibodget.model.dashboard.api.WalletService;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetMonitor;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.dashboard.impl.DefaultCategoryService;
import it.unibo.unibodget.model.dashboard.impl.DefaultDashboardFacade;
import it.unibo.unibodget.model.dashboard.impl.DefaultWalletService;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.view.dashboard.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Unibodget extends Application {

    @Override
    public void start(final Stage stage) {
        final WalletService walletService = new DefaultWalletService();

        // Demo wallet so the dashboard can start without crashing
        walletService.addWallet(new CashAccount("Wallet principale", FiatCurrency.EUR));

        final CategoryService categoryService = new DefaultCategoryService();
        final BudgetSettings budgetSettings = new DefaultBudgetSettings(BigDecimal.valueOf(1000));
        final BudgetMonitor budgetMonitor = new DefaultBudgetMonitor();
        final DashboardFacade dashboardFacade = new DefaultDashboardFacade(
                walletService,
                categoryService,
                budgetMonitor,
                budgetSettings
        );

        final DashboardView view = new DashboardView(walletService, dashboardFacade);

        stage.setTitle("UniBodget");
        stage.setScene(new Scene(view.getRoot(), 1440, 900));
        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
