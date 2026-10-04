package it.unibo.unibodget;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.investment.controllers.DefaultInvestmentController;
import it.unibo.unibodget.model.investment.service.CSVInvestmentsSnapshotService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.wallet.CashAccountManager;
import it.unibo.unibodget.model.wallet.InvestmentAccountManager;
import it.unibo.unibodget.view.main.CurrencyConverterModule;
import it.unibo.unibodget.view.main.DashboardModule;
import it.unibo.unibodget.view.main.ViewControllersFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for UniBodget.
 *
 * <p>Bootstraps the model layer (services, controllers, snapshot service),
 * loads the main FXML view, and displays the primary stage.
 */
public class App extends Application {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;
    private static final String ALPHA_KEY = "B56AX33NF4F29HHZ";

    private CashAccountManager cashAccountManager;
    private InvestmentAccountManager investmentAccountManager;

    private CashAccountService cashAccountsService;
    private InvestmentAccountService investmentsService;

    private boolean persistenceReady;

    /** {@inheritDoc} */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        System.setProperty("ALPHA_VANTAGE_API_KEY", ALPHA_KEY);

        final UniversalPriceService priceService = new UniversalPriceService(
                new ExchangeRateAPIImpl(),
                new CoinGeckoPriceProvider(),
                new AlphaPriceProvider(ALPHA_KEY)
        );

        final BasicCurrencyConverter currencyConverter =
                new BasicCurrencyConverter(priceService);

        cashAccountManager = new CashAccountManager();
        investmentAccountManager = new InvestmentAccountManager(currencyConverter);

        cashAccountsService = new CashAccountService();
        investmentsService = new InvestmentAccountService();

        // Loads wallet before controllers and view
        final var loadedCashAccounts = cashAccountManager.loadAll();
        final var loadedInvestmentAccounts = investmentAccountManager.loadAll();

        loadedCashAccounts.forEach(cashAccountsService::addWallet);
        loadedInvestmentAccounts.forEach(investmentsService::addWallet);

        final SettingsController settingsController = new SettingsController();
        final Settings settings = settingsController.getSettings();

        final var snapshotService = new CSVInvestmentsSnapshotService();

        final var investmentsController = new DefaultInvestmentController(
                investmentsService,
                cashAccountsService,
                currencyConverter,
                settings,
                snapshotService
        );

        final var dashboardModule = new DashboardModule(
                currencyConverter,
                settings,
                cashAccountsService
        );

        final var converterModule = new CurrencyConverterModule(
                new ExchangeRateAPIImpl(),
                currencyConverter
        );

        final FXMLLoader mainViewPage = new FXMLLoader(
                getClass().getResource(
                        "/it/unibo/unibodget/view/jfx/fxml/main/MainView.fxml"
                )
        );

        final var factory = new ViewControllersFactory(
                investmentsController,
                snapshotService,
                dashboardModule,
                converterModule,
                settingsController
        );

        mainViewPage.setControllerFactory(factory::create);
        final Node mainViewNode = mainViewPage.load();
        final HBox root = (HBox) mainViewNode;

        final Scene scene = new Scene(
                root,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );
        ThemeManager.applyThemeToScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("UniBodget");
        primaryStage.show();

        persistenceReady = true;
    }

    /**
     * Saves wallet data when the JavaFX application stops normally.
     */
    @Override
    public void stop() {
        if (!persistenceReady) {
            System.out.println("[PERSISTENCE] Startup incomplete: saving skipped");
            return;
        }
        RuntimeException failure = null;
        for (final Runnable save : new Runnable[] {
                () -> saveWallets("Cash", () ->
                        cashAccountManager.saveAll(cashAccountsService.getWallets())),
                () -> saveWallets("Investment", () ->
                        investmentAccountManager.saveAll(investmentsService.getWallets()))
        }) {
            try {
                save.run();
            } catch (final RuntimeException ex) {
                if (failure == null) {
                    failure = ex;
                } else {
                    failure.addSuppressed(ex);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Executes a save operation and logs its outcome.
     *
     * @param type the wallet type
     * @param action the save operation
     */
    private void saveWallets(final String type, final Runnable action) {
        try {
            action.run();
            System.out.println("[PERSISTENCE] " + type + " wallets saved");
        } catch (final RuntimeException ex) {
            System.err.println("[PERSISTENCE] " + type + " wallet saving failed");
            throw ex;
        }
    }

    /**
     * Application entry point; delegates to {@link #launch(String[])}.
     *
     * @param args command-line arguments forwarded to JavaFX
     */
    public static void main(final String[] args) {
        launch(args);
    }

}
