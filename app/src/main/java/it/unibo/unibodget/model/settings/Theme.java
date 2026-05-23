package it.unibo.unibodget.model.settings;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Represents a visual theme used by the application's user interface.
 *
 * <p>A {@code Theme} defines the core visual identity of the UI, including:
 * a human-readable name, a primary background color, a button color,
 * a text color, and typographic settings such as font family, size,
 * and weight.</p>
 *
 * <p>This class belongs to the model layer and contains only theme data.
 * It does not depend on JavaFX or other UI-specific APIs.</p>
 */
public final class Theme {

    /**
     * The default theme used when no user preference is available.
     */
    public static final Theme DEFAULT =
            new Theme(
                    "Light",
                    ARGBColor.WHITE,
                    ARGBColor.LIGHT_GRAY,
                    getReadableTextColor(ARGBColor.WHITE),
                    "Arial",
                    14,
                    false
            );

    private final String name;
    private final ARGBColor primaryColor;
    private final ARGBColor buttonColor;
    private final ARGBColor textColor;
    private final String fontFamily;
    private final int fontSize;
    private final boolean boldText;

    /**
     * Creates a new {@code Theme} with explicit values.
     *
     * @param name the theme name
     * @param primaryColor the primary background color
     * @param buttonColor the button color
     * @param textColor the text color
     * @param fontFamily the font family
     * @param fontSize the font size
     * @param boldText whether bold text is enabled
     */
    public Theme(
            final String name,
            final ARGBColor primaryColor,
            final ARGBColor buttonColor,
            final ARGBColor textColor,
            final String fontFamily,
            final int fontSize,
            final boolean boldText
    ) {
        this.name = Objects.requireNonNull(name);
        this.primaryColor = Objects.requireNonNull(primaryColor);
        this.buttonColor = Objects.requireNonNull(buttonColor);
        this.textColor = Objects.requireNonNull(textColor);
        this.fontFamily = Objects.requireNonNull(fontFamily);

        if (fontSize <= 0) {
            throw new IllegalArgumentException("Font size must be positive");
        }
        this.fontSize = fontSize;
        this.boldText = boldText;
    }

    /**
     * Creates a new {@code Theme} with explicit color values
     * and default typography settings.
     *
     * @param name the theme name
     * @param primaryColor the primary background color
     * @param buttonColor the button color
     * @param textColor the text color
     */
    public Theme(
            final String name,
            final ARGBColor primaryColor,
            final ARGBColor buttonColor,
            final ARGBColor textColor
    ) {
        this(name, primaryColor, buttonColor, textColor, "Arial", 14, false);
    }

    /**
     * Creates a new {@code Theme} using HEX color strings.
     * The text color is automatically computed for readability.
     *
     * @param name the theme name
     * @param hexColor the primary background color in HEX format
     * @param buttonHexColor the button color in HEX format
     */
    public Theme(final String name, final String hexColor, final String buttonHexColor) {
        this(
                name,
                new ARGBColor(hexColor),
                new ARGBColor(buttonHexColor),
                getReadableTextColor(new ARGBColor(hexColor)),
                "Arial",
                14,
                false
        );
    }

    public String getName() {
        return name;
    }

    public ARGBColor getPrimaryColor() {
        return primaryColor;
    }

    public ARGBColor getButtonColor() {
        return buttonColor;
    }

    public ARGBColor getTextColor() {
        return textColor;
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

    /**
     * Computes whether black or white text provides better readability
     * on top of the given background color.
     *
     * @param color the background color to evaluate
     * @return black or white depending on readability
     */
    public static ARGBColor getReadableTextColor(final ARGBColor color) {
        final double luminance =
                0.299 * color.red()
                + 0.587 * color.green()
                + 0.114 * color.blue();

        return luminance > 128 ? ARGBColor.BLACK : ARGBColor.WHITE;
    }

    @Override
    public String toString() {
        return "Theme{"
                + "name='" + name + '\''
                + ", primaryColor=" + primaryColor
                + ", buttonColor=" + buttonColor
                + ", textColor=" + textColor
                + ", fontFamily='" + fontFamily + '\''
                + ", fontSize=" + fontSize
                + ", boldText=" + boldText
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Theme)) {
            return false;
        }
        final Theme theme = (Theme) o;
        return fontSize == theme.fontSize
                && boldText == theme.boldText
                && Objects.equals(name, theme.name)
                && Objects.equals(primaryColor, theme.primaryColor)
                && Objects.equals(buttonColor, theme.buttonColor)
                && Objects.equals(textColor, theme.textColor)
                && Objects.equals(fontFamily, theme.fontFamily);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                primaryColor,
                buttonColor,
                textColor,
                fontFamily,
                fontSize,
                boldText
        );
    }
}