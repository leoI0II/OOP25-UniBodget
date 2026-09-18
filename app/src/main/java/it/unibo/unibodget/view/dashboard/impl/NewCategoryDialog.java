package it.unibo.unibodget.view.dashboard.impl;

import java.util.Objects;

import it.unibo.unibodget.model.categories.CategoryType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Dialog used to create a new custom category inside the transaction flow.
 *
 * <p>
 * The category type is fixed by the transaction flow that opened the dialog, so
 * the user only has to provide the category name and visually choose a color.
 * </p>
 */
public final class NewCategoryDialog extends Dialog<NewCategoryRequest> {

    private static final String DEFAULT_COLOR = "#4F46E5";

    private final TextField nameField;
    private final ColorPicker colorPicker;
    private final Region colorPreview;
    private final Label hexValueLabel;

    /**
     * Creates a new dialog for custom-category creation.
     *
     * @param fixedType
     *            the category type imposed by the current transaction flow
     */
    public NewCategoryDialog(final CategoryType fixedType) {
        Objects.requireNonNull(fixedType, "fixedType must not be null");

        setTitle("Create New Category");
        setHeaderText("Create a new " + formatCategoryType(fixedType) + " category");

        final ButtonType createButtonType = new ButtonType("Create", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        this.nameField = new TextField();
        this.colorPicker = new ColorPicker(Color.web(DEFAULT_COLOR));
        this.colorPreview = new Region();
        this.hexValueLabel = new Label(DEFAULT_COLOR);

        nameField.setPromptText("Category name");

        colorPreview.setMinSize(42, 42);
        colorPreview.setPrefSize(42, 42);
        colorPreview.setMaxSize(42, 42);
        updateColorPreview(colorPicker.getValue());

        colorPicker.valueProperty().addListener((obs, oldColor, newColor) -> updateColorPreview(newColor));

        final GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Type:"), 0, 0);
        grid.add(new Label(formatCategoryType(fixedType)), 1, 0);

        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);

        grid.add(new Label("Preview:"), 0, 3);
        grid.add(colorPreview, 1, 3);

        grid.add(new Label("Hex value:"), 0, 4);
        grid.add(hexValueLabel, 1, 4);

        getDialogPane().setContent(grid);

        final Node createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldValue, newValue) -> {
            final boolean validName = newValue != null && !newValue.trim().isEmpty();
            createButton.setDisable(!validName);
        });

        setResultConverter(buttonType -> {
            if (!Objects.equals(buttonType, createButtonType)) {
                return null;
            }

            final String name = nameField.getText() == null
                    ? ""
                    : nameField.getText().trim();

            if (name.isEmpty()) {
                return null;
            }

            return new NewCategoryRequest(
                    name,
                    toHexColor(colorPicker.getValue())
            );
        });
    }

    /**
     * Updates the visual preview and hexadecimal label using the currently
     * selected color.
     *
     * @param color
     *            the selected JavaFX color
     */
    private void updateColorPreview(final Color color) {
        final String hexColor = toHexColor(color);
        colorPreview.setStyle(buildPreviewStyle(hexColor));
        hexValueLabel.setText(hexColor);
    }

    /**
     * Builds the style used to render the visual color preview.
     *
     * @param hexColor
     *            the selected color in hexadecimal format
     * @return the CSS style string for the preview node
     */
    private static String buildPreviewStyle(final String hexColor) {
        return "-fx-background-color: " + hexColor + ";"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: rgba(255,255,255,0.25);"
                + "-fx-border-radius: 10;"
                + "-fx-border-width: 1;";
    }

    /**
     * Converts a JavaFX color into the canonical {@code #RRGGBB} format.
     *
     * @param color
     *            the selected JavaFX color
     * @return the normalized hexadecimal RGB string
     */
    private static String toHexColor(final Color color) {
        final int red = (int) Math.round(color.getRed() * 255);
        final int green = (int) Math.round(color.getGreen() * 255);
        final int blue = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /**
     * Formats a category type for UI display.
     *
     * @param type
     *            the type to format
     * @return a user-friendly label
     */
    private static String formatCategoryType(final CategoryType type) {
        return switch (type) {
            case INCOME -> "income";
            case EXPENSE -> "expense";
            case TRANSFER -> "transfer";
            case FRIEND_LOAN -> "friend loan";
        };
    }
}