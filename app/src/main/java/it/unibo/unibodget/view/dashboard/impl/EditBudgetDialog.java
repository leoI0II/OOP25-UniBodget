package it.unibo.unibodget.view.dashboard.impl;

import java.math.BigDecimal;
import java.util.Objects;

import it.unibo.unibodget.model.dashboard.impl.DefaultBudgetSettings;
import it.unibo.unibodget.model.settings.ThemeManager;
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
        this.warningThresholdField = new TextField(settings.getWarningThreshold().stripTrailingZeros().toPlainString());

        limitField.setPromptText("Monthly budget limit");
        warningThresholdField.setPromptText("Warning threshold (0 - 1)");

        final GridPane grid = new GridPane();
        grid.setHgap(GRID_GAP);
        grid.setVgap(GRID_GAP);
        grid.setPadding(new Insets(PADDING_INSET));

        grid.add(new Label("Limit:"), 0, 0);
        grid.add(limitField, 1, 0);

        grid.add(new Label("Warning threshold:"), 0, 1);
        grid.add(warningThresholdField, 1, 1);

        getDialogPane().setContent(grid);

        final Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        final Runnable validate = () -> {
            final BigDecimal parsedLimit = parseDecimal(limitField.getText());
            final BigDecimal parsedThreshold = parseDecimal(warningThresholdField.getText());

            final boolean validLimit = parsedLimit != null && parsedLimit.compareTo(BigDecimal.ZERO) >= 0;
            final boolean validThreshold = parsedThreshold != null
                    && parsedThreshold.compareTo(BigDecimal.ZERO) >= 0
                    && parsedThreshold.compareTo(BigDecimal.ONE) <= 0;

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
            final BigDecimal warningThreshold = parseDecimal(warningThresholdField.getText());

            if (limitValue == null
                    || warningThreshold == null
                    || limitValue.compareTo(BigDecimal.ZERO) < 0
                    || warningThreshold.compareTo(BigDecimal.ZERO) < 0
                    || warningThreshold.compareTo(BigDecimal.ONE) > 0) {
                return null;
            }

            return new EditBudgetRequest(limitValue, warningThreshold);
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
        try {
            if (text == null || text.isBlank()) {
                return null;
            }
            return new BigDecimal(text.trim());
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
