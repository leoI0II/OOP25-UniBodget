package it.unibo.unibodget.view.dashboard.impl;

import java.util.Objects;

import it.unibo.unibodget.model.currency.FiatCurrency;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public final class BaseCurrencyDialog extends Dialog<FiatCurrency> {

    public BaseCurrencyDialog(final FiatCurrency currentCurrency) {
        setTitle("Base Currency");
        setHeaderText("Select the currency used for aggregated totals");

        final ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>(
                FXCollections.observableArrayList(FiatCurrency.values())
        );
        currencyBox.setMaxWidth(Double.MAX_VALUE);
        currencyBox.getSelectionModel().select(currentCurrency);

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
        grid.add(new Label("Base currency:"), 0, 0);
        grid.add(currencyBox, 1, 0);

        getDialogPane().setContent(grid);

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, saveButtonType)) {
                return null;
            }
            return currencyBox.getValue();
        });
    }

    private static String formatCurrency(final FiatCurrency currency) {
        return currency.getSymbol() + " " + currency.getShortName();
    }
}