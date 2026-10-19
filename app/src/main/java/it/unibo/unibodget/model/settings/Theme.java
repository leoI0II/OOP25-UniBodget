package it.unibo.unibodget.model.settings;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Represents a visual theme used by the application's user interface.
 *
 * <p>
 * A {@code Theme} defines the core visual identity of the UI, including:</p>
 * <ul>
 *     <li>a human-readable name</li>
 *     <li>primary background color</li>
 *     <li>button color</li>
 *     <li>text color</li>
 *     <li>font family and size</li>
 *     <li>whether text is bold</li>
 * </ul>
 *
 * <p>
 * This class is part of the model layer and contains only theme data.
 * It does not depend on JavaFX or other UI-specific APIs.</p>
 */
public final class Theme {

    private static final int HALF = 128;
    private static final int FONT_14 = 14;
    private static final String FONT_ARIAL = "Arial";

    /**
     * Predefined dark theme (UniBodget signature look).
     */
    public static final Theme DARK =
            new Theme(
                    "Dark",
                    new ARGBColor(255, 14, 16, 27),
                    ARGBColor.WHITE,
                    ARGBColor.WHITE,
                    FONT_ARIAL,
                    FONT_14,
                    false
            );

    /**
     * Predefined light theme.
     */
    public static final Theme LIGHT =
            new Theme(
                    "Light",
                    new ARGBColor(255, 245, 247, 251),
                    new ARGBColor(255, 24, 27, 36),
                    new ARGBColor(255, 24, 27, 36),
                    FONT_ARIAL,
                    FONT_14,
                    false
            );

    /**
     * The default theme used when no user preference is available.
     */
    public static final Theme DEFAULT = DARK;

    private final String name;
    private final boolean darkMode;
    private final ARGBColor primaryColor;
    private final ARGBColor buttonColor;
    private final ARGBColor textColor;
    private final String fontFamily;
    private final int fontSize;
    private final boolean boldText;

    /**
     * Creates a new {@code Theme} with explicit values.
     *
     * @param name         the theme name
     * @param primaryColor the primary background or accent color
     * @param buttonColor  the button color
     * @param textColor    the text color
     * @param fontFamily   the font family
     * @param fontSize     the font size (must be positive)
     * @param boldText     whether bold text is enabled
     * @param darkMode     whether dark mode is enabled (optional for backward compatibility)
     */
    @JsonCreator
    public Theme(
        @JsonProperty("name") final String name,
        @JsonProperty("primaryColor") final ARGBColor primaryColor,
        @JsonProperty("buttonColor") final ARGBColor buttonColor,
        @JsonProperty("textColor") final ARGBColor textColor,
        @JsonProperty("fontFamily") final String fontFamily,
        @JsonProperty("fontSize") final int fontSize,
        @JsonProperty("boldText") final boolean boldText,
        @JsonProperty("darkMode") final Boolean darkMode) {

        this.name = Objects.requireNonNull(name);
        this.primaryColor = Objects.requireNonNull(primaryColor);
        this.buttonColor = Objects.requireNonNull(buttonColor);
        this.textColor = Objects.requireNonNull(textColor);
        this.fontFamily = Objects.requireNonNull(fontFamily);

        // Fallback to default size if invalid
        this.fontSize = fontSize > 0 ? fontSize : DEFAULT.fontSize;
        this.boldText = boldText;

        if (darkMode != null) {
            this.darkMode = darkMode;
        } else if ("Dark".equalsIgnoreCase(name)) {
            this.darkMode = true;
        } else if ("Light".equalsIgnoreCase(name)) {
            this.darkMode = false;
        } else {
            final double luminance =
                    0.299 * primaryColor.red()
                    + 0.587 * primaryColor.green()
                    + 0.114 * primaryColor.blue();
            this.darkMode = luminance <= HALF;
        }
    }

