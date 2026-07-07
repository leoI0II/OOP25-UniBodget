package it.unibo.unibodget.view.currency_converter;

import it.unibo.unibodget.controller.currency_converter.CurrencyConverterController;
import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.currency.alert.CurrencyAlert;
import it.unibo.unibodget.model.currency.alert.CurrencyAlertService;
import it.unibo.unibodget.model.currency.alert.MarketAlert;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.view.UI.FXAdapter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.math.BigDecimal;

/**
 * JavaFX widget representing the main currency converter tool.
 * <p>
 * This component allows the user to:
 * <ul>
 *     <li>enter an amount</li>
 *     <li>select source and target currencies</li>
 *     <li>perform a conversion using {@link CurrencyConverterController}</li>
 *     <li>register exchange‑rate alerts via {@link CurrencyAlertService}</li>
 * </ul>
 * <p>
 * The widget is designed as a reusable UI card with theme‑aware styling.
 */
public class ConverterWidgetFX {

    /* -------------------- UI CONSTANTS -------------------- */
    private static final double GRID_GAP = 12;
    private static final double CARD_PADDING = 20;
    private static final double CARD_CORNER_RADIUS = 18;
    private static final double BUTTON_CORNER_RADIUS = 8;

    /* -------------------- CONTROLLERS -------------------- */
    private final CurrencyConverterController controller;
    private final CurrencyAlertService alertService;

    /* -------------------- ROOT UI NODE -------------------- */
    private final GridPane card = new GridPane();

