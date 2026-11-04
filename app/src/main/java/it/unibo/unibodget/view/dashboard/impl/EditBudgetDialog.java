package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import it.unibo.unibodget.model.currency.MonetaryInputParser;
import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.view.settings.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dialog used to edit the monthly budget of the currently selected wallet.
 */
public final class EditBudgetDialog extends Dialog<EditBudgetRequest> {

    private static final int GRID_GAP = 12;
    private static final int PADDING_INSET = 16;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int THRESHOLD_SCALE = 4;

    private final TextField limitField;
    private final TextField warningThresholdField;

    /**
     * Creates a dialog prefilled with the current wallet budget settings.
     *
     * @param currentSettings
     *            the current settings of the selected wallet
     */
    public EditBudgetDialog(final DefaultBudgetSettings currentSettings) {
        final DefaultBudgetSettings settings = Objects.requireNonNull(currentSettings);

        setTitle("Edit Monthly Budget");
        setHeaderText("Update the monthly budget for the selected wallet");
        setResizable(true);
        ThemeManager.applyThemeToDialog(this);

        final ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        this.limitField = new TextField(settings.getLimitValue().stripTrailingZeros().toPlainString());
        final BigDecimal prefilledPercentage = settings.getWarningThreshold()
                .multiply(ONE_HUNDRED)
                .stripTrailingZeros();
        this.warningThresholdField = new TextField(prefilledPercentage.toPlainString());

        limitField.setPromptText("Monthly budget limit");
        warningThresholdField.setPromptText("Warning threshold % (0 - 100)");

        final GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(new Insets(PADDING_INSET));

        grid.add(new Label("Limit:"), 0, 0);
        grid.add(limitField, 1, 0);

        grid.add(new Label("Warning threshold (%):"), 0, 1);
        grid.add(warningThresholdField, 1, 1);

        getDialogPane().setContent(grid);

        final Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        final Runnable validate = () -> {
            final BigDecimal parsedLimit = parseDecimal(limitField.getText());
            final BigDecimal parsedRatio = parseThresholdRatio(warningThresholdField.getText());

            final boolean validLimit = parsedLimit != null && parsedLimit.compareTo(BigDecimal.ZERO) >= 0;
            final boolean validThreshold = parsedRatio != null;

            saveButton.setDisable(!(validLimit && validThreshold));
        };

        limitField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());
        warningThresholdField.textProperty().addListener((obs, oldValue, newValue) -> validate.run());

        validate.run();

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, saveButtonType)) {
                return null;
            }

            final BigDecimal limitValue = parseDecimal(limitField.getText());
            final BigDecimal warningRatio = parseThresholdRatio(warningThresholdField.getText());

            if (limitValue == null
                    || warningRatio == null
                    || limitValue.compareTo(BigDecimal.ZERO) < 0
                    || warningRatio.compareTo(BigDecimal.ZERO) < 0
                    || warningRatio.compareTo(BigDecimal.ONE) > 0) {
                return null;
            }

            return new EditBudgetRequest(limitValue, warningRatio);
        });
    }

    /**
     * Parses a decimal number from text.
     *
     * @param text
     *            the text to parse
     * @return the parsed decimal, or null if invalid
     */
    private static BigDecimal parseDecimal(final String text) {
        if (text == null) {
            return null;
        }
        return MonetaryInputParser.parse(text.trim()).orElse(null);
    }

    /**
     * Parses a user-friendly percentage input (0 to 100) and converts it to a ratio (0.0 to 1.0).
     *
     * @param text
     *            the user input
     * @return the normalized ratio, or null if invalid
     */
    private static BigDecimal parseThresholdRatio(final String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        final String sanitized = text.replace("%", "").trim();
        final BigDecimal parsed = parseDecimal(sanitized);
        if (parsed == null || parsed.compareTo(BigDecimal.ZERO) < 0 || parsed.compareTo(ONE_HUNDRED) > 0) {
            return null;
        }
        return parsed.divide(ONE_HUNDRED, THRESHOLD_SCALE, RoundingMode.HALF_UP);
    }
}
