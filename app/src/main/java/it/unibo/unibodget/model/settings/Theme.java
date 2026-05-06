package it.unibo.unibodget.model.settings;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Represents a visual theme used by the application UI.
 * A theme defines a human‑readable name and a primary color in HEX format.
 */
public final class Theme {

    public static final Theme DEFAULT = new Theme("Light", ARGBColor.WHITE);

    private String name;
    private ARGBColor primaryColor;

    /**
     *  Empty constructor required by the generic JSON parser
     */
    public Theme() {

    }

    /**
     * Creates a new Theme with the given name and primary color.
     *
     * @param name       the theme name 
     *                   must not be null
     * @param primaryHex the primary HEX color 
     *                   must not be null
     */
    public Theme(String name, ARGBColor primaryColor) {
        this.name = Objects.requireNonNull(name);
        this.primaryColor = Objects.requireNonNull(primaryColor);
    }

    /**
     * Creates a new Theme from a HEX string (#RRGGBB or #AARRGGBB).
     */
    public Theme(String name, String hexColor) {
        this(name, new ARGBColor(hexColor));
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
