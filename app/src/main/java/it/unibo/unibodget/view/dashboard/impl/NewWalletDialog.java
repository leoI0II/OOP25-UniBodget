package it.unibo.unibodget.view.dashboard.impl;

import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.ThemeManager;
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

/**
 * Dialog used to create a new cash account (wallet).
 */
public final class NewWalletDialog extends Dialog<CashAccount> {

    private static final double GRID_GAP = 12.0;
    private static final double PADDING_INSET = 16.0;

    /**
     * Creates a new wallet creation dialog.
     *
     * @param defaultCurrency the default currency pre-selected in the dialog
     */
    public NewWalletDialog(final FiatCurrency defaultCurrency) {
        this(defaultCurrency, List.of());
    }

    /**
     * Creates a new wallet creation dialog with validation against existing wallets.
     *
     * @param defaultCurrency the default currency pre-selected in the dialog
     * @param existingWallets currently existing cash accounts to prevent duplicate (name, currency)
     */
    public NewWalletDialog(
            final FiatCurrency defaultCurrency,
            final List<CashAccount> existingWallets) {
        setTitle("Create New Wallet");
        setHeaderText("Insert wallet details");
        setResizable(true);
        ThemeManager.applyThemeToDialog(this);

        final List<CashAccount> nonNullExisting =
                existingWallets == null ? List.of() : List.copyOf(existingWallets);

        final ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        final TextField walletNameField = new TextField();
        walletNameField.setPromptText("Wallet name");

        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>(
                FXCollections.observableArrayList(FiatCurrency.values())
        );
        currencyBox.setMaxWidth(Double.MAX_VALUE);
        currencyBox.getSelectionModel().select(Objects.requireNonNull(defaultCurrency));

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
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(new Insets(PADDING_INSET));

        grid.add(new Label("Wallet name:"), 0, 0);
        grid.add(walletNameField, 1, 0);
        grid.add(new Label("Currency:"), 0, 1);
        grid.add(currencyBox, 1, 1);

        getDialogPane().setContent(grid);

        final javafx.scene.Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        final Runnable validator = () -> {
            final String raw = walletNameField.getText();
            final String name = raw == null ? "" : raw.trim();
            final boolean blank = name.isEmpty();
            final FiatCurrency currency = currencyBox.getValue();
            final boolean duplicate = !blank && nonNullExisting.stream().anyMatch(w ->
                    w.getName().trim().equalsIgnoreCase(name)
                            && Objects.equals(w.getBaseCurrency(), currency));
            createButton.setDisable(blank || currency == null || duplicate);
        };

        walletNameField.textProperty().addListener((obs, oldValue, newValue) -> validator.run());
        currencyBox.valueProperty().addListener((obs, oldValue, newValue) -> validator.run());

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, createButtonType)) {
                return null;
            }

            final String raw = walletNameField.getText();
            if (raw == null || raw.isBlank()) {
                return null;
            }
            final FiatCurrency selectedCurrency = currencyBox.getValue();
            if (selectedCurrency == null) {
                return null;
            }

            final String walletName = raw.trim();
            return new CashAccount(walletName, selectedCurrency);
        });
    }

    private static String formatCurrency(final FiatCurrency currency) {
        return currency.getSymbol() + " " + currency.getShortName();
    }
}
