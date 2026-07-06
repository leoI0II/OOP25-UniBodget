package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.BankConversionController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.bank.*;
import it.unibo.unibodget.model.currency.engin.BasicCurrencyConverter;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;

/**
 * JavaFX widget that performs bank‑mediated currency conversions.
 * <p>
 * This component allows the user to:
 * <ul>
 *     <li>select a bank with its fee structure</li>
 *     <li>enter an amount and choose source/target currencies</li>
 *     <li>compute the converted amount, commission, and total cost</li>
 *     <li>add new banks dynamically through a dialog</li>
 * </ul>
 * <p>
 * The widget is designed to be embedded inside larger currency‑conversion views.
 */
public class BankConverterWidgetFX {

    /** Root container of the widget. */
    private final VBox view = new VBox(15);

    /** Dropdown containing available banks. */
    private final ComboBox<Bank> bankBox = new ComboBox<>();

    /** UI labels showing conversion results. */
    private final Label resultLabel = new Label("To be calculated...");
    private final Label detailsLabel = new Label("");
    private final Label convertedLabel = new Label("");

    /** Controller providing bank data and persistence. */
    private final BankConversionController controller;

    /**
     * Creates a new bank conversion widget.
     *
     * @param controller     controller providing available banks and persistence
     * @param amountField    text field containing the amount to convert
     * @param fromBox        combo box for selecting the source currency
     * @param toBox          combo box for selecting the target currency
     * @param baseConverter  base converter used to compute raw currency conversion
     */
    public BankConverterWidgetFX(BankConversionController controller,
                                 TextField amountField,
                                 ComboBox<CurrencyUnit> fromBox,
                                 ComboBox<CurrencyUnit> toBox,
                                 BasicCurrencyConverter baseConverter) {

        this.controller = controller;

        // Populate bank dropdown
        bankBox.getItems().addAll(controller.getAvailableBanks());
        bankBox.setPromptText("Select a bank...");

        // Main action button
        Button calcBtn = new Button("Calculate Bank Fees");
        calcBtn.setMaxWidth(Double.MAX_VALUE);
        calcBtn.setDisable(true);

        // Validation logic: enable button only when amount + bank are valid
        Runnable validate = () -> {
            boolean emptyAmount = amountField.getText() == null || amountField.getText().trim().isEmpty();
            calcBtn.setDisable(emptyAmount || bankBox.getValue() == null);
        };

        amountField.textProperty().addListener((obs, old, val) -> validate.run());
        bankBox.valueProperty().addListener((obs, old, val) -> validate.run());

        // Conversion action
        calcBtn.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                Bank bank = bankBox.getValue();

                if (bank != null && fromBox.getValue() != null && toBox.getValue() != null) {

                    // Perform bank‑mediated conversion
                    BankConversionService service = new BankConversionService(baseConverter);
                    BankConversionResult res = service.convert(amount, fromBox.getValue(), toBox.getValue(), bank);

                    // Update UI
                    convertedLabel.setText("Converted: " + res.getConvertedAmount().toPlainString() + " " + res.getTargetCurrencyCode());
                    resultLabel.setText("Total cost: " + res.getTotalCost().toPlainString() + " " + res.getSourceCurrencyCode());
                    detailsLabel.setText("Fees: " + res.getCommission().toPlainString());

                } else {
                    resultLabel.setText("Select a bank and currencies!");
                }
            } catch (Exception ex) {
                resultLabel.setText("Calculation Error");
            }
        });

        // Button to add a new bank
        Button addBankBtn = new Button("+ Add bank");
        addBankBtn.setOnAction(e -> openAddBankDialog());

        // Assemble UI
        view.getChildren().addAll(
                new Label("Bank conversion"),
                bankBox,
                addBankBtn,
                calcBtn,
                convertedLabel,
                resultLabel,
                detailsLabel
        );
    }

    /**
     * Opens a dialog allowing the user to add a new bank with custom fees.
     * <p>
     * The dialog validates fee values and updates both the controller and the UI.
     */
    private void openAddBankDialog() {
        Dialog<Bank> dialog = new Dialog<>();
        dialog.setTitle("Add new bank");

        // Input fields
        TextField nameField = new TextField();
        TextField fixedField = new TextField();
        TextField percentField = new TextField();

        // Layout for dialog fields
        GridPane grid = new GridPane();
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Fixed Fee:"), fixedField);
        grid.addRow(2, new Label("Percentage Fee:"), percentField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert dialog result into a Bank instance
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    double fixed = Double.parseDouble(fixedField.getText());
                    double percent = Double.parseDouble(percentField.getText());

                    // Basic validation: avoid unrealistic fees
                    if (fixed <= 50.0 && percent <= 50.0) {
                        Bank newBank = new Bank(nameField.getText(), fixed, percent);

                        // Persist and update UI
                        controller.addBank(newBank);
                        bankBox.getItems().add(newBank);

                        return newBank;

                    } else {
                        // Show validation error
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Validation Error");
                        alert.setHeaderText("Invalid Fees");
                        alert.setContentText("The fixed and percentage fees cannot exceed 50%.");
                        alert.showAndWait();
                    }

                } catch (Exception e) {
                    // Show input error
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Input Error");
                    alert.setContentText("Please enter valid numbers in the fee fields.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Returns the root JavaFX node of this widget.
     *
     * @return the widget's {@link VBox} container
     */
    public VBox getView() {
        return view;
    }
}