    /**
     * Creates a new {@code Theme} with explicit dark mode and values.
     *
     * @param name         the theme name
     * @param darkMode     whether dark mode is enabled
     * @param primaryColor the primary accent color
     * @param buttonColor  the button color
     * @param textColor    the text color
     * @param fontFamily   the font family
     * @param fontSize     the font size (must be positive)
     * @param boldText     whether bold text is enabled
     */
    public Theme(
            final String name,
            final boolean darkMode,
            final ARGBColor primaryColor,
            final ARGBColor buttonColor,
            final ARGBColor textColor,
            final String fontFamily,
            final int fontSize,
            final boolean boldText) {
        this(name, primaryColor, buttonColor, textColor, fontFamily, fontSize, boldText, darkMode);
    }

    /**
     * Creates a new {@code Theme} with explicit values.
     *
     * @param name         the theme name
     * @param primaryColor the primary background color
     * @param buttonColor  the button color
     * @param textColor    the text color
     * @param fontFamily   the font family
     * @param fontSize     the font size (must be positive)
     * @param boldText     whether bold text is enabled
     */
    public Theme(
        final String name,
        final ARGBColor primaryColor,
        final ARGBColor buttonColor,
        final ARGBColor textColor,
        final String fontFamily,
        final int fontSize,
        final boolean boldText) {
        this(name, primaryColor, buttonColor, textColor, fontFamily, fontSize, boldText, null);
    }

    /**
     * Creates a new {@code Theme} with explicit color values
     * and default typography settings.
     *
     * @param name         the theme name
     * @param primaryColor the primary background color
     * @param buttonColor  the button color
     * @param textColor    the text color
     */
    public Theme(
            final String name,
            final ARGBColor primaryColor,
            final ARGBColor buttonColor,
            final ARGBColor textColor
    ) {
        this(name, primaryColor, buttonColor, textColor, FONT_ARIAL, FONT_14, false);
    }

    /**
     * Creates a new {@code Theme} using HEX color strings.
     * The text color is automatically computed for readability.
     *
     * @param name           the theme name
     * @param hexColor       the primary background color in HEX format
     * @param buttonHexColor the button color in HEX format
     */
    public Theme(final String name, final String hexColor, final String buttonHexColor) {
        this(
                name,
                new ARGBColor(hexColor),
                new ARGBColor(buttonHexColor),
                getReadableTextColor(new ARGBColor(hexColor)),
                FONT_ARIAL,
                FONT_14,
                false
        );
    }

    /** 
     * Return theme name.
     * 
     * @return the theme name 
     */
    public String getName() { 
        return name; 
    }

    /** 
     * Return primary background color.
     * 
     * @return the primary background color
     */
    public ARGBColor getPrimaryColor() { 
        return primaryColor; 
    }

    /** 
     * Return button color.
     * 
     * @return the button color 
     */
    public ARGBColor getButtonColor() { 
        return buttonColor; 
    }

    /** 
     * Return text color.
     * 
     * @return the text color 
     */
    public ARGBColor getTextColor() { 
        return textColor; 
    }

    /** 
     * Return font family.
     * 
     * @return the font family 
     */
    public String getFontFamily() { 
        return fontFamily; 
    }

    /** 
     * Return font size.
     * 
     * @return the font size 
     */
    public int getFontSize() { 
        return fontSize; 
    }

    /** 
     * Return if the text is bold.
     * 
     * @return true if bold text is enabled 
     */
    public boolean isBoldText() { 
        return boldText; 
    }

    /**
     * Determines whether this theme represents a dark theme.
     *
     * @return {@code true} if dark mode, {@code false} if light mode
     */
    @JsonProperty("darkMode")
    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Computes whether black or white text provides better readability
     * on top of the given background color.
     *
     * @param color the background color to evaluate
     * @return black or white depending on luminance
     */
    public static ARGBColor getReadableTextColor(final ARGBColor color) {
        final double luminance =
                0.299 * color.red()
                + 0.587 * color.green()
                + 0.114 * color.blue();

        return luminance > HALF ? ARGBColor.BLACK : ARGBColor.WHITE;
    }

    @Override
    public String toString() {
        return "Theme{"
                + "name='" + name + '\''
                + ", darkMode=" + darkMode
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
        return darkMode == theme.darkMode
                && fontSize == theme.fontSize
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
                darkMode,
                primaryColor,
                buttonColor,
                textColor,
                fontFamily,
                fontSize,
                boldText
        );
    }

}
