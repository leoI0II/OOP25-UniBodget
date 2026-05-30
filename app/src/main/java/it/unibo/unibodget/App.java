package it.unibo.unibodget;

import it.unibo.unibodget.model.converter.provider.MockExchangeRateProvider;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.investment.controllers.DefaultInvestmentController;
import it.unibo.unibodget.model.investment.service.CSVInvestmentsSnapshotService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.view.main.ViewControllersFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    /** {@inheritDoc} */
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final var mockProvider = new MockExchangeRateProvider();
        final var investmentsService = new InvestmentAccountService();
        final var cashAccountsService = new CashAccountService();
        final var settings = new Settings();
        final var snapshotService = new CSVInvestmentsSnapshotService();
        final var investmentsController = new DefaultInvestmentController(
                investmentsService,
                cashAccountsService,
                mockProvider,
                settings,
                snapshotService
        );
        investmentsService.addWallet(new InvestmentAccount("Binance", FiatCurrency.USD, new MockExchangeRateProvider()));
        investmentsService.addWallet(new InvestmentAccount("OKX", FiatCurrency.USD, new MockExchangeRateProvider()));

        final FXMLLoader mainViewPage = new FXMLLoader(
                getClass().getResource(
                        "/it/unibo/unibodget/view/jfx/fxml/main/MainView.fxml"
                )
        );
        final var factory = new ViewControllersFactory(investmentsController, snapshotService);
        mainViewPage.setControllerFactory(factory::create);
        final Node mainViewNode = mainViewPage.load();

        // wrap content in a temporary HBox layout
        final var root = new HBox(mainViewNode);
        HBox.setHgrow(mainViewNode, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setTitle("Investments");
        primaryStage.show();
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
