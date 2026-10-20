package it.unibo.unibodget.view.settings;

import it.unibo.unibodget.controller.settings.SettingsController;
import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.settings.Settings;
import it.unibo.unibodget.model.settings.SettingsSnapshot;
import it.unibo.unibodget.model.settings.Theme;
import it.unibo.unibodget.model.settings.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Popup window used to edit and apply visual and system application settings.
 *
 * <p>Provides simple switching between Dark Mode and Light Mode,
 * along with typography and base currency settings.</p>
 */
public final class SettingsPopupFX {

    private static final String MODE_DARK = "Dark Mode";
    private static final String MODE_LIGHT = "Light Mode";

    private static final int HISTORY_PREF_WIDTH = 450;
    private static final int HISTORY_ROW_COUNT = 3;
    private static final int GRID_HGAP = 14;
    private static final int GRID_VGAP = 12;
    private static final int GRID_PADDING = 16;
    private static final int FONT_MIN = 8;
    private static final int FONT_MAX = 24;

    private static final int ROW_0 = 0;
    private static final int ROW_1 = 1;
    private static final int ROW_2 = 2;
    private static final int ROW_3 = 3;
    private static final int ROW_4 = 4;
    private static final int ROW_5 = 5;

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

        final StringBuilder historyText = new StringBuilder(128);
        for (final SettingsSnapshot snapshot : controller.getAllSavedConfigurations()) {
            historyText.append(snapshot.getSavedAt())
                    .append(" · ")
                    .append(snapshot.getTheme().isDarkMode() ? "Dark" : "Light")
                    .append(" · ")
                    .append(snapshot.getTheme().getFontFamily())
                    .append(' ')
                    .append(snapshot.getTheme().getFontSize())
                    .append("pt · ")
                    .append(snapshot.getBaseCurrency())
                    .append(System.lineSeparator());
        }
        historyArea.setText(historyText.length() == 0 ? "No saved configurations" : historyText.toString());

        final ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(MODE_DARK, MODE_LIGHT);
        modeBox.setValue(current.getTheme().isDarkMode() ? MODE_DARK : MODE_LIGHT);

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

        final GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP);
        grid.setVgap(GRID_VGAP);
        grid.setPadding(new Insets(GRID_PADDING));

        grid.addRow(ROW_0, new Label("History:"), historyArea);
        grid.addRow(ROW_1, new Label("Appearance:"), modeBox);
        grid.addRow(ROW_2, new Label("Font Family:"), fontBox);
        grid.addRow(ROW_3, new Label("Font Size:"), fontSize);
        grid.addRow(ROW_4, new Label("Style:"), boldCheck);
        grid.addRow(ROW_5, new Label("Base Currency:"), currencyBox);

        final ButtonType applyButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == applyButton) {
                final boolean isDark = MODE_DARK.equals(modeBox.getValue());
                final Theme template = isDark ? Theme.DARK : Theme.LIGHT;

                final Theme newTheme = new Theme(
                        template.getName(),
                        template.getPrimaryColor(),
                        template.getButtonColor(),
                        template.getTextColor(),
                        fontBox.getValue(),
                        fontSize.getValue(),
                        boldCheck.isSelected(),
                        isDark
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

}
