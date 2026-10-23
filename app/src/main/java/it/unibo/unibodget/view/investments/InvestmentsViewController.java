package it.unibo.unibodget.view.investments;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.BalanceSnapshot;
import it.unibo.unibodget.model.investment.ExportResult;
import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.investment.service.InvestmentsSnapshotService;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.MainInfoNotificationEvent;
import it.unibo.unibodget.model.utils.event.NewWalletAddedEvent;
import it.unibo.unibodget.model.utils.event.OrderResultEvent;
import it.unibo.unibodget.model.utils.event.TickEvent;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.wallet.AbstractWallet;
import it.unibo.unibodget.view.main.AppContext;
import it.unibo.unibodget.view.main.BaseViewController;
import it.unibo.unibodget.view.main.SideBarDelegate;
import it.unibo.unibodget.view.main.SideBarItem;
import it.unibo.unibodget.view.main.SideBarViewController;
import it.unibo.unibodget.view.main.ViewControllersFactory;
import it.unibo.unibodget.view.main.WalletRenamePrompt;
import it.unibo.unibodget.view.utils.AssetFormatter;
import it.unibo.unibodget.view.utils.DialogHelper;
import it.unibo.unibodget.view.utils.ToastNotification;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX controller for the investments view.
 * Displays portfolio positions, transaction history, performance charts,
 * and delegates sidebar interactions via {@link SideBarDelegate}.
 */
