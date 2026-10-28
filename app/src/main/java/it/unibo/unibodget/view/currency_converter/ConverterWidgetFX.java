package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.CryptoCurrency;
import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.MonetaryInputParser;
import it.unibo.unibodget.model.currency.StockMarketCurrency;
import it.unibo.unibodget.model.currency.alert.CurrencyAlert;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.alert.MarketAlert;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.view.ui.FXAdapter;
import it.unibo.unibodget.view.utils.DialogHelper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.StringConverter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JavaFX widget representing the main currency converter tool.
 *
 * <p>
 * This component allows the user to:
 * <ul>
 *     <li>enter an amount</li>
 *     <li>select source and target currencies</li>
 *     <li>perform a conversion using {@link CurrencyConverterController}</li>
 *     <li>register exchange‑rate alerts via {@link CurrencyAlertService}</li>
 * </ul>
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "JavaFX UI widget stores services and exposes UI controls to callers")
public final class ConverterWidgetFX {

    /* -------------------- UI CONSTANTS -------------------- */
    private static final double GRID_GAP = 12;
    private static final double CARD_PADDING = 20;
    private static final double OPACITY = 0.7;
    private static final int COL_SPAN_2 = 2;
    private static final int ROW_SPAN_1 = 1;
    private static final int COL_INDEX_0 = 0;
    private static final int ROW_INDEX_5 = 5;
    private static final int ROW_INDEX_6 = 6;
    private static final int ROW_INDEX_7 = 7;
    private static final double COL_LABEL_MIN_WIDTH = 70.0;
    private static final double COL_LABEL_PREF_WIDTH = 75.0;
    private static final double COL_INPUT_MIN_WIDTH = 180.0;
    private static final double CARD_MIN_WIDTH = 320.0;
    private static final double CARD_PREF_WIDTH = 350.0;
    private static final double DIRECTION_BOX_MIN_WIDTH = 125.0;
    private static final double DIRECTION_BOX_PREF_WIDTH = 130.0;

    /* -------------------- CONTROLLERS -------------------- */
    private final CurrencyConverterController controller;
    private final CurrencyAlertService alertService;

    /* -------------------- ROOT UI NODE -------------------- */
    private final GridPane card = new GridPane();

    /* -------------------- INPUT FIELDS -------------------- */
    private final TextField amountField = new TextField();
    private final ComboBox<CurrencyUnit> fromBox = new ComboBox<>();
    private final ComboBox<CurrencyUnit> toBox = new ComboBox<>();
    private final Label amountLabel = new Label("Amount");
    private final Label fromLabel = new Label("From");
    private final Label toLabel = new Label("To");
    private final Label resultLabel = new Label("Net Balance Result");
    private final Button swapButton = new Button("⇅");
    private final Button convertButton = new Button("Convert");
    private final TextField thresholdField = new TextField();
    private final ComboBox<String> directionBox =
            new ComboBox<>(FXCollections.observableArrayList("Above", "Below"));
    private final Button addAlertButton = new Button("Set Alert");
    private final Label output = new Label("Waiting...");

    /**
     * Creates a new converter widget.
     *
     * @param controller   the controller performing currency conversions
     * @param alertService the service managing exchange‑rate alerts
     */
    public ConverterWidgetFX(final CurrencyConverterController controller,
                             final CurrencyAlertService alertService) {
        this.controller = controller;
        this.alertService = alertService;
        buildUI();
        ThemeManager.addListener(t -> Platform.runLater(this::applyTheme));
    }

    /**
     * Return the amount input field.
     *
     * @return the amount input field
     */
    public TextField getAmountField() {
        return amountField;
    }

    /**
     * Return the ComboBox for the source currency.
     *
     * @return the ComboBox for the source currency
     */
    public ComboBox<CurrencyUnit> getFromBox() {
        return fromBox;
    }

