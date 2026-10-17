package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.currency.Asset;
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
import it.unibo.unibodget.model.wallet.AbstractWallet;
import it.unibo.unibodget.view.main.*;
import it.unibo.unibodget.view.utils.AssetFormatter;
import it.unibo.unibodget.view.utils.ToastNotification;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * JavaFX controller for the investments view.
 * Displays portfolio positions, transaction history, performance charts,
 * and delegates sidebar interactions via {@link SideBarDelegate}.
 */
public class InvestmentsViewController extends BaseViewController implements SideBarDelegate {

    private static final int REFRESH_EACH_SECONDS = 60;
    private static final int ADD_TRANSACTION_DIALOG_WIN_WIDTH = 450;
    private static final int ADD_TRANSACTION_DIALOG_WIN_HEIGHT = 620;
    private static final int ADD_NEW_WALLET_DIALOG_WIDTH = 300;
    private static final int ADD_NEW_WALLET_DIALOG_HEIGHT = 400;
    private static final String NOT_AVAILABLE = "N/A";
    private static final String PROFIT_STYLE = "positive";
    private static final String LOSS_STYLE = "negative";

    private final InvestmentController investmentController;
    private final InvestmentsSnapshotService snapshotService;
    private final ViewControllersFactory viewControllersFactory;
    @FXML private VBox investmentsVBoxPage;
    private Window addNewWalletDialog;

    private SideBarViewController sideBarViewController;
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
    private LineChart<String, Number> performanceChart;
    @FXML
    private PieChart allocationPieChart;
    @FXML
    private PieChart quantityPieChart;
    @FXML
    private ToggleGroup pieToggleGroup;

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

    private Timeline currentMarkerPriceRefreshTimeline;

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
    public InvestmentsViewController(
            final InvestmentController investmentController,
            final InvestmentsSnapshotService snapshotService,
            final ViewControllersFactory viewControllersFactory) {
        this.investmentController = Objects.requireNonNull(investmentController);
        this.snapshotService = Objects.requireNonNull(snapshotService);
        this.viewControllersFactory = Objects.requireNonNull(viewControllersFactory);

        subscribe(OrderResultEvent.class, this::onOrderResultEvent);
        subscribe(NewWalletAddedEvent.class, this::onNewWalletAddedEvent);
    }

    /**
     * Injects the sidebar controller so this view can trigger refreshes on wallet selection.
     *
     * @param sideBarViewController the sidebar controller, must not be {@code null}
     */
    public void setSideBarViewController(final SideBarViewController sideBarViewController) {
        this.sideBarViewController = Objects.requireNonNull(sideBarViewController);
    }

    /**
     * Called automatically by {@link javafx.fxml.FXMLLoader} after the FXML is loaded.
     * Sets up the position and history tables and starts the price-refresh timer.
     */
    @FXML
    public void initialize() {
        setupPositionTable();
        setupHistoryTable();
        setupPieToggle();
        startPriceRefreshTimeline();
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
     * Reloads and renders the P/L and cost-basis line chart from the current account's snapshots.
     */
    private void refreshPerformanceLineChart() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        final var account = investmentController.getCurrentInvestmentAccount().get();

        final var plSeries = new XYChart.Series<String, Number>();
        plSeries.setName("Total P/L");

        final var costSeries = new XYChart.Series<String, Number>();
        costSeries.setName("Cost Basis");

        snapshotService.getSnapshots(account.getId())
                .forEach(snap -> {
                    final String date = snap.timestamp()
                            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
                    plSeries.getData().add(new XYChart.Data<>(date, snap.totalPL()));
                    costSeries.getData().add(new XYChart.Data<>(date, snap.costBasis()));
                });
        performanceChart.getData().clear();
        performanceChart.getData().addAll(plSeries, costSeries);
    }

    /**
     * Populates a {@link PieChart} from a list of positions using the provided slice function.
     *
     * @param chart     the chart to update
     * @param positions the positions to render
     * @param sliceFunc maps a position to its pie slice
     */
    private void refreshPie(
            final PieChart chart,
            final List<Position> positions,
            final Function<Position, PieChart.Data> sliceFunc) {
        if (positions.isEmpty()) {
            chart.getData().clear();
            return;
        }
        final var slices = positions.stream().map(sliceFunc).toList();
        chart.getData().setAll(slices);
    }