public final class InvestmentsViewController extends BaseViewController implements SideBarDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentsViewController.class);
    // how the time of the points is shown under the P/L chart, it depends on how long is the
    // period in the chart (see chartDateFormat)
    private static final DateTimeFormatter TIME_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final int ADD_TRANSACTION_DIALOG_WIN_WIDTH = 450;
    private static final int ADD_TRANSACTION_DIALOG_WIN_HEIGHT = 620;
    private static final int ADD_NEW_WALLET_DIALOG_WIDTH = 300;
    private static final int ADD_NEW_WALLET_DIALOG_HEIGHT = 400;
    private static final int RENAME_DIALOG_SIZE = 400;
    private static final String NOT_AVAILABLE = "N/A";
    private static final String PROP_BOLD_BY_DEFAULT = "boldByDefault";
    private static final String PROFIT_STYLE = "positive";
    private static final String LOSS_STYLE = "negative";
    private static final int TOOLTIP_DELAY_MILLIS = 100;
    // one color for each tradeable asset (7 stocks + 17 crypto), so two assets never have the same one
    private static final List<String> ASSET_COLORS = List.of(
            "#3861FB", "#F7A64B", "#16C784", "#EA3943", "#8E6CEF", "#2FC0D6",
            "#F0B90B", "#FF6FB5", "#7CDB5C", "#4FA3FF", "#FF8A5B", "#B07CFF",
            "#26D9A6", "#FFD166", "#E05FD8", "#5EC8FF", "#C9A227", "#FF5C8A",
            "#66E3C4", "#A0A7FF", "#FFA07A", "#9BE15D", "#D68FFF", "#A1A7BB"
    );

    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final ViewControllersFactory viewControllersFactory;
    @FXML private VBox investmentsVBoxPage;
    private Window addNewWalletDialog;

    private SideBarViewController sideBarViewController;
    @FXML
    private Button closeButton;
    @FXML
    private Button addTransactionButton;
    private Window addTransactionDialog;
    @FXML
    private Label currentWalletName;
    @FXML
    private Label walletBalance;
    @FXML
    private Label allTimeProfitValue;
    @FXML
    private Label allTimeProfitPercentage;
    @FXML
    private Label costBasisValue;

    @FXML
    private Label bestPerformerPositionName;
    @FXML
    private Label bestPerformerPositionValue;
    @FXML
    private Label bestPerformerPositionPercentage;
    @FXML
    private Label worstPerformerPositionName;
    @FXML
    private Label worstPerformerPositionValue;
    @FXML
    private Label worstPerformerPositionPercentage;

    @FXML
    private LineChart<Number, Number> performanceChart;
    @FXML
    private NumberAxis chartXAxis;
    @FXML
    private PieChart allocationPieChart;
    @FXML
    private PieChart quantityPieChart;
    @FXML
    private ToggleGroup pieToggleGroup;
    // created only once: JavaFX gives the color (default-color0, 1, ...) with a counter that never
    // goes back to 0, so new series at every refresh had every time other colors
    private final XYChart.Series<Number, Number> plSeries = new XYChart.Series<>();
    // changed at every refresh of the chart, before the axis draws the labels
    private DateTimeFormatter chartDateFormat = TIME;

    @FXML
    private TableView<Position> positionTableView;
    @FXML
    private TableColumn<Position, String> tickerColumn;
    @FXML
    private TableColumn<Position, Position> priceColumn;
    @FXML
    private TableColumn<Position, Position> quantityColumn;
    @FXML
    private TableColumn<Position, Position> totalCostColumn;
    @FXML
    private TableColumn<Position, Position> profitLossColumn;

    @FXML
    private TableView<InvestmentTransaction> investmentTransactionTableView;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnDateColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnAssetColumn;
    @FXML
    private TableColumn<InvestmentTransaction, InvestmentTransaction> txnQuantityColumn;
    @FXML
    private TableColumn<InvestmentTransaction, InvestmentTransaction> txnUnitPriceColumn;
    @FXML
    private TableColumn<InvestmentTransaction, InvestmentTransaction> txnFeeColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnNotesColumn;

    /**
     * Creates the controller with its required dependencies.
     *
     * @param investmentController   handles all investment business logic
     * @param snapshotService        provides historical balance snapshots for the chart
     * @param viewControllersFactory factory used to instantiate sub-dialogs (e.g. add-transaction)
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of controllers and services")
    public InvestmentsViewController(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService,
            final ViewControllersFactory viewControllersFactory) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);

        subscribe(OrderResultEvent.class, this::onOrderResultEvent);
        subscribe(NewWalletAddedEvent.class, ignored -> onNewWalletAddedEvent());
        // refresh with the new prices at every tick of the app ticker. before the page had its own
        // timeline, that was never stopped when changing page: every visit started one more
        subscribe(TickEvent.class, ignored -> refreshMainPanel());
    }

    /**
     * Injects the sidebar controller so this view can trigger refreshes on wallet selection.
     *
     * @param sideBarViewController the sidebar controller, must not be {@code null}
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dependency injection of sidebar controller")
    public void setSideBarViewController(final SideBarViewController sideBarViewController) {
        this.sideBarViewController = Objects.requireNonNull(sideBarViewController);
    }

    /**
     * Called automatically by {@link javafx.fxml.FXMLLoader} after the FXML is loaded.
     * Sets up the position and history tables, the pies and the P/L chart.
     */
    @FXML
    public void initialize() {
        if (closeButton != null) {
            closeButton.getProperties().put(PROP_BOLD_BY_DEFAULT, Boolean.TRUE);
        }
        setupPositionTable();
        setupHistoryTable();
        setupPieToggle();
        setupPerformanceChart();
    }

    /**
     * Adds the P/L series to the chart, only once.
     * At every refresh only its points change (see {@link #refreshPerformanceLineChart()}).
     */
    private void setupPerformanceChart() {
        plSeries.setName("Total P/L");
        // only the P/L: the cost basis jumps up and down with every buy and sell (it is not a
        // performance) and with its big values the P/L line was flat on the zero.
        // the cost basis is still saved in the snapshots and shown in the card
        performanceChart.getData().setAll(List.of(plSeries));
        // the x is the time in milliseconds, so the distance between two points is the real time
        // (with the old CategoryAxis of strings all the points had the same distance). here the
        // number is shown back like a date
        chartXAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(final Number millis) {
                return chartDateFormat.format(toDateTime(millis.longValue()));
            }

            @Override
            public Number fromString(final String text) {
                throw new UnsupportedOperationException("the axis labels are only shown");
            }
        });
    }

    /**
     * Chooses how to show the dates under the P/L chart, from the first and last point: with few
     * minutes of data the seconds are needed, with months of data only the day.
     *
     * @param first the time of the first point
     * @param last the time of the last point
     * @return the format for the labels of the x axis
     */
    private static DateTimeFormatter chartDateFormat(final LocalDateTime first, final LocalDateTime last) {
        if (ChronoUnit.HOURS.between(first, last) < 1) {
            return TIME_WITH_SECONDS;
        }
        if (ChronoUnit.DAYS.between(first, last) < 1) {
            return TIME;
        }
        if (ChronoUnit.MONTHS.between(first, last) < 1) {
            return DAY_AND_TIME;
        }
        return DATE;
    }

    /**
     * Converts a date to the number used on the x axis of the P/L chart.
     *
     * @param time the date
     * @return milliseconds from 1970-01-01, in the time zone of the system
     */
    private static long toMillis(final LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts a number of the x axis of the P/L chart back to a date.
     *
     * @param millis milliseconds from 1970-01-01
     * @return the date, in the time zone of the system
     */
    private static LocalDateTime toDateTime(final long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * Keeps one of the Value / Quantity buttons always selected, so one pie is always visible
     * (the visibility of the pies is bound to the buttons in the FXML).
     */
    private void setupPieToggle() {
        pieToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            // clicking again the selected button unselects it, so we select it back
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });
    }

    /**
     * Configures cell-value factories and cell rendering for the positions table.
     */
    private void setupPositionTable() {
        tickerColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().asset().getShortName()));

        setupNumericColumn(priceColumn,
                p -> AssetFormatter.ofAssetWithName(Asset.of(p.currentMarketValue().currency(), unitPrice(p))),
                InvestmentsViewController::unitPrice,
                false);

        setupNumericColumn(quantityColumn,
                p -> p.quantity().stripTrailingZeros().toPlainString(),
                Position::quantity,
                false);

        setupNumericColumn(totalCostColumn,
                p -> AssetFormatter.ofAssetWithName(p.getTotalCost()),
                p -> p.getTotalCost().amount(),
                false);

        setupNumericColumn(profitLossColumn,
                p -> {
                    final var decimals = p.getTotalCost().currency().getDisplayDecimals();
                    final var pl = p.getUnrealizedProfitLoss();
                    // the amount has already its own "-", before it was added another one ("--0.01")
                    return String.format(
                            "%s %s (%+.2f%%)",
                            pl.currency().getSymbol(),
                            pl.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString(),
                            p.getUnrealizedProfitLossPercentage());
                },
                p -> p.getUnrealizedProfitLoss().amount(),
                true);
    }

    /**
     * Returns the current price of one unit of the position.
     *
     * @param position the position
     * @return market value divided by quantity, with the decimals of the price currency
     */
    private static BigDecimal unitPrice(final Position position) {
        return position.currentMarketValue()
                .amount()
                .divide(position.quantity(),
                        position.currentMarketValue().currency().getDisplayDecimals(),
                        RoundingMode.HALF_UP);
    }

    /**
     * Sets up a column that shows a number as text but sorts by the number itself.
     * If the column value is the text, the sort is alphabetical
     * (e.g. "81738.77" goes before "709.98" because '8' is bigger than '7').
     *
     * @param <T>         the type of the table rows
     * @param column      the column to set up
     * @param toText      gives the text shown in the cell
     * @param number      gives the number used for the sort
     * @param colorBySign if {@code true} the text is green when the number is positive and red when negative
     */
    private static <T> void setupNumericColumn(
            final TableColumn<T, T> column,
            final Function<T, String> toText,
            final Function<T, BigDecimal> number,
            final boolean colorBySign) {
        // the value of the cell is the whole row, so the comparator can take the number from it
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        column.setComparator(Comparator.comparing(number));
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                // cells are reused for other rows, so we always remove the old color
                getStyleClass().removeAll(PROFIT_STYLE, LOSS_STYLE);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(toText.apply(item));
                if (colorBySign) {
                    getStyleClass().add(number.apply(item).signum() < 0 ? LOSS_STYLE : PROFIT_STYLE);
                }
            }
        });
    }

    /**
     * Configures cell-value factories and cell rendering for the transaction history table.
     */
    private void setupHistoryTable() {
        txnDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDate().toString()));
        txnAssetColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getAsset().currency().getShortName()));

        setupNumericColumn(txnQuantityColumn,
                t -> t.getAsset().amount().stripTrailingZeros().toPlainString(),
                t -> t.getAsset().amount(),
                true);

        setupNumericColumn(txnUnitPriceColumn,
                t -> AssetFormatter.ofAssetWithName(t.getUnitPrice()),
                t -> t.getUnitPrice().amount(),
                false);

        // fee can be null, in the sort it counts as 0
        setupNumericColumn(txnFeeColumn,
                t -> t.getFee() == null ? "-" : AssetFormatter.ofAssetWithName(t.getFee()),
                t -> t.getFee() == null ? BigDecimal.ZERO : t.getFee().amount(),
                false);

        txnNotesColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getNotes()));
    }

    /**
     * Reloads and renders the P/L line chart from the current account's snapshots.
     */
    private void refreshPerformanceLineChart() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        final var account = investmentController.getCurrentInvestmentAccount().get();

        final List<BalanceSnapshot> snapshots = new ArrayList<>(snapshotService.getSnapshots(account.getId()));
        // the last point is now, only shown and not saved (the snapshots are saved every few minutes),
        // so the line always ends at the real value
        snapshots.add(BalanceSnapshot.buildInvestmentSnapshot(account));

        final List<XYChart.Data<Number, Number>> plPoints = new ArrayList<>();
        for (final BalanceSnapshot snap : snapshots) {
            plPoints.add(new XYChart.Data<>(toMillis(snap.timestamp()), snap.totalPL()));
        }
        // the format must be ready before the new points, the axis draws the labels after them
        chartDateFormat = chartDateFormat(snapshots.getFirst().timestamp(), snapshots.getLast().timestamp());
        // same series, only new points, so the colors stay the same
        plSeries.getData().setAll(plPoints);
    }

    /**
     * Populates a {@link PieChart} from a list of positions using the provided slice function.
     *
     * @param chart     the chart to update
     * @param positions the positions to render
     * @param sliceFunc maps a position to its pie slice
     * @param valueText  gives the value shown in the tooltip of the slice
     */
    private void refreshPie(
            final PieChart chart,
            final List<Position> positions,
            final Function<Position, PieChart.Data> sliceFunc,
            final Function<Position, String> valueText) {
        if (positions.isEmpty()) {
            chart.getData().clear();
            return;
        }
        final var slices = positions.stream().map(sliceFunc).toList();
        chart.getData().setAll(slices);
        // the nodes of the slices exist only after they are added to the chart
        final Map<String, String> colorByName = new HashMap<>();
        for (int i = 0; i < slices.size(); i++) {
            final String color = colorOf(positions.get(i).asset());
            colorByName.put(slices.get(i).getName(), color);
            slices.get(i).getNode().setStyle("-fx-pie-color: " + color + ";");

            final Tooltip tooltip = new Tooltip(
                    slices.get(i).getName() + "\n" + valueText.apply(positions.get(i)));
            tooltip.setShowDelay(Duration.millis(TOOLTIP_DELAY_MILLIS));
            Tooltip.install(slices.get(i).getNode(), tooltip);
        }
        // the dots of the legend are other nodes with the old default-colorN, so we color them too
        chart.lookupAll(".chart-legend-item").forEach(node -> {
            if (node instanceof Label item && item.getGraphic() != null
                    && colorByName.containsKey(item.getText())) {
                item.getGraphic().setStyle("-fx-pie-color: " + colorByName.get(item.getText()) + ";");
            }
        });
    }

    /**
     * Returns the color of an asset in the pies, always the same for the same asset
     * (also after a restart), so the user can recognize it at every refresh.
     * JavaFX instead changes the colors at every refresh (the default-colorN counter never resets).
     *
     * @param asset the asset
     * @return the css color of the asset
     */
    private String colorOf(final CurrencyUnit asset) {
        final int index = investmentController.getAllTradeableAssets().indexOf(asset);
        // an asset that is not in the list (should not happen) gets a color from its code
        final int colorIndex = index >= 0 ? index : Math.floorMod(asset.getCode().hashCode(), ASSET_COLORS.size());
        return ASSET_COLORS.get(colorIndex % ASSET_COLORS.size());
    }

    /**
     * Refreshes both allocation and quantity pie charts for the current account.
     */
    private void refreshPieCharts() {
        final var positions = investmentController.getPositions();
        refreshPie(
                allocationPieChart,
                positions,
                p -> new PieChart.Data(p.asset().getShortName(), p.currentMarketValue().amount().doubleValue()),
                p -> AssetFormatter.ofAsset(p.currentMarketValue()));
        refreshPie(
                quantityPieChart,
                positions,
                p -> new PieChart.Data(p.asset().getShortName(), p.quantity().doubleValue()),
                p -> p.quantity().stripTrailingZeros().toPlainString() + " " + p.asset().getShortName());
    }

    /**
     * Triggers a full refresh of the main panel data.
     */
    public void refreshData() {
        refreshMainPanel();
    }

    /** {@inheritDoc} */
    @Override
    public List<SideBarItem> getItems() {
        return investmentController.getAllInvestmentAccounts().stream()
                .map(w -> new SideBarItem(
                        w.getId(),
                        w.getName(),
                        String.format("%s %.2f", w.getBalance().currency().getSymbol(), w.getBalance().amount()),
                        w.getId().equals(investmentController.getCurrentInvestmentAccount()
                                .map(AbstractWallet::getId).orElse(null))))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public String getTotalAggregatedBalance() {
        try {
            return AssetFormatter.ofAsset(investmentController.getAggregatedBalance());
        } catch (final IllegalArgumentException e) {
            // the price service now throws if a price is missing (before it used 1 USD),
            // the total in the sidebar is just not shown, the rest of the page still works
            return NOT_AVAILABLE;
        }
    }

    /**
     * Updates the wallet name label to match the currently selected account.
     */
    private void changeCurrentWalletName() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        currentWalletName.setText(investmentController.getCurrentInvestmentAccount().get().getName());
    }

    /**
     * Opens a text-input dialog that lets the user rename the current investment account.
     */
    @FXML
    public void handleCurrentWalletNameChange() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }

        final TextInputDialog dialog = new TextInputDialog();
        ThemeManager.applyThemeToDialog(dialog);
        dialog.initOwner(currentWalletName.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setWidth(RENAME_DIALOG_SIZE);
        dialog.setHeight(RENAME_DIALOG_SIZE);
        dialog.setResizable(false);
        dialog.setTitle(String.format("Change %s's account current name:",
                investmentController.getCurrentInvestmentAccount().get().getName()));
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new name:");

        WalletRenamePrompt.ask(dialog, newName ->
            investmentController.getCurrentInvestmentAccount()
                    .ifPresent(w -> {
                        w.setName(newName);
                        currentWalletName.setText(newName);
                        sideBarViewController.refresh();
                    }));
    }

    /**
     * Opens a file-save dialog and exports the current account data as CSV.
     * On success or failure, fires a notification event on the {@link MessageBus}.
     */
    @FXML
    public void handleExportCSVOnMouseClicked() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }

        final var account = investmentController.getCurrentInvestmentAccount().get();
        final Optional<File> fileOpt = DialogHelper.showSaveCsvDialog(
                currentWalletName,
                "Export CSV investments data",
                account.getName() + "_investments_data");
        if (fileOpt.isEmpty()) {
            return; // user pressed cancel
        }

        final var result = investmentController.exportCurrentAccountData(fileOpt.get());
        if (result instanceof ExportResult.Error error) {
            MessageBus.send(new MainErrorNotificationEvent("Failed to export investments data: " + error.message()));
        } else if (result instanceof ExportResult.Success) {
            MessageBus.send(new MainInfoNotificationEvent("Investments data exported successfully!"));
        }
    }

    /**
     * Closes the application when the close button is clicked.
     */
    @FXML
    public void handleCloseApp() {
        Platform.exit();
    }

    /**
     * Opens the add-transaction dialog as a modal window.
     * Refreshes the view once the dialog is closed.
     */
    @FXML
    public void handleAddTransactionButtonClicked() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/investments/AddTransactionDialog.fxml"));
            loader.setControllerFactory(viewControllersFactory::create);

            final Parent root = loader.load();
            final var addTransactionController = (BaseViewController) loader.getController();

            final Stage dialog = new Stage();
            addTransactionDialog = dialog;
            dialog.initOwner(addTransactionButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Transaction");
            dialog.setScene(new Scene(root, ADD_TRANSACTION_DIALOG_WIN_WIDTH, ADD_TRANSACTION_DIALOG_WIN_HEIGHT));
            ThemeManager.applyThemeToScene(dialog.getScene());
            dialog.setResizable(false);
            closeOnEscape(dialog);
            dialog.setOnCloseRequest(event -> addTransactionController.dispose());
            dialog.showAndWait();

            addTransactionController.dispose();
            refreshData();

        } catch (final IOException e) {
            LOGGER.error("=== DIALOG ERROR ===", e);
            ToastNotification.showError(
                    addTransactionButton.getScene().getWindow(),
                    "Error opening dialog: " + e.getMessage());
        }
    }

    /**
     * Hides the add-transaction dialog when an order completes successfully.
     *
     * @param event the order-result event received from the message bus
     */
    private void onOrderResultEvent(final OrderResultEvent event) {
        if (event.result().isSuccess() && addTransactionDialog != null) {
            addTransactionDialog.hide();
        }
    }

    /**
     * Fully refreshes all UI widgets (labels, tables, charts, sidebar) from the current account state.
     */
    private void refreshMainPanel() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        changeCurrentWalletName();

        walletBalance.setText(
                AssetFormatter.ofAsset(investmentController.getCurrentBalance())
        );
        final Asset allTimeProfit = investmentController.getCurrentAllTimeProfitLoss();
        allTimeProfitValue.setText(AssetFormatter.ofAsset(allTimeProfit));
        allTimeProfitPercentage.setText(
                AssetFormatter.ofPercentage(investmentController.getCurrentAllTimeProfitLossPercentage())
        );
        setProfitColor(allTimeProfit.isNegative(), allTimeProfitValue, allTimeProfitPercentage);
        costBasisValue.setText(
                AssetFormatter.ofAsset(investmentController.getCurrentTotalCostBasis())
        );

        positionTableView.getItems().setAll(investmentController.getPositions());
        investmentTransactionTableView.getItems().setAll(investmentController.getTransactionHistory());

        investmentController.getBestPerformer().ifPresentOrElse(p -> {
            bestPerformerPositionName.setText(p.asset().getShortName());
            bestPerformerPositionValue.setText(AssetFormatter.ofAsset(p.getUnrealizedProfitLoss()));
            bestPerformerPositionPercentage.setText(
                    AssetFormatter.ofPercentage(p.getUnrealizedProfitLossPercentage())
            );
            setProfitColor(p.getUnrealizedProfitLoss().isNegative(),
                    bestPerformerPositionValue, bestPerformerPositionPercentage);
        }, () -> clearPerformer(bestPerformerPositionName, bestPerformerPositionValue,
                bestPerformerPositionPercentage));

        investmentController.getWorstPerformer().ifPresentOrElse(p -> {
            worstPerformerPositionName.setText(p.asset().getShortName());
            worstPerformerPositionValue.setText(AssetFormatter.ofAsset(p.getUnrealizedProfitLoss()));
            worstPerformerPositionPercentage.setText(AssetFormatter.ofPercentage(p.getUnrealizedProfitLossPercentage()));
            setProfitColor(p.getUnrealizedProfitLoss().isNegative(),
                    worstPerformerPositionValue, worstPerformerPositionPercentage);
        }, () -> clearPerformer(worstPerformerPositionName, worstPerformerPositionValue,
                worstPerformerPositionPercentage));

        refreshPieCharts();
        refreshPerformanceLineChart();
        sideBarViewController.refresh();
    }

    /** {@inheritDoc} */
    @Override
    public void onItemSelected(final UUID id) {
        investmentController.selectWallet(id);
        changeCurrentWalletName();
        refreshMainPanel();
    }

    /** {@inheritDoc} */
    @Override
    public void onRenameRequested(final UUID id, final String newName) {
        investmentController.renameWallet(id, newName);
        changeCurrentWalletName();
        sideBarViewController.refresh();
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteRequested(final UUID id) {
        if (!investmentController.removeWallet(id)) {
            return;
        }
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            clearMainPanel();
            sideBarViewController.refresh();
        } else {
            refreshMainPanel();
        }
    }

    /**
     * Closes the dialog when Esc is pressed, so it can be dismissed from the
     * keyboard on every platform (e.g. tiling window managers without title bar).
     * The close request is fired as for the title bar button, so the dialog's
     * {@code onCloseRequest} handler still runs.
     *
     * @param dialog the dialog stage, with its scene already set
     */
    private static void closeOnEscape(final Stage dialog) {
        dialog.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialog.fireEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });
    }

    /**
     * Resets every widget of the main panel to its empty state.
     * Used when the last investment account has been deleted, otherwise the
     * panel would keep showing the data of the removed account.
     */
    private void clearMainPanel() {
        currentWalletName.setText("");
        walletBalance.setText("");
        allTimeProfitValue.setText("");
        allTimeProfitPercentage.setText("");
        costBasisValue.setText("");
        positionTableView.getItems().clear();
        investmentTransactionTableView.getItems().clear();
        clearPerformer(bestPerformerPositionName, bestPerformerPositionValue, bestPerformerPositionPercentage);
        clearPerformer(worstPerformerPositionName, worstPerformerPositionValue, worstPerformerPositionPercentage);
        allocationPieChart.getData().clear();
        quantityPieChart.getData().clear();
        plSeries.getData().clear();
    }

    /**
     * Shows N/A in a best/worst performer card and empties its value and percentage,
     * so the data of the previous wallet is not left on screen.
     *
     * @param name       the label with the asset name
     * @param value      the label with the profit/loss value
     * @param percentage the label with the profit/loss percentage
     */
    private static void clearPerformer(final Label name, final Label value, final Label percentage) {
        name.setText(NOT_AVAILABLE);
        value.setText("");
        percentage.setText("");
    }

    /**
     * Colors the given labels green for a profit or red for a loss,
     * using the style classes of {@code theme.css}.
     *
     * @param loss   {@code true} if the value is a loss
     * @param labels the labels to color
     */
    private static void setProfitColor(final boolean loss, final Label... labels) {
        for (final Label label : labels) {
            // remove the old one first, otherwise both classes stay on the label
            label.getStyleClass().removeAll(PROFIT_STYLE, LOSS_STYLE);
            label.getStyleClass().add(loss ? LOSS_STYLE : PROFIT_STYLE);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAddWalletRequested() {
        try {
            final FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unibo/unibodget/view/jfx/fxml/investments/AddNewWalletDialog.fxml"));
            loader.setControllerFactory(viewControllersFactory::create);

            final Parent root = loader.load();
            final var addNewWalletVC = (AddNewWalletDialogViewController) loader.getController();
            addNewWalletVC.setAppContext(AppContext.INVESTMENTS);

            final Stage dialog = new Stage();
            addNewWalletDialog = dialog;
            dialog.initOwner(investmentsVBoxPage.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Wallet");
            dialog.setScene(new Scene(root, ADD_NEW_WALLET_DIALOG_WIDTH, ADD_NEW_WALLET_DIALOG_HEIGHT));
            ThemeManager.applyThemeToScene(dialog.getScene());
            dialog.setResizable(false);
            closeOnEscape(dialog);
            dialog.setOnCloseRequest(event -> addNewWalletVC.dispose());
            dialog.showAndWait();

            addNewWalletVC.dispose();
            // Reflect the newly created & selected wallet across the whole UI.
            refreshMainPanel();
            sideBarViewController.refresh();

        } catch (final IOException e) {
            LOGGER.error("=== DIALOG ERROR ===", e);
            ToastNotification.showError(
                    investmentsVBoxPage.getScene().getWindow(),
                    "Error opening dialog: " + e.getMessage());
        }
    }

    private void onNewWalletAddedEvent() {
        if (addNewWalletDialog == null) {
            return;
        }
        addNewWalletDialog.hide();
    }
}
