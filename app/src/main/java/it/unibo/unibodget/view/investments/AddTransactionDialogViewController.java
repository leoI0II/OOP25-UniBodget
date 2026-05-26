package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.OrderResultEvent;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.view.main.BaseViewController;
import it.unibo.unibodget.view.utils.AssetFormatter;
import it.unibo.unibodget.view.utils.ToastNotification;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public class AddTransactionDialogViewController extends BaseViewController {

    private final InvestmentController investmentController;

    // contenitori per tab BUY
    @FXML private TabPane transactionTypeTabPane;
    @FXML private Tab buyTab;
    @FXML private Tab sellTab;
    @FXML private Tab transferTab;
    private Map<Tab, OrderType> orderTypeMapByTab;      // no puo essere final a causa che gli tab sono caricati dopo
    @FXML private ComboBox<CurrencyUnit> selectedAssetComboBox;
    @FXML private TextField quantityTextField;
    @FXML private ToggleButton maxQuantityToggleButton;     // solo per SELL
    @FXML private TextField pricePerAssetTextField;
    @FXML private DatePicker datePicker;
    @FXML private TextField feeTextField;
    @FXML private TextArea notesTextArea;
    @FXML private ToggleButton cashAccountToggleButton;
    @FXML private ToggleButton stableCoinToggleButton;
    @FXML private ToggleButton noPaymentToggleButton;
    @FXML private ComboBox<Object> paymentSourceComboBox;
    @FXML private Label totalSpentReceivedTextLabel;
    @FXML private Label totalSpentValueLabel;
    @FXML private Button addTransactionButton;

    @FXML private VBox buySellFieldsPanel;
    // transfer specific fields
    @FXML private VBox transferFieldsPanel;
    @FXML private ComboBox<InvestmentAccount> destinationAccountComboBox;
    @FXML private ComboBox<CurrencyUnit> transferAssetComboBox;
    @FXML private TextField transferQuantityTextField;
    @FXML private ToggleButton transferMaxQuantityToggleButton;
    @FXML private DatePicker transferDatePicker;
    @FXML private TextArea transferNotesTextArea;
    @FXML private Label transferTotalLabel;

    public AddTransactionDialogViewController(InvestmentController investmentController) {
        this.investmentController = Objects.requireNonNull(investmentController);
    }

    public void initialize() {
        orderTypeMapByTab = Map.of(
                buyTab, OrderType.BUY,
                sellTab, OrderType.SELL,
                transferTab, OrderType.TRANSFER
        );
        // setup STRUTTURALE — una volta sola
        setupBuyAssetComboBox();
        setupTransferFields();
        setupQuantityTextField();
        setupPricePerAssetTextField();
        setupDatePicker();
        setupFeeTextField();
        setupNotesTextArea();
        setupTransactionTypeTabPane();
        setupPaymentSourceToggleButtons();
        setupTotalSpentValueLabel();
        setupAddTransactionButton();
        setupTotalSpentReceivedTextLabel();
    }

    private void setupTotalSpentReceivedTextLabel() {
        var currentTab = transactionTypeTabPane.getSelectionModel().getSelectedItem();
        OrderType orderType = orderTypeMapByTab.get(currentTab);
        totalSpentReceivedTextLabel.setText(switch(orderType) {
            case BUY -> "Total Spent";
            case SELL -> "Total Received";
            case TRANSFER -> "Total Transferred";
        });
    }

    private void resetForm(Tab tab) {
        // buy/sell fields
        quantityTextField.clear();
        pricePerAssetTextField.clear();
        feeTextField.clear();
        notesTextArea.clear();
        datePicker.setValue(LocalDate.now());
        var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        totalSpentValueLabel.setText(fmtAsset(
                Asset.zero(baseCurrency)
        ));

        // aggiorna lista asset in base al tab
        if (tab == sellTab) {
            selectedAssetComboBox.setPromptText("Select asset to sell...");
            selectedAssetComboBox.setItems(FXCollections.observableArrayList(
                    investmentController.getAllOwnedAssets()
            ));
        } else if (tab == buyTab) {
            selectedAssetComboBox.setPromptText("Select asset to buy...");
            selectedAssetComboBox.setItems(FXCollections.observableArrayList(
                    investmentController.getAllTradeableAssets()
            ));
        }
        selectedAssetComboBox.setValue(
                selectedAssetComboBox.getItems().stream().findFirst().orElse(null)
        );

        // transfer fields
        transferQuantityTextField.clear();
        transferNotesTextArea.clear();
        transferDatePicker.setValue(LocalDate.now());
        destinationAccountComboBox.setValue(null);
        transferAssetComboBox.setValue(null);
        transferTotalLabel.setText(
            fmtAsset(Asset.zero(baseCurrency))
        );
        setupTotalSpentReceivedTextLabel();
    }

    private void setupAddTransactionButton() {
        addTransactionButton.setOnMouseClicked(e -> {
            var currentTab = transactionTypeTabPane.getSelectionModel().getSelectedItem();
            if (currentTab == transferTab) {
                handleTransferOrder();
            } else {
                handleBuyOrSellOrder();
            }
        });
    }

    private void handleBuyOrSellOrder() {
        var orderType = orderTypeMapByTab.get(
                transactionTypeTabPane.getSelectionModel().getSelectedItem()
        );
        var selectedAsset = selectedAssetComboBox.getValue();
        var quantityText = quantityTextField.getText();
        var priceText = pricePerAssetTextField.getText();
        var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();

        if (selectedAsset == null || quantityText.isBlank() || priceText.isBlank()) {
            showErrorPopup("Please fill all required fields.");
            return;
        }

        var quantity = new BigDecimal(quantityText);
        var pricePerAsset = Asset.of(baseCurrency, new BigDecimal(priceText));
        var fee = feeTextField.getText().isBlank()
                ? Asset.zero(baseCurrency)
                : Asset.of(baseCurrency, new BigDecimal(feeTextField.getText()));
        var paymentSource = getSelectedPaymentSource();
        var date = datePicker.getValue();
        var notes = notesTextArea.getText();

        OrderResult result = switch (orderType) {
            case BUY -> investmentController.executeBuyOrder(
                    investmentController.getCurrentInvestmentAccount().get(),
                    paymentSource, selectedAsset, quantity, pricePerAsset, fee, date, notes
            );
            case SELL -> investmentController.executeSellOrder(
                    investmentController.getCurrentInvestmentAccount().get(),
                    paymentSource, selectedAsset, quantity, pricePerAsset, fee, date, notes
            );
            default -> throw new IllegalStateException("Unexpected order type: " + orderType);
        };

        handleOrderResult(result);
    }

    private void handleTransferOrder() {
        var destination = destinationAccountComboBox.getValue();
        var asset = transferAssetComboBox.getValue();
        var quantityText = transferQuantityTextField.getText();

        if (destination == null || asset == null || quantityText.isBlank()) {
            showErrorPopup("Please fill all transfer fields.");
            return;
        }

        var result = investmentController.executeTransferOrder(
                investmentController.getCurrentInvestmentAccount().get(),
                destination,
                asset,
                new BigDecimal(quantityText),
                transferDatePicker.getValue(),
                transferNotesTextArea.getText()
        );

        handleOrderResult(result);
    }

    private void handleOrderResult(OrderResult result) {
        switch (result) {
            case OrderResult.InsufficientFunds f ->
                    showErrorPopup("Insufficient funds. Required: "
                            + fmtAsset(f.required()) + ", available: " + fmtAsset(f.available()));
            case OrderResult.InsufficientAssets a ->
                    showErrorPopup("Insufficient assets. Required: "
                            + a.requested() + ", available: " + a.available());
            default -> {
                if (result.isSuccess()) {
                    showInfoPopup("Transaction executed successfully!");
                    // chiudo il dialog
                    addTransactionButton.getScene().getWindow().hide();
                }
            }
        }
    }

    private void showErrorPopup(final String message) {
        ToastNotification.showError(
                addTransactionButton.getScene().getWindow(),
                message
        );
    }

    private void showInfoPopup(final String message) {
        ToastNotification.showSuccess(
                addTransactionButton.getScene().getWindow(),
                message
        );
    }

    private void setupTotalSpentValueLabel() {
        var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        totalSpentValueLabel.setText(fmtAsset(Asset.zero(baseCurrency)));
    }

    private void setupPaymentSourceComboBox() {
        paymentSourceComboBox.setCellFactory(lc -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty ||  item == null) {
                    setText(null);
                } else {
                    if (cashAccountToggleButton.isSelected()) {
                        var account = (CashAccount) item;
                        setText(account.getName());
                    } else if (stableCoinToggleButton.isSelected()) {
                        var stable = (CurrencyUnit) item;
                        setText(fmtComboBoxItemString(stable));
                    } else if (noPaymentToggleButton.isSelected()) {
                        setText("No Payment");
                    } else {
                        setText(null);
                    }
                }
            }
        });
        // asset selezionato
        paymentSourceComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                }
                else {
                    if (cashAccountToggleButton.isSelected()) {
                        var account = (CashAccount) item;
                        setText(account.getName());
                    } else if (stableCoinToggleButton.isSelected()) {
                        var stable = (CurrencyUnit) item;
                        setText(fmtComboBoxItemString(stable));
                    } else if (noPaymentToggleButton.isSelected()) {
                        setText("No Payment");
                    }
                }

            }
        });
        paymentSourceComboBox.setOnAction(event -> {
            // TODO: al momento vuoto...
        });
        paymentSourceComboBox.valueProperty()
                .addListener((obs, old, newVal) -> {
                    updateTotalSpent();
                });
    }

    private void setupPaymentSourceToggleButtons() {
        ToggleGroup paymentSourceToggleGroup = new ToggleGroup();
        cashAccountToggleButton.setToggleGroup(paymentSourceToggleGroup);
        stableCoinToggleButton.setToggleGroup(paymentSourceToggleGroup);
        noPaymentToggleButton.setToggleGroup(paymentSourceToggleGroup);
        cashAccountToggleButton.setSelected(true);
        paymentSourceToggleGroup.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updatePaymentSourceToggleGroup(newValue);
                    updateTotalSpent();
                });
        setupPaymentSourceComboBox();
        updatePaymentSourceToggleGroup(cashAccountToggleButton);
    }

    private void updatePaymentSourceToggleGroup(Toggle selected) {
        if (selected == cashAccountToggleButton) {
            paymentSourceComboBox.setDisable(false);
            paymentSourceComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getAvailableCashAccounts()
                    )
            );
        } else if (selected == stableCoinToggleButton) {
            paymentSourceComboBox.setDisable(false);
            paymentSourceComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getOwnedStableCoins()
                    )
            );
        } else if (selected == noPaymentToggleButton) {
            paymentSourceComboBox.setDisable(true);
            paymentSourceComboBox.setValue(null);
        }
    }

    private PaymentSource getSelectedPaymentSource() {
        if (cashAccountToggleButton.isSelected()) {
            var account = (CashAccount) paymentSourceComboBox.getValue();
            if (account == null) return new PaymentSource.NoPaymentChannel();
            return new PaymentSource.CashAccountChannel(account);
        } else if (stableCoinToggleButton.isSelected()) {
            var stable = (CurrencyUnit) paymentSourceComboBox.getValue();
            return new PaymentSource.StableCoinPositionChannel(
                    investmentController.getCurrentInvestmentAccount().get(),
                    stable
            );
        } else {
            return new PaymentSource.NoPaymentChannel();
        }
    }

    private void setupNotesTextArea() {
        notesTextArea.clear();
        // al momento solo pulizia
        notesTextArea.setPromptText("Notes ...");
    }

    private void setupFeeTextField() {
        feeTextField.clear();
        feeTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*\\.?\\d*")) {     // [0..9]+.?[0..9]+
                        feeTextField.setText(oldValue);
                    } else {
                        updateTotalSpent();
                    }
                });
        var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        feeTextField.setPromptText("fee: " + fmtAsset(Asset.zero(baseCurrency)));
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
    }

    private void setupPricePerAssetTextField() {
        pricePerAssetTextField.clear();
        pricePerAssetTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalSpent();
                }
        );
        var baseCurrency = investmentController.getCurrentInvestmentAccount()
                .get()
                .getBaseCurrency();
        pricePerAssetTextField.setPromptText(fmtAsset(Asset.zero(baseCurrency)));
    }

    private void setupMaxQuantityButton(
            final ToggleButton btn,
            final ComboBox<CurrencyUnit> comboBox,
            final TextField qtyTextField) {
        btn.selectedProperty().addListener(
                (observable, wasSelected, isSelected) -> {
                    if (isSelected) {
                        var asset = comboBox.getValue();
                        if (asset == null) {
                            btn.setSelected(false);
                            return;
                        }
                        investmentController.getPositions().stream()
                                .filter(p -> p.asset().equals(asset))
                                .findFirst()
                                .ifPresentOrElse(
                                        p -> {
                                            qtyTextField.setText(
                                                    p.quantity().stripTrailingZeros().toPlainString()
                                            );
                                            qtyTextField.setDisable(true);
                                        },
                                        () -> btn.setSelected(false)
                                );
                    } else {
                        qtyTextField.setDisable(false);
                        qtyTextField.clear();
                    }
                }
        );
    }

    private void setupQuantityTextField() {
        quantityTextField.clear();
        quantityTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*\\.?\\d*"))      // [0..9]+.?[0..9]+
                        quantityTextField.setText(oldValue);
                    else
                        updateTotalSpent();
                });
        quantityTextField.setPromptText("0.00");

        setupMaxQuantityButton(maxQuantityToggleButton, selectedAssetComboBox, quantityTextField);
        // all initialize cmq si apre il tab BUY a cui non serve il btn
        maxQuantityToggleButton.setVisible(false);
        maxQuantityToggleButton.setManaged(false);
        selectedAssetComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    maxQuantityToggleButton.setSelected(false);
                }
        );
    }

    private String fmtAsset(final Asset asset) {
        return asset.currency().getSymbol() + " " + asset.amount().setScale(
                asset.currency().getDisplayDecimals(), RoundingMode.HALF_UP
        ).toPlainString();
    }

    private void updateTotalSpent() {
        try {
            // prendo tutti i dati scelti dall utente e setto il Label di totale da spendere
            var currentTabSelected = transactionTypeTabPane.getSelectionModel().getSelectedItem();
            var selectedAsset = selectedAssetComboBox.getValue();
            var qText = quantityTextField.getText();
            var priceTxt = pricePerAssetTextField.getText();
            var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
            if (qText.isBlank() || priceTxt.isBlank()) {
                totalSpentValueLabel.setText(fmtAsset(Asset.zero(baseCurrency)));
                return;
            }
            var quantity = new BigDecimal(qText);
            var pricePerAsset = Asset.of(
                    baseCurrency,
                    new BigDecimal(priceTxt)
            );
            var fee = feeTextField.getText().isBlank() ?
                    Asset.zero(baseCurrency) :
                    Asset.of(
                            baseCurrency,
                            new BigDecimal(feeTextField.getText())
                    );
            var paymentSource = getSelectedPaymentSource();
            var totalSpent = investmentController.estimateOrderCost(
                    orderTypeMapByTab.get(currentTabSelected),
                    selectedAsset,
                    quantity,
                    pricePerAsset,
                    fee,
                    paymentSource
            );
            totalSpentValueLabel.setText(fmtAsset(totalSpent));
        } catch (NumberFormatException e) {
            totalSpentValueLabel.setText("Invalid input");
        }
    }

    private String fmtComboBoxItemString(final CurrencyUnit currencyUnit) {
        return currencyUnit.getFullName() + " " + currencyUnit.getShortName();
    }

    // TODO pensare se per caso farlo piu generico e passare come argomento la lista di quello da visualizzare
    private void setupBuyAssetComboBox() {
        selectedAssetComboBox.setPromptText("Select asset to buy ...");
        selectedAssetComboBox.setItems(
                FXCollections.observableArrayList(
                    investmentController.getAllTradeableAssets()
                )
        );
        selectedAssetComboBox.setCellFactory(lc -> new ListCell<>() {
            @Override
            protected void updateItem(CurrencyUnit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty ||  item == null) {
                    setText(null);
                } else {
                    setText(fmtComboBoxItemString(item));
                    // per ora solo testo, sarebbe cool anche qualche immagine... con l ImageView
                }
            }
        });
        // asset selezionato
        selectedAssetComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CurrencyUnit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmtComboBoxItemString(item));
            }
        });
        selectedAssetComboBox.setOnAction(event -> {
            var selected = selectedAssetComboBox.getValue();
            if (selected != null) {
                var price = investmentController.getCurrentMarketPrice(selected);
                pricePerAssetTextField.setText(
                        price.amount()
                                .setScale(selected.getDisplayDecimals(), RoundingMode.HALF_UP)
                                .toPlainString()
                );
                updateTotalSpent();
            }
        });
        selectedAssetComboBox.valueProperty().addListener(
                (obs, old, newVal) -> {
                    updateTotalSpent();
                });
        // TODO: setto di default il primo che c e' nella lista, controllare se corretto
        selectedAssetComboBox.setValue(
                investmentController.getAllTradeableAssets().getFirst()
        );
    }

    private void setupTransactionTypeTabPane() {
        transactionTypeTabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldTab, newTab) -> {
                    onTabChanged(newTab);
                });
    }

    private void onTabChanged(Tab tab) {
        boolean isTransfer = tab == transferTab;
        buySellFieldsPanel.setVisible(!isTransfer);
        buySellFieldsPanel.setManaged(!isTransfer);
        transferFieldsPanel.setVisible(isTransfer);
        transferFieldsPanel.setManaged(isTransfer);

        maxQuantityToggleButton.setVisible(tab == sellTab);
        maxQuantityToggleButton.setManaged(tab == sellTab);

        resetForm(tab);

        if (isTransfer) {
            var allAvailableAccounts = investmentController.getAllInvestmentAccounts();
            var currentAccount = investmentController.getCurrentInvestmentAccount().get();
            destinationAccountComboBox.setItems(
                    FXCollections.observableArrayList(
                            allAvailableAccounts.stream()
                                    .filter(a -> !a.getId().equals(currentAccount.getId()))
                                    .toList()
                    )
            );
            transferAssetComboBox.setItems(
                    FXCollections.observableArrayList(investmentController.getAllOwnedAssets())
            );
        }
    }

    private void setupTransferFields() {
        // destination wallet combobox
        destinationAccountComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(InvestmentAccount item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        destinationAccountComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(InvestmentAccount item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // asset combobox — stessa cellFactory di selectedAssetComboBox
        transferAssetComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CurrencyUnit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmtComboBoxItemString(item));
            }
        });
        transferAssetComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CurrencyUnit item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmtComboBoxItemString(item));
            }
        });

        // quantity — stesso validator numerico di quantityTextField
        transferQuantityTextField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*"))
                transferQuantityTextField.setText(old);
        });

        // date — stesso del buy/sell
        transferDatePicker.setValue(LocalDate.now());

        // notes — solo clear
        transferNotesTextArea.clear();
        transferNotesTextArea.setPromptText("Notes...");
        setupMaxQuantityButton(transferMaxQuantityToggleButton, transferAssetComboBox, transferQuantityTextField);
    }

    @FXML
    private void handleCloseDialog() {
        Stage stage = (Stage) addTransactionButton.getScene().getWindow();
        stage.close();
    }
}
