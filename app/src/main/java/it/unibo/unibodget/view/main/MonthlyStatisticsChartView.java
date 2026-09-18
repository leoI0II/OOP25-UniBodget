package it.unibo.unibodget.view.main;

import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.transactions.MonthlyStatistics;
import it.unibo.unibodget.model.transactions.MonthlyStatistics.CategoryTotal;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * JavaFX view responsible for rendering a bar chart of per‑category totals
 * for a specific month, along with a summary row showing income, expenses,
 * and net balance.
 * 
 * <p>
 * This class is presentation‑only: it receives a fully prepared
 * {@link MonthlyStatistics} instance via {@link #setData(MonthlyStatistics)}
 * and draws it. No filtering, aggregation, or date logic is performed here.
 * 
 * <p>
 * The chart displays one bar per category, colored according to the
 * {@link CategoryType} (income or expense). Summary labels provide a quick
 * textual overview of the month.
 */
public final class MonthlyStatisticsChartView extends VBox {

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final int HBOX_SPACING = 20;

    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

    private final Label titleLabel = new Label();
    private final Label incomeLabel = new Label();
    private final Label expenseLabel = new Label();
    private final Label netLabel = new Label();

    /**
     * Creates a new {@code MonthlyStatisticsChartView} with default spacing,
     * padding, axis labels, and summary row styling.
     * 
     * <p>
     * The chart is configured without animations and without a legend,
     * since each bar already represents a single category.
     */
    public MonthlyStatisticsChartView() {
        setSpacing(8);
        setPadding(new Insets(10));

        xAxis.setLabel("Category");
        yAxis.setLabel("Amount");
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        incomeLabel.setStyle("-fx-text-fill: #2e7d32;");
        expenseLabel.setStyle("-fx-text-fill: #c62828;");
        netLabel.setStyle("-fx-font-weight: bold;");

        final HBox summaryRow = new HBox(HBOX_SPACING, incomeLabel, expenseLabel, netLabel);

        getChildren().addAll(titleLabel, summaryRow, chart);
    }

    /**
     * Populates the chart and summary labels with the data contained in the
     * given {@link MonthlyStatistics} object.
     * 
     * <p>
     * Behavior:
     * <ul>
     *     <li>If {@code stats} is {@code null}, the view displays a
     *         "No data" message and clears all fields.</li>
     *     <li>Otherwise, the chart is cleared and repopulated with one bar
     *         per category.</li>
     *     <li>Bars are colored according to the category type
     *         (income = green, expense = red).</li>
     * </ul>
     *
     * @param stats the monthly statistics to display; may be {@code null}
     */
    public void setData(final MonthlyStatistics stats) {
        chart.getData().clear();

        if (stats == null) {
            titleLabel.setText("No data");
            incomeLabel.setText("");
            expenseLabel.setText("");
            netLabel.setText("");
            return;
        }

        titleLabel.setText(stats.getMonth().atDay(1).format(MONTH_FORMAT));
        incomeLabel.setText("Income: " + stats.getTotalIncome());
        expenseLabel.setText("Expense: " + stats.getTotalExpense());
        netLabel.setText("Net: " + stats.getNetBalance());

        final XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Categories");

        for (final CategoryTotal categoryTotal : stats.getCategoryTotals()) {
            final XYChart.Data<String, Number> data =
                    new XYChart.Data<>(categoryTotal.categoryName(),
                                       categoryTotal.total().abs());
            series.getData().add(data);
            colorBarWhenReady(data, categoryTotal.type());
        }

        chart.getData().add(series);
    }

    /**
     * Applies a color to a bar once its underlying JavaFX node becomes available.
     * 
     * <p>
     * JavaFX creates the bar's node lazily, only after the data has been added
     * to a rendered chart. Therefore, styling must be applied via a listener
     * on the node property rather than immediately after constructing the
     * {@link XYChart.Data} object.
     *
     * @param data the chart data point whose bar should be styled
     * @param type the category type determining the bar color
     */
    private void colorBarWhenReady(final XYChart.Data<String, Number> data, final CategoryType type) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                final String color = type == CategoryType.INCOME ? "#2e7d32"
                                : type == CategoryType.EXPENSE ? "#c62828"
                                : "#757575";
                newNode.setStyle("-fx-bar-fill: " + color + ";");
            }
        });
    }
}
