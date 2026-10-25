package it.unibo.unibodget.view.currency_converter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.MonetaryInputParser;
import it.unibo.unibodget.model.currency.history.CurrencyHistoryPoint;
import it.unibo.unibodget.model.settings.ThemeManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone JavaFX view that displays historical exchange‑rate data for a selected currency pair
 * using a line chart.
 *
 * <p>This component:
 *
 * <ul>
 *   <li>owns its own currency selectors
 *   <li>fetches historical data directly from {@link CurrencyConverterController}
 *   <li>plots the last N days of exchange‑rate history
 *   <li>optionally draws a threshold line for alert visualization
 * </ul>
 *
 * <p>It is designed to be opened in a separate window via {@link
 * #showInNewWindow(CurrencyConverterController)}.
 */
public final class CurrencyHistoryChartView extends VBox {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyHistoryChartView.class);

    /* -------------------- CONSTANTS -------------------- */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final int DEFAULT_HISTORY_DAYS = 30;
    private static final int SCENE_WIDTH = 900;
    private static final int SCENE_HEIGHT = 600;
    private static final String POPUP_TITLE = "Currency History - Live Data";

    /* -------------------- CONTROLLER -------------------- */
    private final CurrencyConverterController controller;

    /* -------------------- UI CONTROLS -------------------- */
    private final ComboBox<CurrencyUnit> fromBox = new ComboBox<>();
    private final ComboBox<CurrencyUnit> toBox = new ComboBox<>();
    private final TextField thresholdField = new TextField();
    private final Button loadButton = new Button("Load Chart");
    private final Label statusLabel = new Label("Ready.");

    /* -------------------- CHART -------------------- */
    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

    /**
     * Creates a new history chart view bound to the given controller.
     *
     * @param controller the controller providing historical exchange‑rate data
     */
    public CurrencyHistoryChartView(final CurrencyConverterController controller) {
        this.controller = controller;

        setSpacing(10);
        setPadding(new Insets(10));

        configureCurrencyBoxes();
        configureChart();

        thresholdField.setPromptText("Alert threshold");
        ThemeManager.applyFont(statusLabel);

        loadButton.setOnAction(e -> onLoad());

        // Controls row: From, To, Threshold, Load
        final Label fromLabel = new Label("From:");
        final Label toLabel = new Label("To:");

        ThemeManager.applyFont(fromLabel);
        ThemeManager.applyFont(toLabel);
        ThemeManager.applyFont(fromBox);
        ThemeManager.applyFont(toBox);
        ThemeManager.applyFont(thresholdField);
        ThemeManager.applyFont(loadButton);

        final HBox controlsRow =
                new HBox(10, fromLabel, fromBox, toLabel, toBox, thresholdField, loadButton);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(controlsRow, statusLabel, chart);
        chart.applyCss();
    }

    /**
     * Opens this chart view inside a modal dialog popup attached to the given owner window.
     *
     * @param owner the parent window that owns this dialog, or {@code null}
     * @param controller the controller providing history data
     * @return the created Dialog
     */
    public static Dialog<Void> showAsPopup(final Window owner, final CurrencyConverterController controller) {
        final CurrencyHistoryChartView view = new CurrencyHistoryChartView(controller);
        view.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);

        final Dialog<Void> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(POPUP_TITLE);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(view);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ThemeManager.applyThemeToDialog(dialog);

        dialog.show();
        return dialog;
    }

    /**
     * Opens this chart view inside a new standalone {@link Stage}. Useful for buttons in other
     * widgets that want to show historical data.
     *
     * @param controller the controller providing history data
     * @return the created Stage
     */
    public static Stage showInNewWindow(final CurrencyConverterController controller) {
        final CurrencyHistoryChartView view = new CurrencyHistoryChartView(controller);

        final Stage stage = new Stage();
        stage.setTitle(POPUP_TITLE);

        final Scene scene = new Scene(view, SCENE_WIDTH, SCENE_HEIGHT);
        ThemeManager.applyThemeToScene(scene);
        stage.setScene(scene);

        stage.show();
        return stage;
    }

    /* =============== UI CONFIGURATION ===================== */

    /**
     * Configures the currency ComboBoxes to show only fiat currencies and display only their ISO
     * code (EUR, USD, ...).
     */
    private void configureCurrencyBoxes() {
        for (final CurrencyUnit unit : FiatCurrency.values()) {
            fromBox.getItems().add(unit);
            toBox.getItems().add(unit);
        }

        // Default selection
        fromBox.setValue(FiatCurrency.EUR);
        toBox.setValue(FiatCurrency.USD);

        // Converter that shows only currency code
        final StringConverter<CurrencyUnit> codeOnly =
                new StringConverter<>() {
                    @Override
                    public String toString(final CurrencyUnit unit) {
                        return unit == null ? "" : unit.getCode();
                    }

                    @Override
                    public CurrencyUnit fromString(final String code) {
                        return null; // Not needed
                    }
                };

        fromBox.setConverter(codeOnly);
        toBox.setConverter(codeOnly);

        fromBox.setCellFactory(lv -> codeOnlyCell());
        toBox.setCellFactory(lv -> codeOnlyCell());
    }

    /**
     * Creates a ListCell that displays only the currency code.
     *
     * @return a ListCell for ComboBox items
     */
    private ListCell<CurrencyUnit> codeOnlyCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode());
                ThemeManager.applyFont(this);
            }
        };
    }

    /** Configures the line chart appearance and axes. */
    private void configureChart() {
        xAxis.setLabel("Date");
        yAxis.setLabel("Rate");

        xAxis.setStyle(ThemeManager.getFontStyle());
        yAxis.setStyle(ThemeManager.getFontStyle());

        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
    }

    /* ================== LOAD & PLOT ======================= */

    /**
     * Loads historical data for the selected currency pair and plots it. Also draws a threshold
     * line if provided.
     */
    private void onLoad() {
        final CurrencyUnit from = fromBox.getValue();
        final CurrencyUnit to = toBox.getValue();

        if (from == null || to == null || from.equals(to)) {
            statusLabel.setText("Please select two different currencies.");
            return;
        }

        // Date range: last DEFAULT_HISTORY_DAYS days
        final LocalDate toDate = LocalDate.now();
        final LocalDate fromDate = toDate.minusDays(DEFAULT_HISTORY_DAYS);

        // Fetch history points from controller
        final List<CurrencyHistoryPoint> points =
                controller.getHistoryPoints(from, to, fromDate, toDate);

        points.forEach(p -> LOGGER.debug("{} -> {}", p.getDate(), p.getRate()));

        final Double threshold = parseThreshold(thresholdField.getText());

        plot(points, from.getCode() + " -> " + to.getCode(), threshold);

        statusLabel.setText("Loaded " + points.size() + " points.");
    }

    /**
     * Plots the historical points on the chart and optionally draws a threshold line.
     *
     * @param points list of historical exchange‑rate points
     * @param seriesName name of the main data series
     * @param threshold optional threshold value (null if not used)
     */
    private void plot(
            final List<CurrencyHistoryPoint> points,
            final String seriesName,
            final Double threshold) {
        chart.getData().clear();

        if (points == null || points.isEmpty()) {
            return;
        }

        // Main series
        final XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (final CurrencyHistoryPoint p : points) {
            series.getData().add(new XYChart.Data<>(p.getDate().format(DATE_FORMAT), p.getRate()));
        }

        chart.getData().add(series);

        // Threshold line series
        if (threshold != null) {
            final XYChart.Series<String, Number> thresholdSeries = new XYChart.Series<>();
            thresholdSeries.setName("Threshold (" + threshold + ")");

            for (final CurrencyHistoryPoint p : points) {
                thresholdSeries
                        .getData()
                        .add(new XYChart.Data<>(p.getDate().format(DATE_FORMAT), threshold));
            }

            chart.getData().add(thresholdSeries);

            // Style dashed red line
            if (thresholdSeries.getNode() != null) {
                thresholdSeries.getNode().setStyle("-fx-stroke: red; -fx-stroke-dash-array: 5 5;");
            }
        }

        chart.applyCss();

        chart.lookupAll(".chart-legend-item-text")
                .forEach(
                        item -> {
                            ThemeManager.applyFont(item);

                            item.lookupAll("*").forEach(ThemeManager::applyFont);
                        });
    }

    /**
     * Parses a threshold value from user input.
     *
     * @param text user input
     * @return parsed double or null if invalid
     */
    private Double parseThreshold(final String text) {
        return MonetaryInputParser.parseDouble(text).orElse(null);
    }

    /**
     * Returns the underlying chart node for embedding in other layouts.
     *
     * @return the underlying chart node
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "View exposes chart node")
    public Node getChartNode() {
        return chart;
    }
}
