package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.history.CurrencyHistoryPoint;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Standalone JavaFX view that displays historical exchange‑rate data
 * for a selected currency pair using a line chart.
 * <p>
 * This component:
 * <ul>
 *     <li>owns its own currency selectors</li>
 *     <li>fetches historical data directly from {@link CurrencyConverterController}</li>
 *     <li>plots the last N days of exchange‑rate history</li>
 *     <li>optionally draws a threshold line for alert visualization</li>
 * </ul>
 * <p>
 * It is designed to be opened in a separate window via
 * {@link #showInNewWindow(CurrencyConverterController)}.
 */
public final class CurrencyHistoryChartView extends VBox {

    /* -------------------- CONSTANTS -------------------- */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");
    private static final int DEFAULT_HISTORY_DAYS = 30;

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
    public CurrencyHistoryChartView(CurrencyConverterController controller) {
        this.controller = controller;

        setSpacing(10);
        setPadding(new Insets(10));

        configureCurrencyBoxes();
        configureChart();

        thresholdField.setPromptText("Alert threshold");

        loadButton.setOnAction(e -> onLoad());

        // Controls row: From, To, Threshold, Load
        HBox controlsRow = new HBox(10,
                new Label("From:"), fromBox,
                new Label("To:"), toBox,
                thresholdField, loadButton
        );
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(controlsRow, statusLabel, chart);
    }

    /**
     * Opens this chart view inside a new standalone {@link Stage}.
     * Useful for buttons in other widgets that want to show historical data.
     *
     * @param controller the controller providing history data
     * @return the created Stage
     */
    public static Stage showInNewWindow(CurrencyConverterController controller) {
        CurrencyHistoryChartView view = new CurrencyHistoryChartView(controller);

        Stage stage = new Stage();
        stage.setTitle("Currency History - Live Data");
        stage.setScene(new Scene(view, 900, 600));
        stage.show();
        return stage;
    }

    /* =============== UI CONFIGURATION ===================== */

    /**
     * Configures the currency ComboBoxes to show only fiat currencies
     * and display only their ISO code (EUR, USD, ...).
     */
    private void configureCurrencyBoxes() {
        for (CurrencyUnit unit : FiatCurrency.values()) {
            fromBox.getItems().add(unit);
            toBox.getItems().add(unit);
        }

        // Default selection
        fromBox.setValue(FiatCurrency.EUR);
        toBox.setValue(FiatCurrency.USD);

        // Converter that shows only currency code
        StringConverter<CurrencyUnit> codeOnly = new StringConverter<>() {
            @Override
            public String toString(CurrencyUnit unit) {
                return unit == null ? "" : unit.getCode();
            }

            @Override
            public CurrencyUnit fromString(String code) {
                return null; // Not needed
            }
        };

        fromBox.setConverter(codeOnly);
        toBox.setConverter(codeOnly);

        fromBox.setCellFactory(lv -> codeOnlyCell());
        toBox.setCellFactory(lv -> codeOnlyCell());
    }

    /** Creates a ListCell that displays only the currency code. 
     * 
     * @return a ListCell for ComboBox items
    */
    private ListCell<CurrencyUnit> codeOnlyCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(CurrencyUnit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCode());
            }
        };
    }

    /**
     * Configures the line chart appearance and axes.
     */
    private void configureChart() {
        xAxis.setLabel("Date");
        yAxis.setLabel("Rate");

        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
    }

    /* ================== LOAD & PLOT ======================= */

    /**
     * Loads historical data for the selected currency pair and plots it.
     * Also draws a threshold line if provided.
     */
    private void onLoad() {
        CurrencyUnit from = fromBox.getValue();
        CurrencyUnit to = toBox.getValue();

        if (from == null || to == null || from.equals(to)) {
            statusLabel.setText("Please select two different currencies.");
            return;
        }

        // Date range: last DEFAULT_HISTORY_DAYS days
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(DEFAULT_HISTORY_DAYS);

        // Fetch history points from controller
        List<CurrencyHistoryPoint> points =
                controller.getHistoryPoints(from, to, fromDate, toDate);

        points.forEach(p -> System.out.println(p.getDate() + " -> " + p.getRate()));

        Double threshold = parseThreshold(thresholdField.getText());

        plot(points, from.getCode() + " -> " + to.getCode(), threshold);

        statusLabel.setText("Loaded " + points.size() + " points.");
    }

    /**
     * Plots the historical points on the chart and optionally draws a threshold line.
     *
     * @param points     list of historical exchange‑rate points
     * @param seriesName name of the main data series
     * @param threshold  optional threshold value (null if not used)
     */
    private void plot(List<CurrencyHistoryPoint> points, String seriesName, Double threshold) {
        chart.getData().clear();

        if (points == null || points.isEmpty()) {
            return;
        }

        // Main series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (CurrencyHistoryPoint p : points) {
            series.getData().add(new XYChart.Data<>(
                    p.getDate().format(DATE_FORMAT),
                    p.getRate()
            ));
        }

        chart.getData().add(series);

        // Threshold line series
        if (threshold != null) {
            XYChart.Series<String, Number> thresholdSeries = new XYChart.Series<>();
            thresholdSeries.setName("Threshold (" + threshold + ")");

            for (CurrencyHistoryPoint p : points) {
                thresholdSeries.getData().add(new XYChart.Data<>(
                        p.getDate().format(DATE_FORMAT),
                        threshold
                ));
            }

            chart.getData().add(thresholdSeries);

            // Style dashed red line
            if (thresholdSeries.getNode() != null) {
                thresholdSeries.getNode().setStyle("-fx-stroke: red; -fx-stroke-dash-array: 5 5;");
            }
        }
    }

    /**
     * Parses a threshold value from user input.
     *
     * @param text user input
     * @return parsed double or null if invalid
     */
    private Double parseThreshold(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 
     * Returns the underlying chart node for embedding in other layouts.
     *  
     * @return the underlying chart node
     */
    public Node getChartNode() {
        return chart;
    }
}
