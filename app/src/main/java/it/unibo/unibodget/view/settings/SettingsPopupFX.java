package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.SettingsSnapshot;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.utils.ARGBColor;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * Popup window used to edit and apply application settings.
 *
 * <p>The dialog allows the user to:</p>
 * <ul>
 *     <li>select a previously saved configuration</li>
 *     <li>modify theme colors, font family, size and boldness</li>
 *     <li>change the base currency</li>
 * </ul>
 *
 * <p>Changes are applied through the {@link SettingsController},
 * which handles persistence and theme updates.</p>
 */
public final class SettingsPopupFX {

    private static final int ROW_INDEX_5 = 5;
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
     * <p>The dialog supports both applying a saved configuration
     * and creating a new one based on user input.</p>
     *
     * @param owner the parent window that owns this popup
     */
    public void show(final Stage owner) {

        // Create modal dialog
        final Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");

        // Load current settings
        final Settings current = controller.getSettings();
        // Previous configuration
        final TextArea historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        historyArea.setPrefRowCount(4);
        historyArea.setPrefWidth(450);

        final StringBuilder historyText = new StringBuilder();

        for (final SettingsSnapshot snapshot
                : controller.getAllSavedConfigurations()) {
            historyText.append(snapshot.getSavedAt())
                    .append(" · ")
                    .append(snapshot.getTheme().getPrimaryColor().toHexString())
                    .append(" · ")
                    .append(snapshot.getTheme().getFontFamily())
                    .append(" ")
                    .append(snapshot.getTheme().getFontSize())
                    .append("pt · ")
                    .append(snapshot.getBaseCurrency())
                    .append(System.lineSeparator());
        }

        historyArea.setText(
                historyText.length() == 0
                        ? "Nessuna configurazione precedente"
                        : historyText.toString()
        );

        // Primary color (HEX)
        final ARGBColor currentColor =
        current.getTheme().getPrimaryColor();

        // Nel campo mostriamo RGB: sei cifre, senza alpha.
        final String initialHex = String.format(
                "#%02X%02X%02X",
                currentColor.red(),
                currentColor.green(),
                currentColor.blue()
        );

        final TextField colorField = new TextField(initialHex);

        // Anche il picker viene inizializzato dai componenti RGB,
        // senza interpretare il formato ARGB come CSS.
        final ColorPicker colorPicker = new ColorPicker(
                Color.rgb(
                        currentColor.red(),
                        currentColor.green(),
                        currentColor.blue()
                )
        );

        colorPicker.setOnAction(event -> {
            final Color selected = colorPicker.getValue();

            final String hex = String.format(
                    "#%02X%02X%02X",
                    Math.round(selected.getRed() * 255),
                    Math.round(selected.getGreen() * 255),
                    Math.round(selected.getBlue() * 255)
            );

            colorField.setText(hex);

            System.out.println("[SETTINGS] Colore scelto: " + hex);
        });

        // Se scrivi un codice RGB valido, aggiorna anche il picker.
        colorField.textProperty().addListener((obs, oldValue, newValue) -> {
            final String hex = newValue.trim();

            if (hex.matches("#[0-9a-fA-F]{6}")) {
                colorPicker.setValue(Color.web(hex));
            }
        });

        final HBox colorControls = new HBox(
                10,
                colorField,
                colorPicker
        );

        // Font family selector
        final ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll(Font.getFamilies());
        fontBox.setValue(current.getTheme().getFontFamily());

        // Font size selector
        final Spinner<Integer> fontSize = new Spinner<>(6, 20, current.getTheme().getFontSize());

        // Bold toggle
        final CheckBox boldCheck = new CheckBox("Bold");
        boldCheck.setSelected(current.getTheme().isBoldText());

        // Base currency
        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll(FiatCurrency.values());
        final FiatCurrency currentCurrency =
                current.getBaseCurrencyUnit() instanceof FiatCurrency fiat
                        ? fiat
                        : FiatCurrency.EUR;
        currencyBox.setValue(currentCurrency);

        // Layout
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Previous:"), historyArea);
        grid.addRow(1, new Label("Color HEX:"), colorControls);
        grid.addRow(2, new Label("Font:"), fontBox);
        grid.addRow(3, new Label("Size:"), fontSize);
        grid.addRow(4, boldCheck);
        grid.addRow(ROW_INDEX_5, new Label("Base currency:"), currencyBox);

        // Buttons
        final ButtonType saveButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);
        
        // Logic
        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                // Build new theme from user input
                final Theme newTheme = new Theme(
                        "Custom",
                        new ARGBColor(colorField.getText()),
                        current.getTheme().getButtonColor(),
                        current.getTheme().getTextColor(),
                        fontBox.getValue(),
                        fontSize.getValue(),
                        boldCheck.isSelected()
                );

                // Selected base currency
                //final String newCurrency = currencyBox.getValue().getShortName();
                final String newCurrency = currencyBox.getValue().getCode();

                // If nothing changed → do nothing
                final boolean unchanged = newTheme.equals(current.getTheme())
                        && newCurrency.equals(current.getBaseCurrency());
                if (unchanged) {
                    return null;
                }

                // Apply new theme + currency
                controller.changeTheme(newTheme);
                controller.changeBaseCurrency(newCurrency);

                // Save snapshot in history
                controller.saveConfiguration();

                // Apply theme to UI
                ThemeManager.applyThemeToScene(owner.getScene());
            }
            return null;
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setOnShown(e ->
            ThemeManager.applyThemeToScene(dialog.getDialogPane().getScene())
        );
        
        dialog.showAndWait();
    }

}
