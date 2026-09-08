package it.unibo.unibodget.model.categories;

import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Abstract base class for all category types.
 *
 * <p>
 * Defines the minimal shared structure required to represent a transaction
 * category in the application.
 * </p>
 *
 * <p>
 * Every category has:
 * </p>
 * <ul>
 * <li>a human-readable name,</li>
 * <li>a display color,</li>
 * <li>a high-level type describing its financial meaning.</li>
 * </ul>
 *
 * <p>
 * Concrete subclasses may enrich this base structure with application-specific
 * metadata such as origin, lifecycle state, or persistence-related details.
 * </p>
 */
public abstract class BasicCategory {

    private String name;
    private ARGBColor color;
    private final CategoryType type;

    /**
     * Creates a new category base object.
     *
     * @param name
     *            the category name; must not be {@code null} or blank
     * @param color
     *            the display color; must not be {@code null}
     * @param type
     *            the category type; must not be {@code null}
     * @throws NullPointerException
     *             if any mandatory argument is {@code null}
     * @throws IllegalArgumentException
     *             if the category name is blank
     */
    public BasicCategory(
            final String name,
            final ARGBColor color,
            final CategoryType type) {
        this.name = requireValidName(name);
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

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BasicCategory other)) {
            return false;
        }
        return this.name.equals(other.name)
                && this.color.equals(other.color)
                && this.type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.color, this.type);
    }
}