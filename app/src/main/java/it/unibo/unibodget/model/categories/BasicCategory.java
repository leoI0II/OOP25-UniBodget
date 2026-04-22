package it.unibo.unibodget.model.categories;

import java.util.Objects;

/**
 * Abstract base class for all category types.
 *
 * Defines the minimal shared structure for representing
 * a transaction category, consisting of:
 * - a readable name
 * - a color expressed as a HEX string
 * - a high-level type (income, expense, loan)
 *
 * Concrete implementations may represent:
 * - user‑defined categories loaded from JSON (see {@link Category})
 * - predefined categories exposed through an enum (see {@link CategoryPreset})
 *
 * This class behaves as a value object: equality is based on both name and color.
 */
public abstract class BasicCategory {

    private String name;
    private String colorHex;
    private CategoryType type;
    //private ARGBColor color;

    /**
     * Creates a new Category with the given name and HEX color.
     *
     * @param name      the descriptive name of the category
     * @param colorHex  the color associated with the category
     * @param type      the high-level classification of the category
     */
    public BasicCategory(String name, 
                        String colorHex,
                        CategoryType type
                        /*ARGBColor color*/) {
        this.name = Objects.requireNonNull(name);
        this.colorHex = Objects.requireNonNull(colorHex);
        this.type = Objects.requireNonNull(type);
        //this.color = Objects.requireNonNull(color);
    }

    /**
     * Returns the name of the category.
     *
     * @return the category name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the HEX color associated with the category.
     *
     * @return the category color in HEX format
     */
    public String getColorHex() {
        return this.colorHex;
    }

    /**
     * Returns the HEX color associated with the category.
     *
     * @return the category color in HEX format
     */
    public CategoryType getType() {
        return this.type;
    }

    /*
    public ARGBColor getARGBColor() {
        return color;
    }
    */

    @Override
    public String toString() {
        return "Category{name='" + this.name + 
                "', colorHex='" + this.colorHex + 
                "', type='" + this.type + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicCategory)) {
            return false;
        }
        BasicCategory c = (BasicCategory) o;
        return this.name.equals(c.name) && 
                this.colorHex.equals(c.colorHex) && 
                this.type.equals(c.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.colorHex, this.type);
    }
    
}
