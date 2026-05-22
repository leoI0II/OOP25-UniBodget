package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.wallet.CashAccount;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public class AddTransactionDialogViewController {

    private final InvestmentController investmentController;

    // contenitori per tab BUY
    @FXML private TabPane transactionTypeTabPane;
    @FXML private Tab buyTab;
    @FXML private Tab sellTab;
    @FXML private Tab transferTab;
    private Map<Tab, OrderType> orderTypeMapByTab;      // no puo essere final a causa che gli tab sono caricati dopo
    @FXML private ComboBox<CurrencyUnit> selectedAssetComboBox;
    @FXML private TextField quantityTextField;
    @FXML private TextField pricePerAssetTextField;
    @FXML private DatePicker datePicker;
    @FXML private TextField feeTextField;
    @FXML private TextArea notesTextArea;
    @FXML private ToggleButton cashAccountToggleButton;
    @FXML private ToggleButton stableCoinToggleButton;
    @FXML private ToggleButton noPaymentToggleButton;
    @FXML private ComboBox<Object> paymentSourceComboBox;
    @FXML private Label totalSpentValueLabel;
    @FXML private Button addTransactionButton;

    public AddTransactionDialogViewController(InvestmentController investmentController) {
        this.investmentController = Objects.requireNonNull(investmentController);
    }

    public void initialize() {
        orderTypeMapByTab = Map.of(
                buyTab, OrderType.BUY,
                sellTab, OrderType.SELL,
                transferTab, OrderType.TRANSFER
        );

        setupBuyAssetComboBox();
        setupQuantityTextField();
        setupPricePerAssetTextField();
        setupDatePicker();
        setupFeeTextField();
        setupNotesTextArea();
        setupTransactionTypeTabPane();
        setupPaymentSourceToggleButtons();
        setupTotalSpentValueLabel();
        setupAddTransactionButton();
    }

    private void resetForm(Tab tab) {
        setupBuyAssetComboBox();
        setupQuantityTextField();
        setupPricePerAssetTextField();
        setupDatePicker();
        setupFeeTextField();
        setupNotesTextArea();
        setupTransactionTypeTabPane();
        setupPaymentSourceToggleButtons();
        setupTotalSpentValueLabel();

        if (tab == sellTab || tab == transferTab) {
            selectedAssetComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getAllOwnedAssets()
                    )
            );
        } else {
            selectedAssetComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getAllTradeableAssets()
                    )
            );
        }
        selectedAssetComboBox.setValue(
                selectedAssetComboBox.getItems().stream()
                        .findFirst()
                        .orElse(null)
        );
    }

    private void setupAddTransactionButton() {
        addTransactionButton.setOnMouseClicked(e -> {
            OrderType orderType = orderTypeMapByTab.get(transactionTypeTabPane.getSelectionModel().getSelectedItem());
            var selectedAsset = selectedAssetComboBox.getValue();
            var quantityText = quantityTextField.getText();
            var pricePerAssetText = pricePerAssetTextField.getText();
            if (quantityText.isBlank() || pricePerAssetText.isBlank()) {
                // stampa popup rosso con errore e richiesta di settare quantity o price per asset
                return;
            }
            var quantity = new BigDecimal(quantityText);
            var pricePerAsset = Asset.of(
                    selectedAsset,
                    new BigDecimal(pricePerAssetText)
            );
            LocalDate date = datePicker.getValue();
            var fee = feeTextField.getText().isBlank() ?
                    Asset.zero(selectedAsset) :
                    Asset.of(selectedAsset, new BigDecimal(feeTextField.getText()));
            var notes = notesTextArea.getText();
            var paymentSource = getSelectedPaymentSource();

            OrderResult result = switch(orderType) {
                case OrderType.BUY -> investmentController.executeBuyOrder(
                        investmentController.getCurrentInvestmentAccount().get(),
                        paymentSource,
                        selectedAsset,
                        quantity,
                        pricePerAsset,
                        fee,
                        date,
                        notes
                );
                case OrderType.SELL -> investmentController.executeSellOrder(
                        investmentController.getCurrentInvestmentAccount().get(),
                        paymentSource,
                        selectedAsset,
                        quantity,
                        pricePerAsset,
                        fee,
                        date,
                        notes
                );
                case OrderType.TRANSFER -> investmentController.executeTransferOrder(
                        investmentController.getCurrentInvestmentAccount().get(),
                        null,
                        selectedAsset,
                        quantity,
                        date,
                        notes
                );
            };
        });
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
        feeTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalSpent();
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

    private void setupQuantityTextField() {
        quantityTextField.clear();
        quantityTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*\\.?\\d*"))      // [0..9]+.?[0..9]+
                        quantityTextField.setText(oldValue);
                });
        quantityTextField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateTotalSpent();
                }
        );
        quantityTextField.setPromptText("0.00");
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
                    selectedAsset,
                    new BigDecimal(priceTxt)
            );
            var fee = feeTextField.getText().isBlank() ?
                    Asset.zero(selectedAsset) :
                    Asset.of(
                            selectedAsset,
                            new BigDecimal(feeTextField.getText())
                    );
            var totalSpent = investmentController.estimateOrderCost(
                    orderTypeMapByTab.get(currentTabSelected),
                    selectedAsset,
                    quantity,
                    pricePerAsset,
                    fee,
                    getSelectedPaymentSource()
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
        resetForm(tab);
    }

    @FXML
    private void handleCloseDialog() {

    }
}
