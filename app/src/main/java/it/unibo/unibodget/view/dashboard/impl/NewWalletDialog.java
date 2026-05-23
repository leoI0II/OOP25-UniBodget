package it.unibo.unibodget.view.dashboard.impl;

import java.util.Objects;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.wallet.CashAccount;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public final class NewWalletDialog extends Dialog<CashAccount> {

    public NewWalletDialog(final FiatCurrency defaultCurrency) {
        setTitle("Create New Wallet");
        setHeaderText("Insert wallet details");

        final ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        final TextField walletNameField = new TextField();
        walletNameField.setPromptText("Wallet name");

        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>(
                FXCollections.observableArrayList(FiatCurrency.values())
        );
        currencyBox.setMaxWidth(Double.MAX_VALUE);
        currencyBox.getSelectionModel().select(defaultCurrency);

        currencyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final FiatCurrency currency) {
                return currency == null ? "" : formatCurrency(currency);
            }

            @Override
            public FiatCurrency fromString(final String string) {
                return null;
            }
        });

        currencyBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final FiatCurrency item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        currencyBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final FiatCurrency item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Wallet name:"), 0, 0);
        grid.add(walletNameField, 1, 0);
        grid.add(new Label("Currency:"), 0, 1);
        grid.add(currencyBox, 1, 1);

        getDialogPane().setContent(grid);

        final javafx.scene.Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        walletNameField.textProperty().addListener((obs, oldValue, newValue) -> {
            createButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, createButtonType)) {
                return null;
            }

            final String walletName = walletNameField.getText().trim();
            final FiatCurrency selectedCurrency = currencyBox.getValue();

            if (walletName.isEmpty() || selectedCurrency == null) {
                return null;
            }

            return new CashAccount(walletName, selectedCurrency);
        });
    }

    private static String formatCurrency(final FiatCurrency currency) {
        return currency.getSymbol() + " " + currency.getShortName();
    }
}