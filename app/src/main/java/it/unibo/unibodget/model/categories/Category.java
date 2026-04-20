package it.unibo.unibodget.model.categories;

/**
 * Represents a transaction category (e.g., Food, Rent, Transport).
 * 
 * Each category has:
 * - a readable name
 * - a color used for UI visualization, stored as a HEX string
 * - a high-level type that classifies the category
 *
 * Categories are value objects: two categories are considered equal
 * if both their name and color match.
 */
public final class Category extends BasicCategory {

    /**
     * Creates a new Category with the given name and HEX color.
     *
     * @param name      the descriptive name of the category
     * @param colorHex  the color associated with the category
     * @param type      the high-level classification of the category
     */
    public Category(String name, String colorHex, CategoryType type) {
        super(name, colorHex, type);
    }

}
