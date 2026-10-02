package it.unibo.unibodget.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import it.unibo.unibodget.model.categories.Category;
import it.unibo.unibodget.model.categories.CategoryOrigin;
import it.unibo.unibodget.model.categories.CategoryType;
import it.unibo.unibodget.model.currency.Asset;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.transactions.base.CashTransaction;
import it.unibo.unibodget.model.wallet.CashAccount;

final class FirstBootDialogs {
    record WalletInput(String name, CurrencyUnit currency) {}
    record CategoryInput(String name, CategoryType type) {}
    record TransactionInput(String description, BigDecimal amount) {}

    static Optional<WalletInput> askWallet() {
        final Dialog<WalletInput> dialog = new Dialog<>();
        dialog.setTitle("Create first wallet");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final TextField name = new TextField("Main wallet");
        final TextField currency = new TextField("EUR");
        final GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Wallet name"), name);
        grid.addRow(1, new Label("Currency code"), currency);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new WalletInput(name.getText().trim(), CurrencyUnit.getByCode(currency.getText().trim().toUpperCase())) : null);
        return dialog.showAndWait().filter(v -> v.currency() != null);
    }

    static Optional<CategoryInput> askCategory() {
        final Dialog<CategoryInput> dialog = new Dialog<>();
        dialog.setTitle("Create category");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final TextField name = new TextField("Custom category");
        final TextField type = new TextField("EXPENSE");
        final GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Name"), name);
        grid.addRow(1, new Label("Type"), type);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new CategoryInput(name.getText().trim(), CategoryType.valueOf(type.getText().trim().toUpperCase())) : null);
        return dialog.showAndWait();
    }

    static Optional<TransactionInput> askTransaction() {
        final Dialog<TransactionInput> dialog = new Dialog<>();
        dialog.setTitle("Create transaction");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final TextField description = new TextField("Sample transaction");
        final TextField amount = new TextField("10.00");
        final GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Description"), description);
        grid.addRow(1, new Label("Amount"), amount);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new TransactionInput(description.getText().trim(), new BigDecimal(amount.getText().trim())) : null);
        return dialog.showAndWait();
    }

    static CashAccount walletFrom(final WalletInput input) {
        return new CashAccount(input.name(), input.currency());
    }

    static Category customCategory(final CategoryInput input) {
        return new Category(input.name(), new it.unibo.unibodget.model.utils.ARGBColor(0xFF607D8B), input.type(), CategoryOrigin.CUSTOM, true);
    }

    static CashTransaction sampleTransaction(final CurrencyUnit currency, final String description, final BigDecimal amount) {
        return new CashTransaction(Asset.of(currency, amount), Category.FOOD, LocalDate.now(), description, null);
    }
}
