package it.unibo.unibodget.model.categories;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Abstract base class for all category types.
 *
 * <p>
 * Defines the minimal shared structure for representing
 * a transaction category, consisting of:
 * - a readable name
 * - a color expressed as a HEX string
 * - a high-level type (income, expense, loan)
 *
 * <p>
 * Concrete implementations may represent:
 * - user‑defined categories loaded from JSON (see {@link Category})
 * - predefined categories exposed through an enum (see {@link CategoryPreset})
 *
 * <p>
 * This class behaves as a value object: equality is based on both name and color.
 */
public abstract class AbstractCategory {

    private String name;
    private ARGBColor color;
    private final CategoryType type;

    /**
     * Creates a new category base object.
     *
     * @param name      the descriptive name of the category
     * @param color     the color associated with the category
     * @param type      the high-level classification of the category
     */
    public AbstractCategory(final String name, 
                        final ARGBColor color,
                        final CategoryType type) {
        this.name = Objects.requireNonNull(name);
        this.color = Objects.requireNonNull(color);
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Returns the category name.
     *
     * @return the category name, never {@code null}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the category color.
     *
     * @return the category color, never {@code null}
     */
    public ARGBColor getColorHex() {
        return this.color;
    }

    /**
     * Returns the category type.
     *
     * @return the category type, never {@code null}
     */
    public CategoryType getType() {
        return this.type;
    }

    /**
     * Updates the category name.
     *
     * <p>
     * This method is intentionally protected so that subclasses can enforce
     * domain rules before exposing rename operations publicly.
     * </p>
     *
     * @param name
     *            the new category name; must not be {@code null} or blank
     * @throws NullPointerException
     *             if {@code name} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code name} is blank
     */
    protected void setName(final String name) {
        this.name = requireValidName(name);
    }

    /**
     * Updates the category color.
     *
     * <p>
     * This method is intentionally protected so that subclasses can enforce
     * domain rules before exposing recolor operations publicly.
     * </p>
     *
     * @param color
     *            the new color; must not be {@code null}
     * @throws NullPointerException
     *             if {@code color} is {@code null}
     */
    protected void setColorHex(final ARGBColor color) {
        this.color = Objects.requireNonNull(color);
    }

    /**
     * Validates and normalizes a category name.
     *
     * @param name
     *            the raw category name
     * @return the trimmed validated name
     * @throws NullPointerException
     *             if {@code name} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code name} is blank
     */
    private static String requireValidName(final String name) {
        Objects.requireNonNull(name);
        final String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be blank.");
        }
        return normalized;
    }

    // / ** Returns a string representation of the category.
    //  *
    //  * @return a readable string for the category
    //  */

    @Override
    public String toString() {
        return "Category{name='" 
                + this.name 
                + "', color='" 
                + this.color.toHexString() 
                + "', type='" 
                + this.type 
                + "'}";
    }

    /**
     * Checks if this category is equal to another object.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    // @Override
    // public String toString() {
    //     return "Category{name='"
    //             + this.name
    //             + "', color='"
    //             + this.color.toHexString()
    //             + "', type='"
    //             + this.type
    //             + "'}";
    // }

    // @Override
    // public boolean equals(final Object object) {
    //     if (this == object) {
    //         return true;
    //     }
    //     if (!(object instanceof BasicCategory other)) {
    //         return false;
    //     }
    //     return this.name.equals(other.name)
    //             && this.color.equals(other.color)
    //             && this.type == other.type;
    @Override 
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractCategory)) {
            return false;
        }
        final AbstractCategory c = (AbstractCategory) o;
        return this.name.equals(c.name) && this.color.equals(c.color) && this.type.equals(c.type);
    }

    /**
     * Returns the hash code for this category.
     *
     * @return the hash code based on name, color, and type
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.color, this.type);
    }

}
