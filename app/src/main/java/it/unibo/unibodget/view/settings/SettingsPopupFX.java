package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.settings.*;
import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.utils.ARGBColor;

import javafx.scene.control.*;
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

    private final SettingsController controller;

    /**
     * Creates a popup bound to the given {@link SettingsController}.
     *
     * @param controller the controller managing settings persistence and updates
     */
    public SettingsPopupFX(SettingsController controller) {
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
    public void show(Stage owner) {

        // Create modal dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");

        // Load current settings
        Settings current = controller.getSettings();
        // Previous configuration
        ComboBox<SettingsSnapshot> historyBox = new ComboBox<>();
        historyBox.getItems().addAll(controller.getAllSavedConfigurations());
        historyBox.setPromptText("Previous configurations");

        // Custom rendering of snapshot entries
        historyBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SettingsSnapshot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    // Show date + theme summary + base currency
                    String hex = item.getTheme().getPrimaryColor().toHex();
                    String font = item.getTheme().getFontFamily();
                    int size = item.getTheme().getFontSize();
                    setText(item.getSavedAt() + " · " + hex + " · "
                            + font + " " + size + "pt · " + item.getBaseCurrency());
                }
            }
        });
        historyBox.setButtonCell(historyBox.getCellFactory().call(null));

        // Primary color (HEX)
        TextField colorField = new TextField(current.getTheme().getPrimaryColor().toHex());

        // Font family selector
        ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll(Font.getFamilies());
        fontBox.setValue(current.getTheme().getFontFamily());

        // Font size selector
        Spinner<Integer> fontSize = new Spinner<>(8, 40, current.getTheme().getFontSize());

        // Bold toggle
        CheckBox boldCheck = new CheckBox("Bold");
        boldCheck.setSelected(current.getTheme().isBoldText());

        // Base currency
        ComboBox<Currency> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll(Currency.all());
        currencyBox.setValue(Currency.get(current.getBaseCurrency()));

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Previous:"), historyBox);
        grid.addRow(1, new Label("Color HEX:"), colorField);
        grid.addRow(2, new Label("Font:"), fontBox);
        grid.addRow(3, new Label("Size:"), fontSize);
        grid.addRow(4, boldCheck);
        grid.addRow(5, new Label("Base currency:"), currencyBox);

        // Buttons
        ButtonType saveButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
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
                Theme newTheme = new Theme(
                        "Custom",
                        new ARGBColor(colorField.getText()),
                        current.getTheme().getButtonColor(),
                        current.getTheme().getTextColor(),
                        fontBox.getValue(),
                        fontSize.getValue(),
                        boldCheck.isSelected()
                );

                // Selected base currency
                String newCurrency = currencyBox.getValue().getShortName();

                // If nothing changed → do nothing
                boolean unchanged = newTheme.equals(current.getTheme())
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