    /**
     * Return the ComboBox for the target currency.
     *
     * @return the ComboBox for the target currency
     */
    public ComboBox<CurrencyUnit> getToBox() {
        return toBox;
    }

    /**
     * Restores a saved pair, warning if either currency is unavailable.
     *
     * @param fromCode the source currency code
     * @param toCode the target currency code
     */
    public void setCurrencies(final String fromCode, final String toCode) {
        final var from = fromBox.getItems().stream()
                .filter(currency -> currency.getCode().equalsIgnoreCase(fromCode))
                .findFirst();

        final var to = toBox.getItems().stream()
                .filter(currency -> currency.getCode().equalsIgnoreCase(toCode))
                .findFirst();

        if (from.isEmpty() || to.isEmpty()) {
            final String missingCodes = Stream.of(fromCode, toCode)
                    .filter(code -> fromBox.getItems().stream()
                            .noneMatch(currency ->
                                    currency.getCode().equalsIgnoreCase(code)))
                    .distinct()
                    .collect(Collectors.joining(", "));

            DialogHelper.showWarning(
                    card,
                    "Watchlist",
                    "Couple not available",
                    "Currency not available: " + missingCodes
                            + ".\nThe current selection has not been modified.");
            return;
        }

        fromBox.setValue(from.get());
        toBox.setValue(to.get());
    }

    /**
     * Determines the display order of currency groups.
     *
     * @param currency the currency to classify
     * @return the group order: fiat, stocks, crypto, others
     */
    private static int currencyGroupOrder(final CurrencyUnit currency) {
        if (currency instanceof FiatCurrency) {
            return 0;
        }
        if (currency instanceof StockMarketCurrency) {
            return 1;
        }
        if (currency instanceof CryptoCurrency) {
            return 2;
        }
        return 3;
    }

    /**
     * Return the view type GridPane.
     *
     * @return the root JavaFX node of this widget
     */
    public GridPane getView() {
        return card;
    }

    /* =============== UI BUILDING LOGIC ==================== */

