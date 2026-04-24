package it.unibo.unibodget.model.categories;

import java.util.List;

import it.unibo.unibodget.model.utils.ARGBColor;

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

    private final static Category FOOD = new Category("Food", new ARGBColor(0xFFFF9800), CategoryType.EXPENSE);
    private final static Category RENT = new Category("Rent", new ARGBColor(0xFF9C27B0), CategoryType.EXPENSE);
    private final static Category SAVINGS = new Category("Savings", new ARGBColor(0xFF4CAF50), CategoryType.INCOME);
    private final static Category TRANSPORT = new Category("Transport", new ARGBColor(0xFF009688), CategoryType.EXPENSE);
    private final static Category TRANSFER = new Category("Transfer", new ARGBColor(0xFF2196F3), CategoryType.TRANSFER);

    /**
      * Returns a list of default categories to be used when the application is first launched.
      *
      * @return a list of default {@link Category} instances
      */
    public static List<Category> getDefaultCategories() {
        return List.of(FOOD, RENT, SAVINGS, TRANSPORT, TRANSFER);
    }

    /**
     * Creates a new Category with the given name and HEX color.
     *
     * @param name      the descriptive name of the category
     * @param colorHex  the color associated with the category
     * @param type      the high-level classification of the category
     */
    public Category(final String name, final ARGBColor color, final CategoryType type) {
        super(name, color, type);
    }

}
