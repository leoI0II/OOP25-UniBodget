package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.math.BigDecimal;

/**
 * JavaFX view for the currency converter feature.
 * 
 * This class builds and displays the graphical interface used to convert
 * monetary values between different currencies. It interacts with a
 * {@link CurrencyConverterController} to perform the actual conversion logic.
 * 
 * The layout is momentanealy structured as follows:
 * - a title label
 * - a card-like container with input fields and the convert button
 * - a result section displaying the conversion output
 */
public class CurrencyConverterViewFX extends VBox {

    private final CurrencyConverterController controller;

    /**
     * Creates a new currency converter view bound to the given controller.
     *
     * @param controller the controller responsible for performing conversions,
     *                   must not be {@code null}
     */
    public CurrencyConverterViewFX(CurrencyConverterController controller) {
        this.controller = controller;
        buildUI();
    }

    /**
     * Builds and initializes all UI components of the currency converter view.
     */
    private void buildUI() {

        // --- THEME ---
        var theme = ThemeManager.getTheme();
        Color fxColor = theme.getPrimaryColor().toFXColor();
        Color textColor = theme.getTextColor().toFXColor();
        Color buttonColor = theme.getButtonColor().toFXColor();
        Color buttonTextColor = Theme.getReadableTextColor(theme.getButtonColor()).toFXColor();
        var font = theme.toFXFont();

        // --- Title ---
        Label title = new Label("Currency Converter");
        title.setFont(font);
        title.setTextFill(textColor);

        // --- Card container ---
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setBackground(new Background(
                new BackgroundFill(fxColor, new CornerRadii(12), Insets.EMPTY)
        ));

        // --- Amount field ---
        Label amountLabel = new Label("Amount:");
        amountLabel.setFont(font);
        amountLabel.setTextFill(textColor);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setFont(font);
        amountField.setStyle("-fx-text-fill: " + theme.getTextColor().toHexString() + ";");

        // --- Currency selectors ---
        Label fromLabel = new Label("From:");
        fromLabel.setFont(font);
        fromLabel.setTextFill(textColor);

        Label toLabel = new Label("To:");
        toLabel.setFont(font);
        toLabel.setTextFill(textColor);

        ComboBox<String> fromBox = new ComboBox<>();
        ComboBox<String> toBox = new ComboBox<>();

        fromBox.setStyle("-fx-font-size: " + theme.getFontSize() + "px; -fx-text-fill: " + theme.getTextColor().toHexString() + ";");
        toBox.setStyle("-fx-font-size: " + theme.getFontSize() + "px; -fx-text-fill: " + theme.getTextColor().toHexString() + ";");

        // Populate currency lists
        CurrencyUnit.allCurrencies().forEach(cu -> {
            fromBox.getItems().add(cu.getCode());
            toBox.getItems().add(cu.getCode());
        });

        // Default selection: first two fiat currencies
        var fiat = FiatCurrency.values();
        if (fiat.length >= 2) {
            fromBox.setValue(fiat[0].getCode());
            toBox.setValue(fiat[1].getCode());
        }

        // --- Convert button ---
        Button convertButton = new Button("Convert");
        convertButton.setFont(font);
        convertButton.setBackground(new Background(
                new BackgroundFill(buttonColor, new CornerRadii(8), Insets.EMPTY)
        ));
        convertButton.setTextFill(buttonTextColor);

        // --- Output area ---
        Label resultLabel = new Label("Result:");
        resultLabel.setFont(font);
        resultLabel.setTextFill(textColor);

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(150);
        output.setFont(font);
        output.setStyle("-fx-text-fill: " + theme.getTextColor().toHexString() + ";");

        // Conversion logic
        convertButton.setOnAction(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                CurrencyUnit from = CurrencyUnit.getByCode(fromBox.getValue());
                CurrencyUnit to = CurrencyUnit.getByCode(toBox.getValue());

                BigDecimal result = controller.convert(amount, from, to);

                output.setText(amount + " " + from.getCode() + " = "
                        + result + " " + to.getCode());

            } catch (Exception ex) {
                output.setText("Error: " + ex.getMessage());
            }
        });

        // Add components to card
        card.getChildren().addAll(
                amountLabel, amountField,
                fromLabel, fromBox,
                toLabel, toBox,
                convertButton
        );

        // Layout settings
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setAlignment(Pos.TOP_CENTER);
        this.setBackground(new Background(
                new BackgroundFill(fxColor, CornerRadii.EMPTY, Insets.EMPTY)
        ));

        // Add all components to root container
        this.getChildren().addAll(title, card, resultLabel, output);
    }
}
