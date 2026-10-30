package it.unibo.unibodget;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIImpl;
import it.unibo.unibodget.model.currency.api.provider.AlphaPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.CoinGeckoPriceProvider;
import it.unibo.unibodget.model.currency.api.provider.UniversalPriceService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.controller.investments.DefaultInvestmentController;
import it.unibo.unibodget.model.investment.service.CSVInvestmentsSnapshotService;
import it.unibo.unibodget.model.investment.service.PeriodicSnapshotSaver;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.view.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import it.unibo.unibodget.model.utils.AppTicker;
import it.unibo.unibodget.model.wallet.CashAccountManager;
import it.unibo.unibodget.model.wallet.InvestmentAccountManager;
import it.unibo.unibodget.persistency.WalletAutoSaver;
import it.unibo.unibodget.view.main.CurrencyConverterModule;
import it.unibo.unibodget.view.main.DashboardModule;
import it.unibo.unibodget.view.main.ViewControllersFactory;
import it.unibo.unibodget.view.utils.FxAppTicker;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for UniBodget.
 *
 * <p>Bootstraps services and controllers, loads the main view and persists application data when
 * the application stops.
 */
public final class App extends Application {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(App.class);
    private static final double MIN_WINDOW_WIDTH = 900.0;
    private static final double MIN_WINDOW_HEIGHT = 600.0;
    // time between two ticks of the app (e.g. the portfolio page refreshes the prices)
    private static final Duration TICK_INTERVAL = Duration.ofMinutes(1);
    // how often a P/L snapshot of each investment account is saved while the app is open
    private static final Duration SNAPSHOT_INTERVAL = Duration.ofMinutes(10);
    private WalletAutoSaver walletSaver;
    private PeriodicSnapshotSaver snapshotSaver;
    private AppTicker ticker;
    private boolean persistenceReady;
    private final SettingsController settingsController = new SettingsController();
    private Stage primaryStage;

    /** {@inheritDoc} */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final UniversalPriceService priceService =
                new UniversalPriceService(
                        new ExchangeRateAPIImpl(),
                        new CoinGeckoPriceProvider(),
                        new AlphaPriceProvider());

        final BasicCurrencyConverter currencyConverter = new BasicCurrencyConverter(priceService);

        final var cashAccountManager = new CashAccountManager();
        final var investmentAccountManager = new InvestmentAccountManager(currencyConverter);

        final var cashAccountsService = new CashAccountService();
        final var investmentsService = new InvestmentAccountService();

        // Loads wallets before initializing controllers and views.
        final var loadedCashAccounts = cashAccountManager.loadAll();
        final var loadedInvestmentAccounts = investmentAccountManager.loadAll();

        loadedCashAccounts.forEach(cashAccountsService::addWallet);
        loadedInvestmentAccounts.forEach(investmentsService::addWallet);

        walletSaver =
                new WalletAutoSaver()
                        .register(cashAccountsService::getWallets, cashAccountManager::saveAll)
                        .register(
                                investmentsService::getWallets, investmentAccountManager::saveAll);

        final Settings settings = settingsController.getSettings();
        final var snapshotService = new CSVInvestmentsSnapshotService();
        snapshotSaver = new PeriodicSnapshotSaver(
                        investmentsService::getWallets, snapshotService, SNAPSHOT_INTERVAL
        );
        snapshotSaver.start();

        final var investmentsController =
                new DefaultInvestmentController(
                        investmentsService,
                        cashAccountsService,
                        currencyConverter,
                        settings,
                        snapshotService);

        final var dashboardModule =
                new DashboardModule(currencyConverter, settings, cashAccountsService);

        final var converterModule =
                new CurrencyConverterModule(new ExchangeRateAPIImpl(), currencyConverter);

        final FXMLLoader mainViewPage =
                new FXMLLoader(
                        getClass()
                                .getResource(
                                        "/it/unibo/unibodget/view/jfx/fxml/main/MainView.fxml"));

        final var factory =
                new ViewControllersFactory(
                        investmentsController,
                        snapshotService,
                        dashboardModule,
                        converterModule,
                        settingsController);

        mainViewPage.setControllerFactory(factory::create);
        final Parent root = mainViewPage.load();

        final Scene scene = new Scene(root);
        ThemeManager.applyThemeToScene(scene);

        this.primaryStage = primaryStage;

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setWidth(Math.max(MIN_WINDOW_WIDTH, settings.getWindowPrefs().getWidth()));
        primaryStage.setHeight(Math.max(MIN_WINDOW_HEIGHT, settings.getWindowPrefs().getHeight()));
        primaryStage.setMaximized(settings.getWindowPrefs().isMaximized());
        primaryStage.setTitle("UniBodget");
        primaryStage.show();

        updateWindowProperty(primaryStage);

        persistenceReady = true;
        walletSaver.start();
        // the first tick is sent right away: the snapshots are saved and so the prices are
        // downloaded now, and not when the user opens the portfolio page
        ticker = new FxAppTicker(TICK_INTERVAL);
        ticker.start();
    }

    /**
     * Updates window preferences when the window dimensions or state change.
     *
     * @param stage the main application stage
     */
    private void updateWindowProperty(final Stage stage) {
        stage.widthProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!stage.isMaximized()) {
                                saveWindowPreferences(stage);
                            }
                        });

        stage.heightProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!stage.isMaximized()) {
                                saveWindowPreferences(stage);
                            }
                        });

        stage.maximizedProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            saveWindowPreferences(stage);
                        });

        stage.setOnCloseRequest(event -> saveWindowPreferences(stage));
    }

    /**
     * Persists the current window preferences through the settings controller.
     *
     * @param stage the stage whose geometry to persist
     */
    private void saveWindowPreferences(final Stage stage) {
        final WindowPreferences currentPrefs = settingsController.getSettings().getWindowPrefs();
        final double width = stage.isMaximized()
                ? currentPrefs.getWidth()
                : Math.max(MIN_WINDOW_WIDTH, stage.getWidth());
        final double height = stage.isMaximized()
                ? currentPrefs.getHeight()
                : Math.max(MIN_WINDOW_HEIGHT, stage.getHeight());
        final boolean maximized = stage.isMaximized();

        settingsController.updateWindowPrefs(new WindowPreferences(width, height, maximized));
    }

    /**
     * Stops automatic saving and persists wallets and window preferences.
     *
     * <p>Saving is skipped if application startup did not complete.
     */
    @Override
    public void stop() {
        if (!persistenceReady) {
            LOGGER.info("[PERSISTENCE] Startup incomplete: saving skipped");
            return;
        }

        IllegalStateException failure = null;

        for (final Runnable save
                : List.<Runnable>of(
                        ticker::stop,
                        snapshotSaver::stop,
                        walletSaver::stop,
                        walletSaver::saveAll,
                        () -> {
                            if (primaryStage != null) {
                                saveWindowPreferences(primaryStage);
                            }
                        })) {
                            
            try {
                save.run();
            } catch (final IllegalStateException | IllegalArgumentException ex) {
                if (failure == null) {
                    failure = new IllegalStateException("Shutdown failure", ex);
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
     * Launches the JavaFX application.
     *
     * @param args command-line arguments forwarded to JavaFX
     */
    public static void main(final String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch(args);
    }
}
