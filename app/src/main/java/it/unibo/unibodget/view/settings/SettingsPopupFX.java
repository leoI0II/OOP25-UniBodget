package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.SettingsSnapshot;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.utils.ARGBColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Popup window used to edit and apply visual and system application settings.
 *
 * <p>Provides decoupled configuration of appearance mode (Dark or Light) and
 * financial accent highlights, along with typography and base currency settings.</p>
 */
public final class SettingsPopupFX {

    private static final String MODE_DARK = "Dark Mode";
    private static final String MODE_LIGHT = "Light Mode";
    private static final String HEX_FORMAT = "#%02X%02X%02X";
    private static final String HEX_REGEX = "#[0-9a-fA-F]{6}";

    private static final String COLOR_EMERALD = "#10B981";
    private static final String COLOR_SAPPHIRE = "#2563EB";
    private static final String COLOR_VIOLET = "#8B5CF6";
    private static final String COLOR_AMBER = "#F59E0B";
    private static final String COLOR_RUBY = "#E11D48";

    private static final int HISTORY_PREF_WIDTH = 450;
    private static final int HISTORY_ROW_COUNT = 3;
    private static final int GRID_HGAP = 12;
    private static final int GRID_VGAP = 10;
    private static final int SWATCH_SIZE = 24;
    private static final int PREVIEW_PADDING = 12;
    private static final int PREVIEW_GAP = 6;
    private static final int FONT_MIN = 8;
    private static final int FONT_MAX = 24;
    private static final int RGB_255 = 255;
    private static final int CHARCOAL_RED = 24;
    private static final int CHARCOAL_GREEN = 27;
    private static final int CHARCOAL_BLUE = 36;

    private static final int ROW_0 = 0;
    private static final int ROW_1 = 1;
    private static final int ROW_2 = 2;
    private static final int ROW_3 = 3;
    private static final int ROW_4 = 4;
    private static final int ROW_5 = 5;
    private static final int ROW_6 = 6;
    private static final int ROW_7 = 7;
    private static final int ROW_8 = 8;

    private final SettingsController controller;

    /**
     * Creates a popup bound to the given {@link SettingsController}.
     *
     * @param controller the controller managing settings persistence and updates
     */
    public SettingsPopupFX(final SettingsController controller) {
        this.controller = controller;
    }

    /**
     * Displays the settings popup as a modal dialog.
     *
     * @param owner the parent window that owns this popup
     */
    public void show(final Stage owner) {
        final Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings - UniBodget");

        final Settings current = controller.getSettings();

        final TextArea historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        historyArea.setPrefRowCount(HISTORY_ROW_COUNT);
        historyArea.setPrefWidth(HISTORY_PREF_WIDTH);

        final StringBuilder historyText = new StringBuilder();
        for (final SettingsSnapshot snapshot : controller.getAllSavedConfigurations()) {
            historyText.append(snapshot.getSavedAt())
                    .append(" · ")
                    .append(snapshot.getTheme().isDarkMode() ? "Dark" : "Light")
                    .append(" [")
                    .append(snapshot.getTheme().getPrimaryColor().toHexString())
                    .append("] · ")
                    .append(snapshot.getTheme().getFontFamily())
                    .append(" ")
                    .append(snapshot.getTheme().getFontSize())
                    .append("pt · ")
                    .append(snapshot.getBaseCurrency())
                    .append(System.lineSeparator());
        }
        historyArea.setText(historyText.length() == 0 ? "No saved configurations" : historyText.toString());

        final ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(MODE_DARK, MODE_LIGHT);
        modeBox.setValue(current.getTheme().isDarkMode() ? MODE_DARK : MODE_LIGHT);

        final ARGBColor currentColor = current.getTheme().getPrimaryColor();
        final String initialHex = String.format(
                HEX_FORMAT,
                currentColor.red(),
                currentColor.green(),
                currentColor.blue()
        );

        final TextField colorField = new TextField(initialHex);
        colorField.setPrefWidth(90);

        final ColorPicker colorPicker = new ColorPicker(
                Color.rgb(currentColor.red(), currentColor.green(), currentColor.blue())
        );

        final HBox presetSwatches = new HBox(6);
        presetSwatches.setAlignment(Pos.CENTER_LEFT);
        addPresetSwatch(presetSwatches, "Emerald Growth", COLOR_EMERALD, colorField, colorPicker);
        addPresetSwatch(presetSwatches, "Sapphire Trust", COLOR_SAPPHIRE, colorField, colorPicker);
        addPresetSwatch(presetSwatches, "Violet Luxury", COLOR_VIOLET, colorField, colorPicker);
        addPresetSwatch(presetSwatches, "Amber Wealth", COLOR_AMBER, colorField, colorPicker);
        addPresetSwatch(presetSwatches, "Ruby Energy", COLOR_RUBY, colorField, colorPicker);

        final HBox accentRow = new HBox(8, presetSwatches, colorField, colorPicker);
        accentRow.setAlignment(Pos.CENTER_LEFT);

        final ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll(Font.getFamilies());
        fontBox.setValue(current.getTheme().getFontFamily());

        final Spinner<Integer> fontSize = new Spinner<>(FONT_MIN, FONT_MAX, current.getTheme().getFontSize());

        final CheckBox boldCheck = new CheckBox("Bold text");
        boldCheck.setSelected(current.getTheme().isBoldText());

        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll(FiatCurrency.values());
        final FiatCurrency currentCurrency = current.getBaseCurrencyUnit() instanceof FiatCurrency fiat
                ? fiat
                : FiatCurrency.EUR;
        currencyBox.setValue(currentCurrency);

        final VBox previewCard = new VBox(PREVIEW_GAP);
        previewCard.setPadding(new Insets(PREVIEW_PADDING));
        previewCard.setAlignment(Pos.CENTER_LEFT);

        final Label previewTitle = new Label("Live Financial Preview");
        final Label previewAmount = new Label("+ 12,450.80 EUR");
        previewAmount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        final Button previewButton = new Button("Quick Action");

        previewCard.getChildren().addAll(previewTitle, previewAmount, previewButton);

        final Runnable updatePreview = () -> {
            final boolean isDark = MODE_DARK.equals(modeBox.getValue());
            final String hex = colorField.getText().matches(HEX_REGEX) ? colorField.getText() : COLOR_EMERALD;

            final String bgStyle = isDark
                    ? "-fx-background-color: linear-gradient(to bottom right, #0e101b, #181c2a); "
                      + "-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 10px; "
                      + "-fx-background-radius: 10px;"
                    : "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f1f5f9); "
                      + "-fx-border-color: rgba(0,0,0,0.10); -fx-border-radius: 10px; "
                      + "-fx-background-radius: 10px;";
            previewCard.setStyle(bgStyle);

            final String textFill = isDark ? "#ffffff" : "#0f172a";
            final String fontFam = fontBox.getValue();
            final int fSize = fontSize.getValue();
            final String weight = boldCheck.isSelected() ? "bold" : "normal";

            previewTitle.setStyle("-fx-text-fill: " + textFill + "; -fx-font-family: '" + fontFam + "'; "
                    + "-fx-font-size: " + fSize + "px; -fx-font-weight: " + weight + ";");
            previewAmount.setStyle("-fx-text-fill: " + hex + "; -fx-font-family: '" + fontFam + "'; "
                    + "-fx-font-size: " + (fSize + 3) + "px; -fx-font-weight: bold;");
            previewButton.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: #ffffff; "
                    + "-fx-background-radius: 8px; -fx-font-weight: bold;");
        };

