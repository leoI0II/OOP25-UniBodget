package it.unibo.unibodget.model.settings;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Represents a visual theme used by the application UI.
 * A theme defines a human‑readable name and a primary color in HEX format.
 */
public final class Theme {

    public static final Theme DEFAULT = new Theme("Light", ARGBColor.WHITE, new ARGBColor("#FF4CAF50"));

    private String name;
    private ARGBColor primaryColor;
    private ARGBColor buttonColor;

    private String fontFamily = "Arial";
    private int fontSize = 14;
    private boolean boldText = false;

    /**
     * Creates a new Theme with the given name and primary color.
     *
     * @param name       the theme name 
     *                   must not be null
     * @param primaryHex the primary HEX color 
     *                   must not be null
     * @param buttonHex  the button HEX color
     *                   must not be null
     */
    public Theme(String name, ARGBColor primaryColor, ARGBColor buttonColor) {
        this.name = Objects.requireNonNull(name);
        this.primaryColor = Objects.requireNonNull(primaryColor);
        this.buttonColor = Objects.requireNonNull(buttonColor);
    }

    /**
     * Creates a new Theme from a single HEX color (#RRGGBB or #AARRGGBB).
     * The given color becomes the primary background color, while the button
     * color defaults to a standard accent color.
     */
    public Theme(String name, String hexColor, String buttonHexColor) {
        this(
            name,
            new ARGBColor(hexColor),
            new ARGBColor(buttonHexColor)
        );
    }

    /**
     * Returns the theme name.
     *
     * @return the theme name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the primary HEX color associated with the theme.
     *
     * @return the primary HEX color
     */
    public ARGBColor getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Returns the button HEX color associated with the theme.
     *
     * @return the button HEX color
     */
    public ARGBColor getButtonColor() {
        return buttonColor;
    }

    /**
     * Computes whether black or white text is more readable on top of the given color.
     * Uses the standard luminance formula:
     *     luminance = 0.299*R + 0.587*G + 0.114*B
     *
     * If luminance > 128 → return BLACK, else WHITE.
     *
     * @param color the background color
     * @return ARGBColor.BLACK or ARGBColor.WHITE
     */
    public ARGBColor getReadableTextColor(ARGBColor color) {
        double luminance =
                0.299 * color.red() +
                0.587 * color.green() +
                0.114 * color.blue();

        return luminance > 128 ? ARGBColor.BLACK : ARGBColor.WHITE;
    }

    public javafx.scene.text.Font toFXFont() {
        return javafx.scene.text.Font.font(
                fontFamily,
                boldText ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL,
                fontSize
        );
    }

    public String getFontFamily() { 
        return fontFamily; 
    }

    public int getFontSize() { 
        return fontSize; 
    }

    public boolean isBoldText() { 
        return boldText; 
    }

    @Override
    public String toString() {
        return "Theme{name='" + name + "', primaryColor='" + primaryColor + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme)) return false;
        Theme t = (Theme) o;
        return name.equals(t.name) && primaryColor.equals(t.primaryColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, primaryColor);
    }
}
