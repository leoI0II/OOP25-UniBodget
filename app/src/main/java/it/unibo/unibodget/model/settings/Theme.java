package it.unibo.unibodget.model.settings;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Represents a visual theme used by the application's user interface.
 * 
 * A {@code Theme} defines the core visual identity of the UI, including:
 * - a human‑readable name
 * - a primary background color
 * - a button color
 * - a text color (automatically computed when not provided)
 * - typographic settings such as font family, size, and weight
 * 
 * Colors are represented using {@link ARGBColor}, ensuring type‑safety and
 * consistent color manipulation across the application.
 */
public final class Theme {

    /**
     * The default theme used when no user preference is available.
     * 
     * - black primary background<
     * - light‑gray buttons
     * - text color automatically computed for readability
     */
    public static final Theme DEFAULT =
            new Theme(
                    "Light",
                    ARGBColor.WHITE,
                    ARGBColor.LIGHT_GRAY,
                    getReadableTextColor(ARGBColor.BLACK)
            );

    private String name;
    private ARGBColor primaryColor;
    private ARGBColor buttonColor;
    private ARGBColor textColor;

    private String fontFamily = "Arial";
    private int fontSize = 14;
    private boolean boldText = false;

    /**
     * Creates a new {@code Theme} with explicit color values.
     *
     * @param name         the theme name, must not be {@code null}
     * @param primaryColor the primary background color, must not be {@code null}
     * @param buttonColor  the button color, must not be {@code null}
     * @param textColor    the text color, must not be {@code null}
     */
    public Theme(String name, ARGBColor primaryColor, ARGBColor buttonColor, ARGBColor textColor) {
        this.name = Objects.requireNonNull(name);
        this.primaryColor = Objects.requireNonNull(primaryColor);
        this.buttonColor = Objects.requireNonNull(buttonColor);
        this.textColor = Objects.requireNonNull(textColor);
    }

    /**
     * Creates a new {@code Theme} using HEX color strings.
     * 
     * The text color is automatically computed based on the primary color's
     * luminance to ensure optimal readability.
     *
     * @param name           the theme name
     * @param hexColor       the primary background color in HEX format
     * @param buttonHexColor the button color in HEX format
     */
    public Theme(String name, String hexColor, String buttonHexColor) {
        this(
                name,
                new ARGBColor(hexColor),
                new ARGBColor(buttonHexColor),
                getReadableTextColor(new ARGBColor(hexColor))
        );
    }

    /**
     * Returns the theme name.
     *
     * @return the name of the theme
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the primary background color of the theme.
     *
     * @return the primary {@link ARGBColor}
     */
    public ARGBColor getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Returns the button color of the theme.
     *
     * @return the button {@link ARGBColor}
     */
    public ARGBColor getButtonColor() {
        return buttonColor;
    }

    /**
     * Returns the text color of the theme.
     *
     * @return the text {@link ARGBColor}
     */
    public ARGBColor getTextColor() {
        return textColor;
    }

    /**
     * Computes whether black or white text provides better readability
     * on top of the given background color.
     * 
     * The method uses the standard luminance formula:
     * luminance = 0.299 * R + 0.587 * G + 0.114 * B
     * 
     * If the luminance is greater than 128, black text is preferred;
     * otherwise white text is used.
     *
     * @param color the background color to evaluate
     * @return {@link ARGBColor#BLACK} or {@link ARGBColor#WHITE}
     */
    public static ARGBColor getReadableTextColor(ARGBColor color) {
        double luminance =
                0.299 * color.red() +
                0.587 * color.green() +
                0.114 * color.blue();

        return luminance > 128 ? ARGBColor.BLACK : ARGBColor.WHITE;
    }

    /**
     * Converts the theme's typographic settings into a JavaFX {@link javafx.scene.text.Font}.
     *
     * @return a JavaFX font instance matching the theme's typography
     */
    public javafx.scene.text.Font toFXFont() {
        return javafx.scene.text.Font.font(
                fontFamily,
                boldText ? javafx.scene.text.FontWeight.BOLD : javafx.scene.text.FontWeight.NORMAL,
                fontSize
        );
    }

    /**
     * Returns the font family used by this theme.
     *
     * @return the font family name
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * Returns the font size used by this theme.
     *
     * @return the font size in points
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Indicates whether the theme uses bold text.
     *
     * @return {@code true} if bold text is enabled, {@code false} otherwise
     */
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
