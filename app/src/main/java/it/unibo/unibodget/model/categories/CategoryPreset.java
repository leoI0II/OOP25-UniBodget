package it.unibo.unibodget.model.categories;

/**
 * Enumeration of predefined used transaction categories.
 *
 * Each constant defines:
 * - a default name
 * - a default HEX color
 *
 * These presets provide convenient, ready‑to‑use categories for the application.
 * They can be converted into concrete {@link Category} instances through
 * the {@link #toCategory()} method.
 */
public enum CategoryPreset {

    FOOD("Food", "#FF9800", CategoryType.EXPENSE),
    RENT("Shopping", "#9C27B0", CategoryType.EXPENSE),
    SAVINGS("Savings", "#4CAF50", CategoryType.INCOME),
    TRANSPORT("Transport", "#009688", CategoryType.EXPENSE);

    private final String name;
    private final String colorHex;
    private final CategoryType type;

    /**
     * Creates a predefined category with the given name and HEX color.
     *
     * @param name      the display name of the category
     * @param colorHex  the default color associated with the category
     * @param type      the high-level classification of the category
     */
    CategoryPreset(String name, String colorHex, CategoryType type) {
        this.name = name;
        this.colorHex = colorHex;
        this.type = type;
    }

    /**
     * Converts this predefined category into a concrete {@link Category} instance.
     * This is useful when a preset needs to be treated as a regular category.
     *
     * @return a new Category object with the preset's name and color
     */
    public Category toCategory() {
        return new Category(this.name, this.colorHex, this.type);
    }
}
