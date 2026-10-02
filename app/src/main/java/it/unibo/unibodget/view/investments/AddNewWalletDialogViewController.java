package it.unibo.unibodget.view.investments;

import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.investment.controllers.InvestmentController;
import it.unibo.unibodget.model.utils.MessageBus;
import it.unibo.unibodget.model.utils.event.CreateNewInvestmentWalletRequestedEvent;
import it.unibo.unibodget.model.utils.event.MainErrorNotificationEvent;
import it.unibo.unibodget.view.main.AppContext;
import it.unibo.unibodget.view.main.BaseViewController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddNewWalletDialogViewController extends BaseViewController {

    private final InvestmentController investmentController;
    private AppContext appContext = AppContext.NUM_CONTEXTS;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<CurrencyUnit> currencyComboBox;
    @FXML
    private Button createWalletButton;

    public AddNewWalletDialogViewController(final InvestmentController investmentController) {
        this.investmentController = Objects.requireNonNull(investmentController);
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public void initialize() {
        setupNameTextField();
        setupCurrencyComboBox();
        setupCreateWalletButton();
    }

    private void setupNameTextField() {
        nameTextField.clear();
        nameTextField.setPromptText("Insert the name ...");
    }

    private String fmtComboBoxItemString(final CurrencyUnit currencyUnit) {
        return currencyUnit.getFullName() + " " + currencyUnit.getShortName();
    }

    private void setupCurrencyComboBox() {
        currencyComboBox.setPromptText("Select preferred currency ...");
        final List<CurrencyUnit> currencies = new ArrayList<>(investmentController.getAllTradeableAssets());
        currencies.addAll(List.of(FiatCurrency.values()));
        currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
        currencyComboBox.setCellFactory(lc-> new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(fmtComboBoxItemString(item));
                }
            }
        });
        currencyComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(fmtComboBoxItemString(item));
                }
            }
        });
        currencyComboBox.setValue(currencies.stream()
                .filter(c -> c instanceof CryptoCurrency && ((CryptoCurrency) c).isStableCoin())
                .findFirst()
                .orElse(currencies.getFirst())
        );
    }

    private void setupCreateWalletButton() {
        createWalletButton.setOnAction(event -> {
            final var name = nameTextField.getText();
            if (name.isBlank()) {
                MessageBus.send(new MainErrorNotificationEvent(
                        "The name field must be compiled! The name cannot be empty or blank!"
                ));
                return;
            }
            final var preferredCurrency = currencyComboBox.getValue();
            if (appContext == AppContext.INVESTMENTS) {
                MessageBus.send(new CreateNewInvestmentWalletRequestedEvent(name, preferredCurrency));
            }
        });
    }
}
