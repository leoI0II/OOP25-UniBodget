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
import it.unibo.unibodget.model.settings.WindowPreferences;
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
 * <p>Bootstraps services and controllers, loads the main view and
 * persists application data when the application stops.
 */
public class App extends Application {

    private static final String ALPHA_KEY_VARIABLE = "ALPHA_VANTAGE_API_KEY";
    private static final String DEFAULT_ALPHA_KEY = "B56AX33NF4F29HHZ";

    private CashAccountManager cashAccountManager;
    private InvestmentAccountManager investmentAccountManager;

    private CashAccountService cashAccountsService;
    private InvestmentAccountService investmentsService;

    private boolean persistenceReady;
    private final SettingsController settingsController = new SettingsController();

    /** {@inheritDoc} */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final String alphaKey = resolveAlphaKey();
        System.setProperty(ALPHA_KEY_VARIABLE, alphaKey);
        System.out.println(alphaKey);

        final UniversalPriceService priceService = new UniversalPriceService(
                new ExchangeRateAPIImpl(),
                new CoinGeckoPriceProvider(),
                new AlphaPriceProvider(alphaKey)
        );

        final BasicCurrencyConverter currencyConverter =
                new BasicCurrencyConverter(priceService);

        cashAccountManager = new CashAccountManager();
        investmentAccountManager = new InvestmentAccountManager(currencyConverter);

        cashAccountsService = new CashAccountService();
        investmentsService = new InvestmentAccountService();

        // Loads wallets before initializing controllers and views.
        final var loadedCashAccounts = cashAccountManager.loadAll();
        final var loadedInvestmentAccounts = investmentAccountManager.loadAll();

        loadedCashAccounts.forEach(cashAccountsService::addWallet);
        loadedInvestmentAccounts.forEach(investmentsService::addWallet);

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

        final Scene scene = new Scene(root);
        ThemeManager.applyThemeToScene(scene);

        primaryStage.setScene(scene);
        primaryStage.setWidth(settings.getWindowPrefs().getWidth());
        primaryStage.setHeight(settings.getWindowPrefs().getHeight());
        primaryStage.setMaximized(settings.getWindowPrefs().isMaximized());
        primaryStage.setTitle("UniBodget");
        primaryStage.show();

        updateWindowProperty(primaryStage, settings);

        persistenceReady = true;
    }

    /**
     * Updates window preferences when the window dimensions or state change.
     *
     * @param primaryStage the main application stage
     * @param settings the settings instance to update
     */
    private void updateWindowProperty(final Stage primaryStage, final Settings settings) {
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (!primaryStage.isMaximized()) {
                settings.getWindowPrefs().setWidth(newValue.doubleValue());
            }
        });

        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!primaryStage.isMaximized()) {
                settings.getWindowPrefs().setHeight(newValue.doubleValue());
            }
        });

        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            settings.getWindowPrefs().setMaximized(newValue);
        });
    }

    /**
     * Saves wallets and window preferences when the application stops.
     *
     * <p>Saving is skipped if application startup did not complete.
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
                        investmentAccountManager.saveAll(investmentsService.getWallets())),
                () -> {
                    final WindowPreferences preferences =
                            settingsController.getSettings().getWindowPrefs();

                    settingsController.updateWindowPrefs(new WindowPreferences(
                            preferences.getWidth(),
                            preferences.getHeight(),
                            preferences.isMaximized()
                    ));
                }
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
     * Reads the API key from the environment, falling back to the default key.
     *
     * @return the configured API key, or the default if unset or blank
     */
    private static String resolveAlphaKey() {
        final String environmentKey = System.getenv(ALPHA_KEY_VARIABLE);
        return environmentKey == null || environmentKey.isBlank()
                ? DEFAULT_ALPHA_KEY
                : environmentKey.strip();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments forwarded to JavaFX
     */
    public static void main(final String[] args) {
        launch(args);
    }
}