    /**
     * Builds the entire UI card.
     *
     * <p>
     * Including:
     * <ul>
     *     <li>theme‑aware styling</li>
     *     <li>input fields</li>
     *     <li>conversion button</li>
     *     <li>alert registration controls</li>
     *     <li>result output area</li>
     * </ul>
     */
    private void buildUI() {

        /* ---------- CARD LAYOUT ---------- */
        card.getStyleClass().add("card");
        card.setHgap(GRID_GAP);
        card.setVgap(GRID_GAP);
        card.setPadding(new Insets(CARD_PADDING));
        card.setMinWidth(CARD_MIN_WIDTH);
        card.setPrefWidth(CARD_PREF_WIDTH);

        final ColumnConstraints colLabels = new ColumnConstraints();
        colLabels.setMinWidth(COL_LABEL_MIN_WIDTH);
        colLabels.setPrefWidth(COL_LABEL_PREF_WIDTH);
        colLabels.setHgrow(Priority.NEVER);

        final ColumnConstraints colInputs = new ColumnConstraints();
        colInputs.setMinWidth(COL_INPUT_MIN_WIDTH);
        colInputs.setHgrow(Priority.ALWAYS);

        card.getColumnConstraints().setAll(colLabels, colInputs);

        /* ---------- AMOUNT FIELD ---------- */
        amountField.setPromptText("e.g. 10");
        amountField.setMaxWidth(Double.MAX_VALUE);

        /* ---------- CURRENCY BOXES ---------- */
        final ObservableList<CurrencyUnit> currencies =
                FXCollections.observableArrayList(CurrencyUnit.allCurrencies());

        currencies.sort(
                Comparator.comparingInt(ConverterWidgetFX::currencyGroupOrder)
                        .thenComparing(CurrencyUnit::getCode, String.CASE_INSENSITIVE_ORDER)
        );

        fromBox.setItems(currencies);
        toBox.setItems(currencies);
        fromBox.setMaxWidth(Double.MAX_VALUE);
        toBox.setMaxWidth(Double.MAX_VALUE);

        setCurrencies("EUR", "USD");

        configureCurrencyBox(fromBox);
        configureCurrencyBox(toBox);

        /* ---------- SWAP BUTTON ---------- */
        swapButton.setOnAction(e -> {
            final CurrencyUnit temp = fromBox.getValue();
            fromBox.setValue(toBox.getValue());
            toBox.setValue(temp);
        });

        /* ---------- CONVERT BUTTON ---------- */
        convertButton.getStyleClass().add("button-primary");
        convertButton.setMaxWidth(Double.MAX_VALUE);
        convertButton.setDisable(true);

        /* ---------- ALERT CONTROLS ---------- */
        thresholdField.setPromptText("Threshold (e.g. 1.10)");
        thresholdField.setMaxWidth(Double.MAX_VALUE);
        directionBox.setValue("Above");
        directionBox.getStyleClass().add("direction-box");
        directionBox.setMinWidth(DIRECTION_BOX_MIN_WIDTH);
        directionBox.setPrefWidth(DIRECTION_BOX_PREF_WIDTH);

        addAlertButton.setOnAction(e -> {
            final BigDecimal parsedThreshold = parseDecimal(thresholdField.getText());
            if (parsedThreshold == null || fromBox.getValue() == null || toBox.getValue() == null) {
                return;
            }

            final boolean isLowerThan = "Below".equals(directionBox.getValue());

            alertService.addAlert(new CurrencyAlert(
                    fromBox.getValue(),
                    toBox.getValue(),
                    parsedThreshold.doubleValue(),
                    isLowerThan
            ));

            thresholdField.clear();
        });

        /* ---------- RESULT OUTPUT ---------- */
        output.setWrapText(true);
        output.getStyleClass().add("converter-output");

        applyTheme();

        /* ---------- VALIDATION ---------- */
        final Runnable validate = () -> {
            final BigDecimal parsed = parseDecimal(amountField.getText());
            convertButton.setDisable(parsed == null || parsed.compareTo(BigDecimal.ZERO) <= 0
                || fromBox.getValue() == null || toBox.getValue() == null);
        };

        amountField.textProperty().addListener((obs, old, val) -> validate.run());
        fromBox.valueProperty().addListener((obs, old, val) -> validate.run());
        toBox.valueProperty().addListener((obs, old, val) -> validate.run());
        validate.run();

        /* ---------- CONVERSION ACTION ---------- */
        convertButton.setOnAction(e -> {
            output.setText("");
            resultLabel.setText("");

            final BigDecimal amount = parseDecimal(amountField.getText());
            final CurrencyUnit from = fromBox.getValue();
            final CurrencyUnit to = toBox.getValue();

            // 1. check import
            if (amount == null || amount.signum() <= 0) {
                output.setText("Inserisci un importo maggiore di zero.");
                return;
            }

            // 2. check currency unit both selected
            if (from == null || to == null) {
                output.setText("Seleziona la valuta di partenza e di arrivo.");
                return;
            }

            final BigDecimal result;
            // 3. convert
            try {
                result = controller.convert(amount, from, to);

                output.setText(result.toPlainString() + " " + to.getCode());

                resultLabel.setText(
                        "Converted from " + amount.toPlainString()
                        + " " + from.getCode()
                );

            } catch (final IllegalArgumentException | IllegalStateException ex) {
                output.setText("Conversione non disponibile. Riprova più tardi.");
                return;
            }

            // 4. same currency
            if (from.getCode().equalsIgnoreCase(to.getCode())) {
                return;
            }

            // 5. check alert
            try {
                final double rate = result.divide(
                        amount,
                        4,
                        java.math.RoundingMode.HALF_UP
                ).doubleValue();

                final Window owner = DialogHelper.findWindow(card).orElse(null);
                new MarketAlert(this.alertService)
                        .checkAndShowAlerts(rate, from, to, owner);

            } catch (final IllegalArgumentException | IllegalStateException ignored) {
                // Alert failures are non-critical during conversion display.
            }
        });

        /* ---------- LAYOUT ASSEMBLY ---------- */
        card.addRow(0, amountLabel, amountField);
        card.addRow(1, fromLabel, fromBox);
        card.add(swapButton, 1, 2);
        GridPane.setHalignment(swapButton, HPos.CENTER);
        card.addRow(3, toLabel, toBox);
        card.add(convertButton, 0, 4, COL_SPAN_2, ROW_SPAN_1);

        final VBox resultBox = new VBox(4, resultLabel, output);
        resultBox.setAlignment(Pos.CENTER_LEFT);
        resultBox.setPadding(new Insets(10, 0, 0, 0));
        card.add(resultBox, COL_INDEX_0, ROW_INDEX_5, COL_SPAN_2, ROW_SPAN_1);

        final HBox alertInputs = new HBox(8, thresholdField, directionBox);
        HBox.setHgrow(thresholdField, Priority.ALWAYS);
        HBox.setHgrow(directionBox, Priority.NEVER);
        card.add(alertInputs, COL_INDEX_0, ROW_INDEX_6, COL_SPAN_2, ROW_SPAN_1);

        card.add(addAlertButton, COL_INDEX_0, ROW_INDEX_7, COL_SPAN_2, ROW_SPAN_1);

        GridPane.setHgrow(amountField, Priority.ALWAYS);
        GridPane.setHgrow(fromBox, Priority.ALWAYS);
        GridPane.setHgrow(toBox, Priority.ALWAYS);
    }

