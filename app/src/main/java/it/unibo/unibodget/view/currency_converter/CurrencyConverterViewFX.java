package it.unibo.unibodget.view.currency_converter;

import java.time.LocalDate;
import java.util.Map;

import it.unibo.unibodget.controller.currency_converter.BankConversionController;
import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.controller.currency_converter.WatchListController;
import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.alert.CurrencyAlert;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import it.unibo.unibodget.model.currency.watchlist.WatchList;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import it.unibo.unibodget.view.UI.FXAdapter;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPI;
import it.unibo.unibodget.model.currency.api.ExchangeRateAPIClient;
import it.unibo.unibodget.model.currency.api.MockExchangeRateAPI;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.Button;

/**
 * Main dashboard screen for all currency‑related operations.
 * <p>
 * This JavaFX view hosts multiple widgets:
 * <ul>
 *     <li>{@link ConverterWidgetFX} – base currency converter</li>
 *     <li>{@link WatchlistWidgetFX} – watchlist for favorite currency pairs</li>
 *     <li>{@link BankConverterWidgetFX} – bank‑mediated conversion with fees</li>
 *     <li>Historical chart viewer</li>
 * </ul>
 * <p>
 * The dashboard applies theme‑aware styling, window preferences, and
 * orchestrates the interaction between widgets.
 */
public final class CurrencyConverterViewFX extends Application {

    private static final double ROOT_PADDING = 35;

    /** Shared controller injected via launchWith(). */
    private static CurrencyConverterController sharedController;

    /** Window size and maximize preferences. */
    private static final WindowPreferences prefs = new WindowPreferences();

    /**
     * Launches the JavaFX application with a pre‑initialized controller.
     *
     * @param controller the main currency converter controller
     */
    public static void launchWith(final CurrencyConverterController controller) {
        sharedController = controller;
        Application.launch(CurrencyConverterViewFX.class);
    }

    @Override
    public void start(final Stage stage) {
        if (sharedController == null) {
            throw new IllegalStateException("Controller not initialized. Use launchWith().");
        }

        /* -------------------- THEME SETUP -------------------- */
        final Theme theme = ThemeManager.getTheme();
        final Color primaryColor = FXAdapter.toFXColor(theme.getPrimaryColor());
        final Color textColor = FXAdapter.toFXColor(theme.getTextColor());
        final Color darkPrimary = primaryColor.deriveColor(0, 1.0, 0.15, 1.0);

        final Font titleFont = Font.font(theme.getFontFamily(), FontWeight.BOLD, theme.getFontSize() + 14);

        /* -------------------- PAGE TITLE -------------------- */
        final Label pageTitle = new Label("Currencies Converter Dashboard");
        pageTitle.setFont(titleFont);
        pageTitle.setTextFill(textColor);

        /* -------------------- WIDGET INSTANTIATION -------------------- */
        // Base converter widget
        final ConverterWidgetFX converterWidget =
                new ConverterWidgetFX(sharedController, new CurrencyAlertService());

        // Watchlist widget
        final WatchList watchlistModel = new WatchList();
        final WatchListController watchlistController = new WatchListController(watchlistModel);
        final WatchlistWidgetFX watchlistWidget =
                new WatchlistWidgetFX(watchlistController, converterWidget);

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
                (BasicCurrencyConverter) sharedController.getConverter()
        );

        /* -------------------- HISTORICAL CHART BUTTON -------------------- */
        final Button showChartButton = new Button("Show Chart");
        showChartButton.setOnAction(e -> {
            ExchangeRateAPI historyApi = new ExchangeRateAPIClient();
            Map<LocalDate, Double> history = historyApi.getHistoricalRates(
                    FiatCurrency.EUR,
                    FiatCurrency.USD,
                    LocalDate.now().minusDays(5),
                    LocalDate.now()
            );

            // if https call result nothing -> offline/error mode on
            if (history.isEmpty()) {
                System.out.println("Offline mode: using mock history");

                Map<LocalDate, Double> mockHistory =
                        MockExchangeRateAPI.generateMockHistory(
                                LocalDate.now().minusDays(5),
                                LocalDate.now()
                        );

                historyApi = new MockExchangeRateAPI(
                        FiatCurrency.EUR,
                        MockExchangeRateAPI.generateMockLatestRates(FiatCurrency.EUR)
                ) {
                    @Override
                    public Map<LocalDate, Double> getHistoricalRates(
                            CurrencyUnit base, CurrencyUnit target,
                            LocalDate from, LocalDate to) {
                        return mockHistory;
                    }
                };
            }

            final BasicCurrencyConverter historyConverter =
                    new BasicCurrencyConverter(historyApi, FiatCurrency.EUR);
            final CurrencyConverterController historyController =
                    new CurrencyConverterController(historyApi, historyConverter);
            CurrencyHistoryChartView.showInNewWindow(historyController);
        });

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
        final LinearGradient bgGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, primaryColor),
                new Stop(1, darkPrimary)
        );
        root.setBackground(new Background(new BackgroundFill(bgGradient, CornerRadii.EMPTY, Insets.EMPTY)));

        /* -------------------- WINDOW SETUP -------------------- */
        stage.setScene(new Scene(root));
        stage.setTitle("UniBodget - Currency Dashboard");

        // Save window width changes
        stage.widthProperty().addListener((obs, old, val) -> prefs.setWidth(val.doubleValue()));

        // Restore window size or maximize
        if (prefs.isMaximized()) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(prefs.getWidth());
            stage.setHeight(prefs.getHeight());
        }

        stage.show();
    }
}
