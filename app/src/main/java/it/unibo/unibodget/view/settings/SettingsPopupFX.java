package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.Currency;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Modality;

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
        final ComboBox<SettingsSnapshot> historyBox = new ComboBox<>();
        historyBox.getItems().addAll(controller.getAllSavedConfigurations());
        historyBox.setPromptText("Previous configurations");

        // Custom rendering of snapshot entries
        historyBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(final SettingsSnapshot item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    // Show date + theme summary + base currency
                    final String hex = item.getTheme().getPrimaryColor().toHex();
                    final String font = item.getTheme().getFontFamily();
                    final int size = item.getTheme().getFontSize();
                    setText(item.getSavedAt() + " · " + hex + " · "
                            + font + " " + size + "pt · " + item.getBaseCurrency());
                }
            }
        });
        historyBox.setButtonCell(historyBox.getCellFactory().call(null));

        // Primary color (HEX)
        final TextField colorField = new TextField(current.getTheme().getPrimaryColor().toHex());

        // Font family selector
        final ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll(Font.getFamilies());
        fontBox.setValue(current.getTheme().getFontFamily());

        // Font size selector
        final Spinner<Integer> fontSize = new Spinner<>(8, 40, current.getTheme().getFontSize());

        // Bold toggle
        final CheckBox boldCheck = new CheckBox("Bold");
        boldCheck.setSelected(current.getTheme().isBoldText());

        // Base currency
        final ComboBox<Currency> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll(Currency.all());
        currencyBox.setValue(Currency.get(current.getBaseCurrency()));

        // Layout
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Previous:"), historyBox);
        grid.addRow(1, new Label("Color HEX:"), colorField);
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

                // If user selected a previous configuration → apply it directly
                if (historyBox.getValue() != null) {
                    controller.applyConfiguration(historyBox.getValue());
                    ThemeManager.applyThemeToScene(owner.getScene());
                    return null;
                }

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
                final String newCurrency = currencyBox.getValue().getShortName();

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

        dialog.showAndWait();
    }
}
