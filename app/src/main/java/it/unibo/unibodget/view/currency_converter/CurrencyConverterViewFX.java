package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.BankConversionController;
import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.controller.currency_converter.WatchListController;
import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.alert.CurrencyAlert;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIClient;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.currency.watchlist.WatchList;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import it.unibo.unibodget.view.UI.FXAdapter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main dashboard screen for all currency‑related operations.
 *
 * <p>
 * This JavaFX view hosts multiple widgets:
 * <ul>
 * <li>{@link ConverterWidgetFX} – base currency converter</li>
 * <li>{@link WatchlistWidgetFX} – watchlist for favorite currency pairs</li>
 * <li>{@link BankConverterWidgetFX} – bank‑mediated conversion with fees</li>
 * <li>Historical chart viewer</li>
 * </ul>
 *
 * <p>
 * The dashboard applies theme‑aware styling, window preferences, and
 * orchestrates the interaction between widgets.
 */
public final class CurrencyConverterViewFX {

    private static final double ROOT_PADDING = 35;

    /**
     * Shared controller injected via launchWith().
     */
    private static CurrencyConverterController sharedController;

    /**
     * Window size and maximize preferences.
     */
    private static final WindowPreferences PREFS = new WindowPreferences();

    /**
     * Launches the JavaFX application with a pre‑initialized controller.
     *
     * @param controller the main currency converter controller
     */
    public static void launchWith(final CurrencyConverterController controller) {
        sharedController = controller;
        final Stage stage = new Stage();

        //Application.launch(CurrencyConverterViewFX.class);

        if (sharedController == null) {
            throw new IllegalStateException("Controller not initialized. Use launchWith().");
        }

        final Parent root = buildContent(sharedController);

        /* -------------------- WINDOW SETUP -------------------- */
        final Scene scene = new Scene(root);
        stage.setScene(scene);
        ThemeManager.applyThemeToScene(scene);
        stage.setTitle("UniBodget - Currency Dashboard");

        // Save window width changes
        stage.widthProperty().addListener((obs, old, val) -> PREFS.setWidth(val.doubleValue()));

        // Restore window size or maximize
        if (PREFS.isMaximized()) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(PREFS.getWidth());
            stage.setHeight(PREFS.getHeight());
        }

        stage.show();
    }

    /**
     * Builds the currency converter dashboard content as a reusable JavaFX node.
     *
     * <p>Used both by the standalone {@link #start(Stage)} application and by the
     * main shell to embed the converter inside the content area.</p>
     *
     * @param controller the shared currency converter controller
     * @return the root node of the converter dashboard
     */
    public static Parent buildContent(final CurrencyConverterController controller) {
        /* -------------------- PAGE TITLE -------------------- */
        final Label pageTitle = new Label("Currencies Converter Dashboard");
        ThemeManager.applyFont(pageTitle);

        /* -------------------- WIDGET INSTANTIATION -------------------- */
        // Base converter widget
        final ConverterWidgetFX converterWidget
                = new ConverterWidgetFX(controller, new CurrencyAlertService());

        // Watchlist widget
        final WatchList watchlistModel = new WatchList();
        final WatchListController watchlistController = new WatchListController(watchlistModel);
        final WatchlistWidgetFX watchlistWidget
                = new WatchlistWidgetFX(watchlistController, converterWidget);

        // Alert service with a sample alert
        final CurrencyAlertService alertService = new CurrencyAlertService();
        alertService.addAlert(new CurrencyAlert(
                Currency.get("EUR"),
                Currency.get("USD"),
                0.5,
                true // triggers when rate < 0.5
        ));

        // Bank conversion widget
        final BankConversionController bankController = new BankConversionController();
        final BankConverterWidgetFX bankConverterWidget = new BankConverterWidgetFX(
                bankController,
                converterWidget.getAmountField(),
                converterWidget.getFromBox(),
                converterWidget.getToBox(),
                (BasicCurrencyConverter) controller.getConverter()
        );

        /* -------------------- HISTORICAL CHART BUTTON -------------------- */
        final Button showChartButton = new Button("Show Chart");
        ThemeManager.applyFont(showChartButton);

        final CurrencyConverterController historyController =
                new CurrencyConverterController(
                        new ExchangeRateAPIClient(),
                        controller.getConverter()
                );

        showChartButton.setOnAction(e ->
                CurrencyHistoryChartView.showInNewWindow(historyController)
        );
        
        /* -------------------- TOP BAR -------------------- */
        final HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getChildren().addAll(pageTitle, showChartButton);

        /* -------------------- MAIN CONTENT -------------------- */
        final HBox contentRow = new HBox(25);
        contentRow.setAlignment(Pos.TOP_LEFT);
        contentRow.getChildren().addAll(
                converterWidget.getView(),
                watchlistWidget.getView(),
                bankConverterWidget.getView()
        );

        final VBox root = new VBox(30, topBar, contentRow);
        root.setPadding(new Insets(ROOT_PADDING));
        root.setAlignment(Pos.TOP_LEFT);

        /* -------------------- BACKGROUND GRADIENT -------------------- */
        root.getStyleClass().add("theme-background");

        // Applies the theme when embedded in the main application scene.
        root.sceneProperty().addListener((observable, oldScene, newScene) -> {
        if (newScene != null) {
                ThemeManager.applyThemeToScene(newScene);
        }
        });

        return root;
    }

}
