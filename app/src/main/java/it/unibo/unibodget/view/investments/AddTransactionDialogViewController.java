package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.investment.OrderResult;
import it.unibo.unibodget.model.investment.OrderType;
import it.unibo.unibodget.model.investment.PaymentSource;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.model.utils.event.OrderResultEvent;
import it.unibo.unibodget.model.wallet.CashAccount;
import it.unibo.unibodget.model.wallet.InvestmentAccount;
import it.unibo.unibodget.view.main.BaseViewController;
import it.unibo.unibodget.view.utils.AssetFormatter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for the dialog window that allows the user to add a new investment transaction
 * (e.g., buy, sell, or transfer assets).
 */
public final class AddTransactionDialogViewController extends BaseViewController {

    private static final String INPUT_VALUE_REGEX = "\\d*\\.?\\d*";     // [0..9]+.?[0..9]+
    private final InvestmentController investmentController;

    // contenitori per tab BUY
    @FXML
    private TabPane transactionTypeTabPane;
    @FXML
    private Tab buyTab;
    @FXML
    private Tab sellTab;
    @FXML
    private Tab transferTab;
    private Map<Tab, OrderType> orderTypeMapByTab;      // no puo essere final a causa che gli tab sono caricati dopo
    @FXML
    private ComboBox<CurrencyUnit> selectedAssetComboBox;
    @FXML
    private TextField quantityTextField;
    @FXML
    private ToggleButton maxQuantityToggleButton;     // solo per SELL
    @FXML
    private TextField pricePerAssetTextField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField feeTextField;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private ToggleButton cashAccountToggleButton;
    @FXML
    private ToggleButton stableCoinToggleButton;
    @FXML
    private ToggleButton noPaymentToggleButton;
    @FXML
    private ComboBox<Object> paymentSourceComboBox;
    @FXML
    private Label totalSpentReceivedTextLabel;
    @FXML
    private Label totalSpentValueLabel;
    @FXML
    private Button addTransactionButton;

    @FXML
    private VBox buySellFieldsPanel;
    // transfer specific fields
    @FXML
    private VBox transferFieldsPanel;
    @FXML
    private ComboBox<InvestmentAccount> destinationAccountComboBox;
    @FXML
    private ComboBox<CurrencyUnit> transferAssetComboBox;
    @FXML
    private TextField transferQuantityTextField;
    @FXML
    private ToggleButton transferMaxQuantityToggleButton;
    @FXML
    private DatePicker transferDatePicker;
    @FXML
    private TextArea transferNotesTextArea;
    @FXML
    private Label transferTotalLabel;

    /**
     * Constructs a new {@code AddTransactionDialogViewController}.
     *
     * @param investmentController the controller handling investment-related business logic
     */
    public AddTransactionDialogViewController(final InvestmentController investmentController) {
        this.investmentController = Objects.requireNonNull(investmentController);
    }

