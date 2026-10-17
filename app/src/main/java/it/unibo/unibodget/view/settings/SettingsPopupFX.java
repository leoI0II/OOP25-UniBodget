package it.unibo.unibodget.view.settings;

import java.util.Locale;
import java.util.Objects;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.SettingsSnapshot;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import it.unibo.unibodget.model.utils.ARGBColor;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Modal dialog for editing application settings.
 *
 * <p>Allows changing the primary color, font family, boldness and base
 * currency, and viewing previous configurations without selecting them.
 */
public final class SettingsPopupFX {

    private static final int HISTORY_HEIGHT = 150;
    private static final int HISTORY_WIDTH = 450;
    private static final int GRID_GAP = 10;
    private static final int COLOR_COMPONENT_MAX = 255;

    private final SettingsController controller;

    /**
     * Creates a popup bound to the settings controller.
     *
     * @param controller the controller managing settings and persistence
     */
    public SettingsPopupFX(final SettingsController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    /**
     * Displays the settings dialog.
     *
     * @param owner the parent window
     */
    public void show(final Stage owner) {
        final Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");

        final Settings current = controller.getSettings();

        // Expandable history with no selection or restore action
        final ListView<String> historyList = new ListView<>();
        historyList.setPrefHeight(HISTORY_HEIGHT);
        historyList.setMinHeight(HISTORY_HEIGHT);
        historyList.setPrefWidth(HISTORY_WIDTH);
        historyList.setFocusTraversable(false);
        historyList.setSelectionModel(null);
        historyList.setPlaceholder(new Label("No previous configurations"));

        historyList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
                setStyle("-fx-cursor: default;");
                ThemeManager.applyFont(this);
            }
        });

        for (final SettingsSnapshot snapshot
                : controller.getAllSavedConfigurations()) {
            final Theme savedTheme = snapshot.getTheme();

            historyList.getItems().add(
                    snapshot.getSavedAt()
                    + "\nTheme: " + savedTheme.getName()
                    + " · Background: " + savedTheme.getPrimaryColor().toHexString()
                    + "\nButtons: " + savedTheme.getButtonColor().toHexString()
                    + " · Text: " + savedTheme.getTextColor().toHexString()
                    + "\nFont: " + savedTheme.getFontFamily()
                    + " · Size: " + savedTheme.getFontSize()
                    + " · Bold: " + savedTheme.isBoldText()
                    + "\nBase currency: " + snapshot.getBaseCurrency()
            );
        }

        final TitledPane historyPane = new TitledPane(
                "Previous configurations — read only",
                historyList
        );
        historyPane.setExpanded(false);
        historyPane.setAnimated(false);

        // Color selector: no separate HEX text field
        final ARGBColor currentColor = current.getTheme().getPrimaryColor();

        final ColorPicker colorPicker = new ColorPicker(
                Color.rgb(
                        currentColor.red(),
                        currentColor.green(),
                        currentColor.blue(),
                        currentColor.alpha() / (double) COLOR_COMPONENT_MAX
                )
        );
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        // Font family selector
        final ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll(Font.getFamilies());
        fontBox.setValue(current.getTheme().getFontFamily());
        fontBox.setMaxWidth(Double.MAX_VALUE);

        // Bold toggle
        final CheckBox boldCheck = new CheckBox("Bold");
        boldCheck.setSelected(current.getTheme().isBoldText());

        // Base currency selector
        final ComboBox<FiatCurrency> currencyBox = new ComboBox<>();
        currencyBox.getItems().addAll(FiatCurrency.values());

        final FiatCurrency currentCurrency =
                current.getBaseCurrencyUnit() instanceof FiatCurrency fiat
                        ? fiat
                        : FiatCurrency.EUR;

        currencyBox.setValue(currentCurrency);
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        // Layout
        final GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);

        grid.add(historyPane, 0, 0, 2, 1);
        grid.addRow(1, new Label("Color:"), colorPicker);
        grid.addRow(2, new Label("Font:"), fontBox);
        grid.add(boldCheck, 1, 3);
        grid.addRow(4, new Label("Base currency:"), currencyBox);

        final ButtonType saveButton = new ButtonType(
                "Apply",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButton) {
                final Color selectedColor = colorPicker.getValue();

                final String selectedHex = String.format(
                        Locale.ROOT,
                        "#%02X%02X%02X%02X",
                        Math.round(selectedColor.getOpacity() * COLOR_COMPONENT_MAX),
                        Math.round(selectedColor.getRed() * COLOR_COMPONENT_MAX),
                        Math.round(selectedColor.getGreen() * COLOR_COMPONENT_MAX),
                        Math.round(selectedColor.getBlue() * COLOR_COMPONENT_MAX)
                );

                final ARGBColor primaryColor = new ARGBColor(selectedHex);

                final Theme newTheme = new Theme(
                        "Custom",
                        primaryColor,
                        current.getTheme().getButtonColor(),
                        Theme.getReadableTextColor(primaryColor),
                        fontBox.getValue(),
                        current.getTheme().getFontSize(),
                        boldCheck.isSelected()
                );

                final String newCurrency = currencyBox.getValue().getCode();

                final boolean unchanged =
                        newTheme.equals(current.getTheme())
                        && newCurrency.equals(current.getBaseCurrency());

                if (!unchanged) {
                    controller.changeTheme(newTheme);
                    controller.changeBaseCurrency(newCurrency);
                    controller.saveConfiguration();

                    ThemeManager.applyThemeToScene(owner.getScene());
                }
            }

            return null;
        });

        dialog.setOnShown(event ->
                ThemeManager.applyThemeToScene(
                        dialog.getDialogPane().getScene()
                )
        );

        // Resizes the dialog when the history is expanded or collapsed.
        historyPane.expandedProperty().addListener((observable, oldValue, expanded) ->
                Platform.runLater(() -> {
                    dialog.getDialogPane().requestLayout();
                    dialog.getDialogPane().getScene().getWindow().sizeToScene();
                })
        );

        dialog.showAndWait();
    }
}