    /* -------------------- INPUT FIELDS -------------------- */
    private final TextField amountField = new TextField();
    public final ComboBox<CurrencyUnit> fromBox = new ComboBox<>();
    public final ComboBox<CurrencyUnit> toBox = new ComboBox<>();

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
    }

    /** @return the amount input field */
    public TextField getAmountField() { 
        return amountField; 
    }

    /** @return the ComboBox for the source currency */
    public ComboBox<CurrencyUnit> getFromBox() { 
        return fromBox; 
    }

    /** @return the ComboBox for the target currency */
    public ComboBox<CurrencyUnit> getToBox() { 
        return toBox; 
    }

    /**
     * Sets the currencies for the converter.
     *
     * @param fromCode the ISO code of the source currency
     * @param toCode the ISO code of the target currency
     */
    public void setCurrencies(String fromCode, String toCode) {
        CurrencyUnit from = Currency.get(fromCode);
        CurrencyUnit to = Currency.get(toCode);

        fromBox.setValue(from);
        toBox.setValue(to);
    }

    /** @return the root JavaFX node of this widget */
    public GridPane getView() { return card; }

    /* =============== UI BUILDING LOGIC ==================== */

    /**
     * Builds the entire UI card, including:
     * <ul>
     *     <li>theme‑aware styling</li>
     *     <li>input fields</li>
     *     <li>conversion button</li>
     *     <li>alert registration controls</li>
     *     <li>result output area</li>
     * </ul>
     */
    private void buildUI() {

        /* ---------- THEME SETUP ---------- */
        final Theme theme = ThemeManager.getTheme();
        final Font font = FXAdapter.toFXFont(theme);
        final Font resultFont = Font.font(theme.getFontFamily(), FontWeight.BOLD, theme.getFontSize() + 10);

        final Color textColor = FXAdapter.toFXColor(theme.getTextColor());
        final Color buttonColor = FXAdapter.toFXColor(theme.getButtonColor());
        final Color buttonTextColor = FXAdapter.toFXColor(Theme.getReadableTextColor(theme.getButtonColor()));

        // Glass‑style background
        final Color glassBase = FXAdapter.toFXColor(Theme.getReadableTextColor(theme.getPrimaryColor()));
        final Color glassFill = new Color(glassBase.getRed(), glassBase.getGreen(), glassBase.getBlue(), 0.045);
        final Color glassBorder = new Color(glassBase.getRed(), glassBase.getGreen(), glassBase.getBlue(), 0.07);

        /* ---------- CARD LAYOUT ---------- */
        card.setHgap(GRID_GAP);
        card.setVgap(GRID_GAP);
        card.setPadding(new Insets(CARD_PADDING));
        card.setBackground(new Background(new BackgroundFill(glassFill, new CornerRadii(CARD_CORNER_RADIUS), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(glassBorder, BorderStrokeStyle.SOLID, new CornerRadii(CARD_CORNER_RADIUS), BorderWidths.DEFAULT)));

        /* ---------- AMOUNT FIELD ---------- */
        amountField.setPromptText("e.g. 10");
        amountField.setFont(font);
        amountField.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 8; -fx-text-fill: white;");

        /* ---------- CURRENCY BOXES ---------- */
        //final ObservableList<CurrencyUnit> currencies = FXCollections.observableArrayList();
        //CurrencyUnit.basicCurrencies().forEach(currencies::add);
        final ObservableList<CurrencyUnit> currencies = FXCollections.observableArrayList(Currency.all());
        
        fromBox.setItems(currencies);
        toBox.setItems(currencies);

        //fromBox.setValue(currencies.get(0));
        toBox.setValue(currencies.get(1));

        fromBox.setValue(Currency.get("EUR"));
        toBox.setValue(Currency.get("USD"));
        System.out.println("FROM selected: " + fromBox.getValue().getClass());
        System.out.println("FROM code: " + fromBox.getValue().getCode());
        System.out.println("TO selected: " + toBox.getValue().getClass());
        System.out.println("TO code: " + toBox.getValue().getCode());

        configureCurrencyBox(fromBox, font);
        configureCurrencyBox(toBox, font);

        // Default selection
        //final FiatCurrency[] fiat = FiatCurrency.values();
        //if (fiat.length >= 2) {
        //    fromBox.setValue(fiat[0]);
        //    toBox.setValue(fiat[1]);
        //}

        /* ---------- LABELS ---------- */
        final Label amountLabel = createMutedLabel("Amount", font, textColor);
        final Label fromLabel = createMutedLabel("From", font, textColor);
        final Label toLabel = createMutedLabel("To", font, textColor);
        final Label resultLabel = createMutedLabel("Net Balance Result", font, textColor);

        /* ---------- SWAP BUTTON ---------- */
        final Button swapButton = new Button("⇅");
        swapButton.setFont(font);
        swapButton.setTextFill(textColor);
        swapButton.setBackground(new Background(new BackgroundFill(glassFill, new CornerRadii(BUTTON_CORNER_RADIUS), Insets.EMPTY)));

        swapButton.setOnAction(e -> {
            CurrencyUnit temp = fromBox.getValue();
            fromBox.setValue(toBox.getValue());
            toBox.setValue(temp);
        });

        /* ---------- CONVERT BUTTON ---------- */
        final Button convertButton = new Button("Convert");
        convertButton.setFont(font);
        convertButton.setBackground(new Background(new BackgroundFill(buttonColor, new CornerRadii(BUTTON_CORNER_RADIUS), Insets.EMPTY)));
        convertButton.setTextFill(buttonTextColor);
        convertButton.setMaxWidth(Double.MAX_VALUE);
        convertButton.setDisable(true);

        /* ---------- ALERT CONTROLS ---------- */
        final TextField thresholdField = new TextField();
        thresholdField.setPromptText("Threshold (e.g. 1.10)");
        thresholdField.setFont(font);
        thresholdField.setStyle(amountField.getStyle());

        final ComboBox<String> directionBox = new ComboBox<>(FXCollections.observableArrayList("Above", "Below"));
        directionBox.setValue("Above");

        final Button addAlertButton = new Button("Set Alert");
        addAlertButton.setFont(font);

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
        final Label output = new Label("Waiting...");
        output.setFont(resultFont);
        output.setTextFill(textColor);
        output.setWrapText(true);

        /* ---------- VALIDATION ---------- */
        final Runnable validate = () -> {
            final BigDecimal parsed = parseDecimal(amountField.getText());
            convertButton.setDisable(parsed == null ||
                    parsed.compareTo(BigDecimal.ZERO) <= 0 ||
                    fromBox.getValue() == null ||
                    toBox.getValue() == null);
        };

        amountField.textProperty().addListener((obs, old, val) -> validate.run());
        fromBox.valueProperty().addListener((obs, old, val) -> validate.run());
        toBox.valueProperty().addListener((obs, old, val) -> validate.run());
        validate.run();

        /* ---------- CONVERSION ACTION ---------- */
        convertButton.setOnAction(e -> {
            try {
                final BigDecimal amount = parseDecimal(amountField.getText());
                final CurrencyUnit from = fromBox.getValue();
                final CurrencyUnit to = toBox.getValue();

                final BigDecimal result = controller.convert(amount, from, to);

                output.setText(result.toPlainString() + " " + to.getCode());
                resultLabel.setText("Converted from " + amount.toPlainString() + " " + from.getCode());

                // Compute rate for alert evaluation
                double rate = result.divide(amount, 4, java.math.RoundingMode.HALF_UP).doubleValue();
                new MarketAlert(this.alertService).checkAndShowAlerts(rate, from, to);

            } catch (Exception ex) {
                output.setFont(font);
                output.setText("Error: " + ex.getMessage());
            }
        });

        /* ---------- LAYOUT ASSEMBLY ---------- */
        card.addRow(0, amountLabel, amountField);
        card.addRow(1, fromLabel, fromBox);
        card.add(swapButton, 1, 2);
        GridPane.setHalignment(swapButton, HPos.CENTER);
        card.addRow(3, toLabel, toBox);
        card.add(convertButton, 0, 4, 2, 1);

        final VBox resultBox = new VBox(4, resultLabel, output);
        resultBox.setAlignment(Pos.CENTER_LEFT);
        resultBox.setPadding(new Insets(10, 0, 0, 0));
        card.add(resultBox, 0, 5, 2, 1);

        card.addRow(6, thresholdField, directionBox);
        card.add(addAlertButton, 0, 7, 2, 1);

        GridPane.setHgrow(amountField, Priority.ALWAYS);
        GridPane.setHgrow(fromBox, Priority.ALWAYS);
        GridPane.setHgrow(toBox, Priority.ALWAYS);
    }

    /* ================== HELPER METHODS ==================== */

    /** Creates a muted label used for section headers.
     *  
     * @param text the label text
     * @param font the font to use
     * @param color the text color
     * @return a styled Label instance
    */
    private Label createMutedLabel(String text, Font font, Color color) {
        Label l = new Label(text);
        l.setFont(font);
        l.setTextFill(color);
        l.setOpacity(0.7);
        return l;
    }

    /** Configures a currency ComboBox with formatting and styling. 
     * 
     * @param box the ComboBox to configure
     * @param font the font to use for the ComboBox items
    */
    private void configureCurrencyBox(final ComboBox<CurrencyUnit> box, final Font font) {
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-font-size: " + font.getSize() + "px; -fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 8;");

        box.setConverter(new StringConverter<>() {
            @Override public String toString(final CurrencyUnit c) { return c == null ? "" : formatCurrency(c); }
            @Override public CurrencyUnit fromString(final String s) { return null; }
        });

        box.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        box.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(final CurrencyUnit item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });
    }

    /** Formats a currency for display. 
     * 
     * @param currency the currency to format
     * @return a string representation, e.g., "€ EUR" for Euro
    */
    private String formatCurrency(final CurrencyUnit currency) {
        if (currency instanceof FiatCurrency fiat) {
            return fiat.getSymbol() + " " + fiat.getShortName();
        }
        if (currency instanceof it.unibo.unibodget.model.currency.Currency c) {
            return c.getSymbol() + " " + c.getShortName();
        }
        return currency.getCode();
    }

    /** Parses a decimal number from user input. 
     * 
     * @param text the string to parse
     * @return the parsed BigDecimal or null if parsing fails
    */
    private BigDecimal parseDecimal(final String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return new BigDecimal(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
