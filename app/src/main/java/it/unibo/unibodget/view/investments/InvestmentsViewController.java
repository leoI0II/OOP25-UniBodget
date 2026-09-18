package it.unibo.unibodget.view.investments;

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
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
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
    private TableView<Position> positionTableView;
    @FXML
    private TableColumn<Position, String> tickerColumn;
    @FXML
    private TableColumn<Position, String> priceColumn;
    @FXML
    private TableColumn<Position, String> quantityColumn;
    @FXML
    private TableColumn<Position, String> totalCostColumn;
    @FXML
    private TableColumn<Position, String> profitLossColumn;

    private Timeline currentMarkerPriceRefreshTimeline;

    @FXML
    private TableView<InvestmentTransaction> investmentTransactionTableView;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnDateColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnAssetColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnQuantityColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnUnitPriceColumn;
    @FXML
    private TableColumn<InvestmentTransaction, String> txnFeeColumn;
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
        startPriceRefreshTimeline();
    }

    /**
     * Configures cell-value factories and cell rendering for the positions table.
     */
    private void setupPositionTable() {
        tickerColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().asset().getShortName()));

        priceColumn.setCellValueFactory(cell -> {
            final var position = cell.getValue();
            final var unitCurrentMarketValue = position.currentMarketValue()
                    .amount()
                    .divide(position.quantity(), 2, RoundingMode.HALF_UP);
            return new SimpleStringProperty(
                    String.format(
                            "%s %.2f (%s)",
                            position.currentMarketValue().currency().getSymbol(),
                            unitCurrentMarketValue,
                            position.currentMarketValue().currency().getShortName()));
        });

        quantityColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().quantity().stripTrailingZeros().toPlainString()));

        totalCostColumn.setCellValueFactory(cell -> {
            final var totalCost = cell.getValue().getTotalCost();
            return new SimpleStringProperty(
                    AssetFormatter.ofAssetWithName(totalCost)
            );
        });

        profitLossColumn.setCellValueFactory(cell -> {
            final var decimals = cell.getValue().getTotalCost().currency().getDisplayDecimals();
            final var pl = cell.getValue().getUnrealizedProfitLoss();
            final var percent = cell.getValue().getUnrealizedProfitLossPercentage();
            final var sign = pl.isNegative() ? "-" : "";
            return new SimpleStringProperty(
                    String.format(
                            "%s %s%s (%+.2f%%)",
                            pl.currency().getSymbol(),
                            sign,
                            pl.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString(),
                            percent));
        });
        profitLossColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(final String value, final boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value);
                    final var position = getTableView().getItems().get(getIndex());
                    if (position.getUnrealizedProfitLoss().isNegative()) {
                        setStyle("-fx-text-fill: #F44336;"); // rosso
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // verde
                    }
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
        txnQuantityColumn.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getAsset().amount().stripTrailingZeros().toPlainString()));
        txnQuantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(final String value, final boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                } else {
                    setText(value);
                    final var txn = getTableView().getItems().get(getIndex());
                    if (txn.getAsset().isNegative()) {
                        setStyle("-fx-text-fill: #F44336;"); // rosso
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // verde
                    }
                }
            }
        });
        txnUnitPriceColumn.setCellValueFactory(cell -> {
            final var unitPrice = cell.getValue().getUnitPrice();
            return new SimpleStringProperty(
                    AssetFormatter.ofAssetWithName(unitPrice)
            );
        });

        txnFeeColumn.setCellValueFactory(cell -> {
            final var fee = cell.getValue().getFee();
            if (fee == null) {
                return new SimpleStringProperty("-");
            }
            return new SimpleStringProperty(
                    AssetFormatter.ofAssetWithName(fee)
            );
        });
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
     * @param title     chart title
     */
    private void refreshPie(
            final PieChart chart,
            final List<Position> positions,
            final Function<Position, PieChart.Data> sliceFunc,
            final String title) {
        if (positions.isEmpty()) {
            chart.getData().clear();
            return;
        }
        final var slices = positions.stream().map(sliceFunc).toList();
        chart.getData().setAll(slices);
        chart.setTitle(title);
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
                "Allocation Pie");
        refreshPie(
                quantityPieChart,
                positions,
                p -> new PieChart.Data(p.asset().getShortName(), p.quantity().doubleValue()),
                "Quantity Pie");
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
        return String.format(
                "%s %.2f",
                investmentController.getAggregatedBalance().currency().getSymbol(),
                investmentController.getAggregatedBalance().amount());
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
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setWidth(400);
        dialog.setHeight(400);
        dialog.setResizable(false);
        dialog.setTitle(String.format("Change %s's account current name:",
                investmentController.getCurrentInvestmentAccount().get().getName()));
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new name:");

        dialog.showAndWait().ifPresent(newName -> {
            investmentController.getCurrentInvestmentAccount()
                    .ifPresent(w -> {
                        w.setName(newName);
                        currentWalletName.setText(newName);
                        sideBarViewController.refresh();
                    });
        });
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
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            dialog.setWidth(ADD_TRANSACTION_DIALOG_WIN_WIDTH);
            dialog.setHeight(ADD_TRANSACTION_DIALOG_WIN_HEIGHT);
            dialog.setResizable(false);
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
        allTimeProfitValue.setText(
                AssetFormatter.ofAsset(investmentController.getCurrentAllTimeProfitLoss())
        );
        allTimeProfitPercentage.setText(
                AssetFormatter.ofPercentage(investmentController.getCurrentAllTimeProfitLossPercentage())
        );
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
        }, () -> bestPerformerPositionName.setText("N/A"));

        investmentController.getWorstPerformer().ifPresentOrElse(p -> {
            worstPerformerPositionName.setText(p.asset().getShortName());
            worstPerformerPositionValue.setText(AssetFormatter.ofAsset(p.getUnrealizedProfitLoss()));
            worstPerformerPositionPercentage.setText(AssetFormatter.ofPercentage(p.getUnrealizedProfitLossPercentage()));
        }, () -> worstPerformerPositionName.setText("N/A"));

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
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            dialog.setWidth(ADD_NEW_WALLET_DIALOG_WIDTH);
            dialog.setHeight(ADD_NEW_WALLET_DIALOG_HEIGHT);
            dialog.setResizable(false);
            dialog.setOnCloseRequest(event -> addNewWalletVC.dispose());
            dialog.showAndWait();

            addNewWalletVC.dispose();
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