        modeBox.setOnAction(e -> updatePreview.run());
        fontBox.setOnAction(e -> updatePreview.run());
        fontSize.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());
        boldCheck.setOnAction(e -> updatePreview.run());

        colorPicker.setOnAction(event -> {
            final Color selected = colorPicker.getValue();
            final String hex = String.format(
                    HEX_FORMAT,
                    Math.round(selected.getRed() * RGB_255),
                    Math.round(selected.getGreen() * RGB_255),
                    Math.round(selected.getBlue() * RGB_255)
            );
            colorField.setText(hex);
            updatePreview.run();
        });

        colorField.textProperty().addListener((obs, oldValue, newValue) -> {
            final String hex = newValue.trim();
            if (hex.matches(HEX_REGEX)) {
                colorPicker.setValue(Color.web(hex));
                updatePreview.run();
            }
        });

        updatePreview.run();

        final GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP);
        grid.setVgap(GRID_VGAP);

        grid.addRow(ROW_0, new Label("History:"), historyArea);
        grid.addRow(ROW_1, new Label("Appearance:"), modeBox);
        grid.addRow(ROW_2, new Label("Accent Color:"), accentRow);
        grid.addRow(ROW_3, new Label("Font Family:"), fontBox);
        grid.addRow(ROW_4, new Label("Font Size:"), fontSize);
        grid.addRow(ROW_5, new Label("Style:"), boldCheck);
        grid.addRow(ROW_6, new Label("Base Currency:"), currencyBox);
        grid.addRow(ROW_7, new Label("Live Preview:"), previewCard);

        final ButtonType applyButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == applyButton) {
                final boolean isDark = MODE_DARK.equals(modeBox.getValue());
                final String rawHex = colorField.getText().trim();
                final String hex = rawHex.matches(HEX_REGEX) ? rawHex : COLOR_EMERALD;

                final ARGBColor accent = new ARGBColor(hex);
                final ARGBColor textColor = isDark
                        ? ARGBColor.WHITE
                        : new ARGBColor(RGB_255, CHARCOAL_RED, CHARCOAL_GREEN, CHARCOAL_BLUE);
                final ARGBColor buttonColor = accent;

                final Theme newTheme = new Theme(
                        isDark ? "Dark" : "Light",
                        isDark,
                        accent,
                        buttonColor,
                        textColor,
                        fontBox.getValue(),
                        fontSize.getValue(),
                        boldCheck.isSelected()
                );

                final String newCurrency = currencyBox.getValue().getCode();

                final boolean unchanged = newTheme.equals(current.getTheme())
                        && newCurrency.equals(current.getBaseCurrency());
                if (unchanged) {
                    return null;
                }

                controller.changeTheme(newTheme);
                controller.changeBaseCurrency(newCurrency);
                controller.saveConfiguration();

                ThemeManager.applyThemeToScene(owner.getScene());
            }
            return null;
        });

        dialog.setOnShown(e -> ThemeManager.applyThemeToScene(dialog.getDialogPane().getScene()));
        dialog.showAndWait();
    }

    private static void addPresetSwatch(
            final HBox container,
            final String tooltipText,
            final String hexColor,
            final TextField colorField,
            final ColorPicker colorPicker) {
        final Button swatch = new Button();
        swatch.setPrefSize(SWATCH_SIZE, SWATCH_SIZE);
        swatch.setMinSize(SWATCH_SIZE, SWATCH_SIZE);
        swatch.setMaxSize(SWATCH_SIZE, SWATCH_SIZE);
        swatch.setStyle("-fx-background-color: " + hexColor + "; -fx-background-radius: 50%; "
                + "-fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 50%; -fx-cursor: hand;");
        swatch.setTooltip(new Tooltip(tooltipText + " (" + hexColor + ")"));

        swatch.setOnAction(e -> {
            colorField.setText(hexColor);
            colorPicker.setValue(Color.web(hexColor));
        });

        container.getChildren().add(swatch);
    }

}