    /**
     * Refreshes both allocation and quantity pie charts for the current account.
     */
    private void refreshPieCharts() {
        final var positions = investmentController.getPositions();
        refreshPie(
                allocationPieChart,
                positions,
                p -> new PieChart.Data(p.asset().getShortName(), p.currentMarketValue().amount().doubleValue()));
        refreshPie(
                quantityPieChart,
                positions,
                p -> new PieChart.Data(p.asset().getShortName(), p.quantity().doubleValue()));
    }

    /**
     * Reloads the positions table with the latest data from the controller.
     */
    private void refreshPositionTable() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }

        positionTableView.getItems().setAll(investmentController.getPositions());
        positionTableView.refresh();
    }

    /**
     * Starts (or restarts) the periodic timer that refreshes market prices in the position table.
     */
    private void startPriceRefreshTimeline() {
        if (currentMarkerPriceRefreshTimeline == null) {
            currentMarkerPriceRefreshTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(REFRESH_EACH_SECONDS), e -> refreshPositionTable()));
        }
        currentMarkerPriceRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        currentMarkerPriceRefreshTimeline.playFromStart();
    }

    /**
     * Stops the periodic market-price refresh timer.
     */
    public void stopRefresh() {
        if (currentMarkerPriceRefreshTimeline != null) {
            currentMarkerPriceRefreshTimeline.stop();
        }
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
    private void handleCurrentWalletNameChange() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }

        final TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(currentWalletName.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setWidth(400);
        dialog.setHeight(400);
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
    private void handleExportCSVOnMouseClicked() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }

        final var account = investmentController.getCurrentInvestmentAccount().get();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV investments data");

        final var accountName = account.getName().replaceAll("\\s+", "_");
        fileChooser.setInitialFileName(accountName + "_investments_data.csv");

        final var downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (downloadsDir.exists()) {
            fileChooser.setInitialDirectory(downloadsDir);
        }
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        final var stage = (Stage) currentWalletName.getScene().getWindow();
        final var selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) {
            return; // user pressed cancel
        }

        final var result = investmentController.exportCurrentAccountData(selectedFile);
        switch (result) {
            case ExportResult.Error error ->
                    MessageBus.send(new MainErrorNotificationEvent("Export failed: " + error.message()));
            case ExportResult.Success success ->
                    MessageBus.send(new MainInfoNotificationEvent("Export successful!"));
        }
    }

    /**
     * Opens the add-transaction dialog as a modal window.
     * Refreshes the view once the dialog is closed.
     */
    @FXML
    private void handleAddTransactionButtonClicked() {
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
            dialog.setResizable(false);
            closeOnEscape(dialog);
            dialog.setOnCloseRequest(event -> addTransactionController.dispose());
            dialog.showAndWait();

            addTransactionController.dispose();
            refreshData();

        } catch (final IOException e) {
            System.err.println("=== DIALOG ERROR ===");
            e.printStackTrace();
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
        performanceChart.getData().clear();
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
     * using the style classes of {@code investments.css}.
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
            dialog.setResizable(false);
            closeOnEscape(dialog);
            dialog.setOnCloseRequest(event -> addNewWalletVC.dispose());
            dialog.showAndWait();

            addNewWalletVC.dispose();
            // Reflect the newly created & selected wallet across the whole UI.
            refreshMainPanel();
            sideBarViewController.refresh();

        } catch (final IOException e) {
            System.err.println("=== DIALOG ERROR ===");
            e.printStackTrace();
            ToastNotification.showError(
                    investmentsVBoxPage.getScene().getWindow(),
                    "Error opening dialog: " + e.getMessage());
        }
    }

    private void onNewWalletAddedEvent(final NewWalletAddedEvent event) {
        if (addNewWalletDialog == null) {
            return;
        }
        addNewWalletDialog.hide();
    }
}
