package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.investment.Position;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.transactions.base.InvestmentTransaction;
import it.unibo.unibodget.model.wallet.Wallet;
import it.unibo.unibodget.view.main.SideBarDelegate;
import it.unibo.unibodget.view.main.SideBarItem;
import it.unibo.unibodget.view.main.SideBarViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class InvestmentsViewController implements SideBarDelegate {

    InvestmentController investmentController;
    @FXML private VBox walletList;
    @FXML private SideBarViewController sideBarViewController;
    @FXML private Label currentWalletName;

    @FXML private TableView<Position> positionTableView;
    @FXML private TableColumn<Position, String> tickerColumn;
    @FXML private TableColumn<Position, String> priceColumn;
    @FXML private TableColumn<Position, String> quantityColumn;
    @FXML private TableColumn<Position, String> totalCostColumn;
    @FXML private TableColumn<Position, String> profitLossColumn;

    private Timeline currentMarkerPriceRefreshTimeline;
    private final static int REFRESH_EACH_SECONDS = 60;

    @FXML private TableView<InvestmentTransaction> investmentTransactionTableView;
    @FXML private TableColumn<InvestmentTransaction, String> txnDateColumn;
    @FXML private TableColumn<InvestmentTransaction, String> txnAssetColumn;
    @FXML private TableColumn<InvestmentTransaction, String> txnQuantityColumn;
    @FXML private TableColumn<InvestmentTransaction, String> txnUnitPriceColumn;
    @FXML private TableColumn<InvestmentTransaction, String> txnFeeColumn;
    @FXML private TableColumn<InvestmentTransaction, String> txnNotesColumn;

    @FXML
    public void initialize() {
        // chiamato automaticamente da FXMLLoader dopo il caricamento

        tickerColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().asset().getShortName()
                )
        );

        priceColumn.setCellValueFactory(cell -> {
            var position = cell.getValue();
            var unitCurrentMarketValue = position.currentMarketValue()
                    .amount()
                    .divide(position.quantity(), 2,  RoundingMode.HALF_UP);
            return new SimpleStringProperty(
                    String.format(
                            "%s %.2f (%s)",
                            position.currentMarketValue().currency().getSymbol(),
                            unitCurrentMarketValue,
                            position.currentMarketValue().currency().getShortName()
                    )
            );
        });
        currentMarkerPriceRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(REFRESH_EACH_SECONDS), e -> refreshPositionTable())
        );
        currentMarkerPriceRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        currentMarkerPriceRefreshTimeline.play();

        quantityColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().quantity().stripTrailingZeros().toPlainString()
                )
        );

        totalCostColumn.setCellValueFactory(cell -> {
            var decimals = cell.getValue().getTotalCost().currency().getDisplayDecimals();
            var totalCost = cell.getValue().getTotalCost();
            return new SimpleStringProperty(
                    String.format(
                            "%s %s (%s)",
                            totalCost.currency().getSymbol(),
                            totalCost.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString(),
                            totalCost.currency().getShortName()
                    )
            );
        });

        profitLossColumn.setCellValueFactory(cell -> {
            var decimals = cell.getValue().getTotalCost().currency().getDisplayDecimals();
            var pl = cell.getValue().getUnrealizedProfitLoss();
            var percent = cell.getValue().getUnrealizedProfitLossPercentage();
            return new SimpleStringProperty(
                    String.format(
                            "%s %+." + decimals + "f (%%%+.2f)",
                            pl.currency().getSymbol(),
                            pl.amount().doubleValue(),
                            percent.doubleValue()
                    )
            );
        });
        profitLossColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                }
                else {
                    setText(value);
                    var position = getTableView().getItems().get(getIndex());
                    if (position.getUnrealizedProfitLoss().isNegative()) {
                        setStyle("-fx-text-fill: #F44336;"); // rosso
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // verde
                    }
                }
            }
        });

        txnDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDate().toString()
                )
        );
        txnAssetColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getAsset().currency().getShortName()
                )
        );
        txnQuantityColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getAsset().amount().stripTrailingZeros().toPlainString()
                )
        );
        txnQuantityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                if (empty || value == null) {
                    return;
                } else {
                    setText(value);
                    var txn = getTableView().getItems().get(getIndex());
                    if (txn.getAsset().isNegative()) {
                        setStyle("-fx-text-fill: #F44336;"); // rosso
                    } else {
                        setStyle("-fx-text-fill: #4CAF50;"); // verde
                    }
                }
            }
        });
        txnUnitPriceColumn.setCellValueFactory(cell -> {
            var decimals = cell.getValue().getUnitPrice().currency().getDisplayDecimals();
            var unitPrice = cell.getValue().getUnitPrice();
            return new SimpleStringProperty(
                    String.format(
                            "%s %s (%s)",
                            unitPrice.currency().getSymbol(),
                            unitPrice.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString(),
                            unitPrice.currency().getShortName()
                    )
            );
        });

        txnFeeColumn.setCellValueFactory(cell -> {
            var decimals = cell.getValue().getUnitPrice().currency().getDisplayDecimals();
            var fee = cell.getValue().getFee();
            return new SimpleStringProperty(
                    String.format(
                            "%s %s (%s)",
                            fee.currency().getSymbol(),
                            fee.amount().setScale(decimals, RoundingMode.HALF_UP).toPlainString(),
                            fee.currency().getShortName()
                    )
            );
        });
        txnNotesColumn.setCellValueFactory(cell -> {
            return new SimpleStringProperty(
                    cell.getValue().getNotes()
            );
        });

        sideBarViewController.setDelegate(this);
        sideBarViewController.refresh();
    }

    private void refreshPositionTable() {
        positionTableView.getItems().setAll(investmentController.getPositions());
        positionTableView.refresh();
    }

    public void stopRefresh() {
        if (currentMarkerPriceRefreshTimeline != null) {
            currentMarkerPriceRefreshTimeline.stop();
        }
    }

    public void setInvestmentController(final InvestmentController investmentController) {
        this.investmentController = investmentController;
    }

    public void refreshData() {

    }

    @Override
    public List<SideBarItem> getItems() {
        return investmentController.getAllInvestmentAccounts().stream()
                .map(w -> new SideBarItem(
                        w.getId(),
                        w.getName(),
                        String.format("%s %.2f", w.getBalance().currency().getSymbol(), w.getBalance().amount()),
                        w.getId().equals(investmentController.getCurrentInvestmentAccount()
                                .map(Wallet::getId).orElse(null))
                ))
                .toList();
    }

    @Override
    public String getTotalAggregatedBalance() {
        return String.format(
                "%s %.2f",
                investmentController.getAggregatedBalance().currency().getSymbol(),
                investmentController.getAggregatedBalance().amount()
        );
    }

    private void changeCurrentWalletName() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        currentWalletName.setText(investmentController.getCurrentInvestmentAccount().get().getName());
    }

    @FXML
    private void handleCurrentWalletNameChange() {
        if (investmentController.getCurrentInvestmentAccount().isEmpty()) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(String.format("Change %s's account current name:",  investmentController.getCurrentInvestmentAccount().get().getName()));
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

    private void refreshMainPanel() {
        changeCurrentWalletName();

    }

    @Override
    public void onItemSelected(UUID id) {
        investmentController.selectWallet(id);
        changeCurrentWalletName();
        refreshMainPanel();
        sideBarViewController.refresh();
    }
}
