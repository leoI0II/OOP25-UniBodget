package it.unibo.unibodget.model.settings;

import java.util.Objects;

/**
 * Represents a visual theme used by the application UI.
 * A theme defines a human‑readable name and a primary color in HEX format.
 */
public final class Theme {

    public static final Theme DEFAULT = new Theme("Light", "#FFFFFF");
    private String name;
    private String primaryHex;

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
    public Theme(String name, String primaryHex) {
        this.name = Objects.requireNonNull(name);
        this.primaryHex = Objects.requireNonNull(primaryHex);
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
    public String getPrimaryHex() {
        return primaryHex;
    }

    @Override
    public String toString() {
        return "Theme{name='" + name + "', primaryHex='" + primaryHex + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme)) return false;
        Theme t = (Theme) o;
        return name.equals(t.name) && primaryHex.equals(t.primaryHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, primaryHex);
    }
}