    /* ================== HELPER METHODS ==================== */

    private void applyTheme() {
        final Color textColor = FXAdapter.toFXColor(ThemeManager.getTheme().getTextColor());

        ThemeManager.applyFont(amountField);
        ThemeManager.applyFont(swapButton);
        ThemeManager.applyFont(convertButton);
        ThemeManager.applyFont(thresholdField);
        ThemeManager.applyFont(directionBox);
        ThemeManager.applyFont(addAlertButton);

        styleMutedLabel(amountLabel, textColor);
        styleMutedLabel(fromLabel, textColor);
        styleMutedLabel(toLabel, textColor);
        styleMutedLabel(resultLabel, textColor);

        ThemeManager.applyFont(output);
        output.setTextFill(textColor);

        ThemeManager.applyFont(card);
    }

    /**
     * Styles a muted label used for section headers.
     *
     * @param label the label to style
     * @param color the text color
     */
    private void styleMutedLabel(final Label label, final Color color) {
        ThemeManager.applyFont(label);
        label.setTextFill(color);
        label.setOpacity(OPACITY);
    }

    /**
     * Configures a currency ComboBox with formatting and styling.
     *
     * @param box the ComboBox to configure
     */
    private void configureCurrencyBox(final ComboBox<CurrencyUnit> box) {
        box.setMaxWidth(Double.MAX_VALUE);

        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(final CurrencyUnit c) {
                return c == null ? "" : formatCurrency(c);
            }

            @Override
            public CurrencyUnit fromString(final String s) {
                return null;
            }
        });

        box.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        box.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });
    }

    /**
     * Formats a currency for display.
     *
     * @param currency the currency to format
     * @return a string representation, e.g., "€ EUR" for Euro
     */
    private String formatCurrency(final CurrencyUnit currency) {
        if (currency instanceof FiatCurrency fiat) {
            return fiat.getSymbol() + " " + fiat.getShortName();
        }
        if (currency instanceof Currency c) {
            return c.getSymbol() + " " + c.getShortName();
        }
        return currency.getCode();
    }

    /**
     * Parses a decimal number from user input.
     *
     * @param text the string to parse
     * @return the parsed BigDecimal or null if parsing fails
     */
    private BigDecimal parseDecimal(final String text) {
        return MonetaryInputParser.parse(text).orElse(null);
    }

}
