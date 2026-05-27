package it.unibo.unibodget;

import it.unibo.unibodget.model.converter.provider.MockExchangeRateProvider;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.investment.controllers.DefaultInvestmentController;
import it.unibo.unibodget.model.investment.service.CSVInvestmentsSnapshotService;
import it.unibo.unibodget.model.service.CashAccountService;
import it.unibo.unibodget.model.service.InvestmentAccountService;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.view.investments.InvestmentsViewController;
import it.unibo.unibodget.view.main.MainViewController;
import it.unibo.unibodget.view.main.SideBarViewController;
import it.unibo.unibodget.view.main.ViewControllersFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var mockProvider = new MockExchangeRateProvider();
        var investmentsService = new InvestmentAccountService();
        var cashAccountsService = new CashAccountService();
        var settings = new Settings();
        var snapshotService = new CSVInvestmentsSnapshotService();
        var investmentsController = new DefaultInvestmentController(
                investmentsService,
                cashAccountsService,
                mockProvider,
                settings,
                snapshotService
        );
        investmentsService.addWallet(new InvestmentAccount("Binance", FiatCurrency.USD, new MockExchangeRateProvider()));
        investmentsService.addWallet(new InvestmentAccount("OKX", FiatCurrency.USD, new MockExchangeRateProvider()));

        FXMLLoader mainViewPage = new FXMLLoader(
                getClass().getResource(
                        "/it/unibo/unibodget/view/jfx/fxml/main/MainView.fxml"
                )
        );
        var factory = new ViewControllersFactory(investmentsController, snapshotService);
        mainViewPage.setControllerFactory(factory::create);
        Node mainViewNode = mainViewPage.load();
        MainViewController mainVC = mainViewPage.getController();

        // metto tutto in un Hbox temp
        var root = new HBox(mainViewNode);
        HBox.setHgrow(mainViewNode, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root, 900, 700));
        primaryStage.setTitle("Investments");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