    /**
     * Initializes the controller, configuring all UI components, setting up bindings,
     * and establishing initial state after FXML injection.
     */
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
        final var currentTab = transactionTypeTabPane.getSelectionModel().getSelectedItem();
        final OrderType orderType = orderTypeMapByTab.get(currentTab);
        totalSpentReceivedTextLabel.setText(switch (orderType) {
            case BUY -> "Total Spent";
            case SELL -> "Total Received";
            case TRANSFER -> "Total Transferred";
        });
    }

    private void resetForm(final Tab tab) {
        // buy/sell fields
        quantityTextField.clear();
        pricePerAssetTextField.clear();
        feeTextField.clear();
        notesTextArea.clear();
        datePicker.setValue(LocalDate.now());
        final var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        totalSpentValueLabel.setText(AssetFormatter.ofAsset(
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
                AssetFormatter.ofAsset(Asset.zero(baseCurrency))
        );
        setupTotalSpentReceivedTextLabel();
    }

    private void setupAddTransactionButton() {
        addTransactionButton.setOnMouseClicked(e -> {
            final var currentTab = transactionTypeTabPane.getSelectionModel().getSelectedItem();
            if (currentTab == transferTab) {
                handleTransferOrder();
            } else {
                handleBuyOrSellOrder();
            }
        });
    }

    private void handleBuyOrSellOrder() {
        final var orderType = orderTypeMapByTab.get(
                transactionTypeTabPane.getSelectionModel().getSelectedItem()
        );
        final var selectedAsset = selectedAssetComboBox.getValue();
        final var quantityText = quantityTextField.getText();
        final var priceText = pricePerAssetTextField.getText();
        final var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();

        if (selectedAsset == null || quantityText.isBlank() || priceText.isBlank()) {
            MessageBus.send(new MainErrorNotificationEvent("Please fill all required fields."));
            return;
        }

        final var quantity = new BigDecimal(quantityText);
        final var pricePerAsset = Asset.of(baseCurrency, new BigDecimal(priceText));
        final var fee = feeTextField.getText().isBlank()
                ? Asset.zero(baseCurrency)
                : Asset.of(baseCurrency, new BigDecimal(feeTextField.getText()));
        final var paymentSource = getSelectedPaymentSource();
        final var date = datePicker.getValue();
        final var notes = notesTextArea.getText();

        final OrderResult result = switch (orderType) {
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
        final var destination = destinationAccountComboBox.getValue();
        final var asset = transferAssetComboBox.getValue();
        final var quantityText = transferQuantityTextField.getText();

        if (destination == null || asset == null || quantityText.isBlank()) {
            MessageBus.send(new MainErrorNotificationEvent("Please fill all transfer fields."));
            return;
        }

        final var result = investmentController.executeTransferOrder(
                investmentController.getCurrentInvestmentAccount().get(),
                destination,
                asset,
                new BigDecimal(quantityText),
                transferDatePicker.getValue(),
                transferNotesTextArea.getText()
        );
        handleOrderResult(result);
    }

    private void handleOrderResult(final OrderResult result) {
        MessageBus.send(new OrderResultEvent(result));
    }

    private void setupTotalSpentValueLabel() {
        final var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        totalSpentValueLabel.setText(AssetFormatter.ofAsset(Asset.zero(baseCurrency)));
    }

    private void setupPaymentSourceComboBox() {
        paymentSourceComboBox.setCellFactory(lc -> new ListCell<Object>() {
            @Override
            protected void updateItem(final Object item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                // pattern matching invece di cast basato su toggle
                switch (item) {
                    case CashAccount account -> setText(account.getName());
                    case CurrencyUnit stable -> setText(fmtComboBoxItemString(stable));
                    default -> setText(item.toString());
                }
            }
        });
        // asset selezionato
        paymentSourceComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(final Object item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                // pattern matching invece di cast basato su toggle
                switch (item) {
                    case CashAccount account -> setText(account.getName());
                    case CurrencyUnit stable -> setText(fmtComboBoxItemString(stable));
                    default -> setText(item.toString());
                }
            }
        });
        paymentSourceComboBox.valueProperty()
                .addListener((obs, old, newVal) -> {
                    updateTotalSpent();
                });
    }

    private void setupPaymentSourceToggleButtons() {
        final ToggleGroup paymentSourceToggleGroup = new ToggleGroup();

        cashAccountToggleButton.setToggleGroup(paymentSourceToggleGroup);
        final boolean noAvailableCashAccounts = investmentController.getAvailableCashAccounts().isEmpty();
        cashAccountToggleButton.setDisable(noAvailableCashAccounts);

        // di default voglio gli stable, se ci sono
        stableCoinToggleButton.setToggleGroup(paymentSourceToggleGroup);
        final boolean noStablesInOwn = investmentController.getOwnedStableCoins().isEmpty();
        stableCoinToggleButton.setDisable(noStablesInOwn);
        stableCoinToggleButton.setSelected(!noStablesInOwn);

        // se non ci sono stable, allora uso no payment
        noPaymentToggleButton.setToggleGroup(paymentSourceToggleGroup);
        noPaymentToggleButton.setSelected(noStablesInOwn);

        paymentSourceToggleGroup.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updatePaymentSourceToggleGroup(newValue);
                    updateTotalSpent();
                });
        final var defaultToggle = noStablesInOwn ? noPaymentToggleButton : stableCoinToggleButton;
        updatePaymentSourceToggleGroup(defaultToggle);
    }

    private void updatePaymentSourceToggleGroup(final Toggle selected) {
        paymentSourceComboBox.setValue(null);
        if (selected == cashAccountToggleButton) {
            paymentSourceComboBox.setDisable(false);
            paymentSourceComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getAvailableCashAccounts()
                    )
            );
            if (!investmentController.getAvailableCashAccounts().isEmpty()) {
                paymentSourceComboBox.setValue(investmentController.getAvailableCashAccounts().getFirst());
            }
        } else if (selected == stableCoinToggleButton) {
            paymentSourceComboBox.setDisable(false);
            paymentSourceComboBox.setItems(
                    FXCollections.observableArrayList(
                            investmentController.getOwnedStableCoins()
                    )
            );
            if (!investmentController.getOwnedStableCoins().isEmpty()) {
                paymentSourceComboBox.setValue(investmentController.getOwnedStableCoins().getFirst());
            }
        } else if (selected == noPaymentToggleButton) {
            paymentSourceComboBox.setDisable(true);
            paymentSourceComboBox.setValue(null);

        }
    }

    private PaymentSource getSelectedPaymentSource() {
        final var value = paymentSourceComboBox.getValue();

        return switch (value) {
            case CashAccount account -> new PaymentSource.CashAccountChannel(account);
            case CurrencyUnit stable -> new PaymentSource.StableCoinPositionChannel(
                    investmentController.getCurrentInvestmentAccount().get(),
                    stable
            );
            case null, default -> new PaymentSource.NoPaymentChannel();
        };
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
                    if (!newValue.matches(INPUT_VALUE_REGEX)) {     // [0..9]+.?[0..9]+
                        feeTextField.setText(oldValue);
                    } else {
                        updateTotalSpent();
                    }
                });
        final var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
        feeTextField.setPromptText("fee: " + AssetFormatter.ofAsset(Asset.zero(baseCurrency)));
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
        final var baseCurrency = investmentController.getCurrentInvestmentAccount()
                .get()
                .getBaseCurrency();
        pricePerAssetTextField.setPromptText(AssetFormatter.ofAsset(Asset.zero(baseCurrency)));
    }

    private void setupMaxQuantityButton(
            final ToggleButton btn,
            final ComboBox<CurrencyUnit> comboBox,
            final TextField qtyTextField) {
        btn.selectedProperty().addListener(
                (observable, wasSelected, isSelected) -> {
                    if (isSelected) {
                        final var asset = comboBox.getValue();
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
                    if (!newValue.matches(INPUT_VALUE_REGEX)) {      // [0..9]+.?[0..9]+
                        quantityTextField.setText(oldValue);
                    } else {
                        updateTotalSpent();
                    }
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

    private void updateTotalSpent() {
        try {
            // prendo tutti i dati scelti dall utente e setto il Label di totale da spendere
            final var currentTabSelected = transactionTypeTabPane.getSelectionModel().getSelectedItem();
            final var selectedAsset = selectedAssetComboBox.getValue();
            final var qText = quantityTextField.getText();
            final var priceTxt = pricePerAssetTextField.getText();
            final var baseCurrency = investmentController.getCurrentInvestmentAccount().get().getBaseCurrency();
            if (qText.isBlank() || priceTxt.isBlank()) {
                totalSpentValueLabel.setText(AssetFormatter.ofAsset(Asset.zero(baseCurrency)));
                return;
            }
            final var quantity = new BigDecimal(qText);
            final var pricePerAsset = Asset.of(
                    baseCurrency,
                    new BigDecimal(priceTxt)
            );
            final var fee = feeTextField.getText().isBlank()
                    ? Asset.zero(baseCurrency)
                    : Asset.of(
                            baseCurrency,
                            new BigDecimal(feeTextField.getText())
                    );
            final var paymentSource = getSelectedPaymentSource();
            final var totalSpent = investmentController.estimateOrderCost(
                    orderTypeMapByTab.get(currentTabSelected),
                    selectedAsset,
                    quantity,
                    pricePerAsset,
                    fee,
                    paymentSource
            );
            totalSpentValueLabel.setText(AssetFormatter.ofAsset(totalSpent));
        } catch (final NumberFormatException e) {
            totalSpentValueLabel.setText("Invalid input");
        }
    }

    private String fmtComboBoxItemString(final CurrencyUnit currencyUnit) {
        return currencyUnit.getFullName() + " " + currencyUnit.getShortName();
    }

    private void setupBuyAssetComboBox() {
        selectedAssetComboBox.setPromptText("Select asset to buy ...");
        selectedAssetComboBox.setItems(
                FXCollections.observableArrayList(
                        investmentController.getAllTradeableAssets()
                )
        );
        selectedAssetComboBox.setCellFactory(lc -> new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
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
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmtComboBoxItemString(item));
            }
        });
        selectedAssetComboBox.setOnAction(event -> {
            final var selected = selectedAssetComboBox.getValue();
            if (selected != null) {
                final var price = investmentController.getCurrentMarketPrice(selected);
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

    private void onTabChanged(final Tab tab) {
        final boolean isTransfer = tab == transferTab;
        buySellFieldsPanel.setVisible(!isTransfer);
        buySellFieldsPanel.setManaged(!isTransfer);
        transferFieldsPanel.setVisible(isTransfer);
        transferFieldsPanel.setManaged(isTransfer);

        maxQuantityToggleButton.setVisible(tab == sellTab);
        maxQuantityToggleButton.setManaged(tab == sellTab);

        resetForm(tab);

        if (isTransfer) {
            final var allAvailableAccounts = investmentController.getAllInvestmentAccounts();
            final var currentAccount = investmentController.getCurrentInvestmentAccount().get();
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
            protected void updateItem(final InvestmentAccount item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        destinationAccountComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final InvestmentAccount item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        // asset combobox — stessa cellFactory di selectedAssetComboBox
        transferAssetComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmtComboBoxItemString(item));
            }
        });
        transferAssetComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : fmtComboBoxItemString(item));
            }
        });

        // quantity — stesso validator numerico di quantityTextField
        transferQuantityTextField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches(INPUT_VALUE_REGEX)) {
                transferQuantityTextField.setText(old);
            }
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
        final Stage stage = (Stage) addTransactionButton.getScene().getWindow();
        stage.close();
    }
}
