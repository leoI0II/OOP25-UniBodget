package it.unibo.unibodget.model.utils;

import javafx.scene.paint.Color;

/**
 * An immutable representation of a color in the ARGB (Alpha, Red, Green, Blue) color space.
 * Each component is an 8-bit value ranging from 0 to 255.
 *
 * @param alpha the alpha (transparency) component, where 0 is fully transparent and 255 is fully opaque
 * @param red   the red color component (0-255)
 * @param green the green color component (0-255)
 * @param blue  the blue color component (0-255)
 */
public record ARGBColor(int alpha, int red, int green, int blue) {

    private static final int MIN = 0;
    private static final int MAX = 255;

    public static final ARGBColor TRANSPARENT = new ARGBColor(0, 0, 0, 0);
    public static final ARGBColor BLACK = new ARGBColor(0xFF, 0, 0, 0);
    public static final ARGBColor WHITE = new ARGBColor(0xFF, 0xFF, 0xFF, 0xFF);
    public static final ARGBColor RED = new ARGBColor(0xFF, 0xFF, 0, 0);
    public static final ARGBColor GREEN = new ARGBColor(0xFF, 0, 0xFF, 0);
    public static final ARGBColor BLUE = new ARGBColor(0xFF, 0, 0, 0xFF);
    public static final ARGBColor YELLOW = new ARGBColor(0xFF, 0xFF, 0xFF, 0);
    public static final ARGBColor CYAN = new ARGBColor(0xFF, 0, 0xFF, 0xFF);
    public static final ARGBColor MAGENTA = new ARGBColor(0xFF, 0xFF, 0, 0xFF);
    public static final ARGBColor GRAY = new ARGBColor(0xFF, 0x80, 0x80, 0x80);
    public static final ARGBColor DARK_GRAY = new ARGBColor(0xFF, 0x40, 0x40, 0x40);
    public static final ARGBColor LIGHT_GRAY = new ARGBColor(0xFF, 0xC0, 0xC0, 0xC0);


    /**
     * Compact constructor that validates all color components upon instantiation.
     *
     * @throws IllegalArgumentException if any of the components are outside the valid 0-255 range
     */
    public ARGBColor {
        checkValue(alpha, "Alpha");
        checkValue(red, "Red");
        checkValue(green, "Green");
        checkValue(blue, "Blue");
    }

    /**
     * Validates a single color component.
     *
     * @param value the numeric value of the component
     * @param name  the name of the component (used for the exception message)
     * @throws IllegalArgumentException if the value is not between 0 and 255
     */
    private static void checkValue(final int value, final String name) {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(name + " value must be between 0 and 255.");
        }
    }

    /**
     * Creates a fully opaque color using the specified red, green, and blue components.
     * The alpha component is automatically set to 255.
     *
     * @param red   the red color component (0-255)
     * @param green the green color component (0-255)
     * @param blue  the blue color component (0-255)
     */
    public ARGBColor(int red, int green, int blue) {
        this(255, red, green, blue);
    }

    /**
     * Creates a color from a 32-bit packed integer in ARGB format.
     *
     * @param argb the packed integer containing alpha in the highest byte, followed by red, green, and blue
     */
    public ARGBColor(int argb) {
        this((argb >> 24) & 0xFF, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    /**
     * Creates a color by parsing a hexadecimal string.
     * The string can be in either 6-character ({@code #RRGGBB} or {@code RRGGBB})
     * or 8-character ({@code #AARRGGBB} or {@code AARRGGBB}) format.
     *
     * @param hex the hexadecimal string representing the color
     * @throws IllegalArgumentException if the string format or length is invalid
     * @throws NumberFormatException    if the string contains non-hexadecimal characters
     */
    public ARGBColor(String hex) {
        this(parseHexToInt(hex));
    }

    /**
     * Parses a hexadecimal color string into a 32-bit integer.
     * If the string lacks an alpha channel (6 characters), it defaults to fully opaque (FF).
     *
     * @param hex the raw hexadecimal string
     * @return the parsed 32-bit ARGB integer
     * @throws IllegalArgumentException if the string is not exactly 6 or 8 characters long (excluding the '#')
     */
    private static int parseHexToInt(String hex) {
        String clean = hex.replaceFirst("^#", "");
        if (clean.length() != 6 && clean.length() != 8) {
            throw new IllegalArgumentException(String.format("Hex color string \"%s\" must be in the format #RRGGBB or #AARRGGBB.", hex));
        }
        if (clean.length() == 6) {
            clean = "FF" + clean;
        }
        return Integer.parseUnsignedInt(clean, 16);
    }

    /**
     * Returns the string representation of this color in an 8-character hexadecimal format.
     *
     * @return a formatted string in the form of {@code #AARRGGBB} (e.g., {@code #FFFF0000} for pure opaque red)
     */
    public String toHexString() {
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
    }

    /**
     * Converts this ARGBColor into a JavaFX Color instance.
     *
     * @return a JavaFX Color with the same ARGB components
     */
    public Color toFXColor() {
        return Color.rgb(
            this.red,
            this.green,
            this.blue,
            this.alpha / 255.0
        );
    }
    
}