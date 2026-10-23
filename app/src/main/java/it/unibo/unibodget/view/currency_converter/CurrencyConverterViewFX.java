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
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.settings.WindowPreferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

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

    private static final double ROOT_PADDING_TOP = 18;
    private static final double ROOT_PADDING_HORIZONTAL = 14;
    private static final double ROOT_PADDING_BOTTOM = 18;
    private static final int TITLE_FONT_SIZE = 28;
    private static final String PROP_BOLD_BY_DEFAULT = "boldByDefault";

    /**
     * Window size and maximize preferences.
     */
    private static final WindowPreferences PREFS = new WindowPreferences();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CurrencyConverterViewFX() {
    }

    /**
     * Launches the JavaFX application with a pre‑initialized controller.
     *
     * @param controller the main currency converter controller
     */
    public static void launchWith(final CurrencyConverterController controller) {
        if (controller == null) {
            throw new IllegalStateException("Controller not initialized. Use launchWith().");
        }
        final Stage stage = new Stage();

        final Parent root = buildContent(controller);

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
        pageTitle.getStyleClass().add("header-title");
        pageTitle.setStyle("-fx-font-size: " + TITLE_FONT_SIZE + "px; -fx-font-weight: bold;");
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

        showChartButton.setOnAction(e -> {
            final Window owner = showChartButton.getScene() != null
                    ? showChartButton.getScene().getWindow()
                    : null;
            CurrencyHistoryChartView.showAsPopup(owner, historyController);
        });

        /* -------------------- TOP BAR -------------------- */
        final HBox titleBox = new HBox(16, pageTitle, showChartButton);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        final Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        final Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("secondary-button", "close-app-button");
        closeButton.getProperties().put(PROP_BOLD_BY_DEFAULT, Boolean.TRUE);
        closeButton.setOnAction(e -> Platform.exit());
        ThemeManager.applyFont(closeButton);

        final HBox topBar = new HBox(12, titleBox, topSpacer, closeButton);
        topBar.setAlignment(Pos.TOP_LEFT);

        ThemeManager.addListener(t -> Platform.runLater(() -> {
            ThemeManager.applyFont(pageTitle);
            ThemeManager.applyFont(showChartButton);
            ThemeManager.applyFont(closeButton);
        }));

        /* -------------------- MAIN CONTENT -------------------- */
        final FlowPane contentRow = new FlowPane(20, 20);
        contentRow.setAlignment(Pos.TOP_LEFT);
        contentRow.getChildren().addAll(
                converterWidget.getView(),
                watchlistWidget.getView(),
                bankConverterWidget.getView()
        );

        final VBox root = new VBox(20, topBar, contentRow);
        root.setPadding(new Insets(
                ROOT_PADDING_TOP,
                ROOT_PADDING_HORIZONTAL,
                ROOT_PADDING_BOTTOM,
                ROOT_PADDING_HORIZONTAL
        ));
        root.setAlignment(Pos.TOP_LEFT);

        /* -------------------- BACKGROUND -------------------- */
        root.setStyle("-fx-background-color: transparent;");

        final ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("converter-scroll");
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Applies the theme when embedded in the main application scene.
        scrollPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                ThemeManager.applyThemeToScene(newScene);
                Platform.runLater(() -> {
                    scrollPane.applyCss();
                    scrollPane.layout();
                    contentRow.requestLayout();
                });
            }
        });

        return scrollPane;
    }

}
