package it.unibo.unibodget.model.categories;

import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Concrete category used by the application.
 *
 * <p>
 * A category extends the shared base category with application-level metadata:
 * </p>
 * <ul>
 * <li>its origin, either built-in or user-defined,</li>
 * <li>its active state, used for archive/reactivate lifecycle management.</li>
 * </ul>
 *
 * <p>
 * Default categories are always active and cannot be renamed, recolored,
 * archived, or otherwise altered through management operations intended for
 * custom categories.
 * </p>
 */
public final class Category extends BasicCategory {

    /**
     * Built-in food category.
     */
    public static final Category FOOD =
            new Category("Food", new ARGBColor(0xFFFF9800), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in rent category.
     */
    public static final Category RENT =
            new Category("Rent", new ARGBColor(0xFF9C27B0), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in savings category.
     */
    public static final Category SAVINGS =
            new Category("Savings", new ARGBColor(0xFF4CAF50), CategoryType.INCOME, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in transport category.
     */
    public static final Category TRANSPORT =
            new Category("Transport", new ARGBColor(0xFF009688), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in transfer category.
     */
    public static final Category TRANSFER =
            new Category("Transfer", new ARGBColor(0xFF2196F3), CategoryType.TRANSFER, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in investment buy category.
     */
    public static final Category INVESTMENT_BUY =
            new Category("Investment Buy", new ARGBColor(0xFFFFC107), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in investment sell category.
     */
    public static final Category INVESTMENT_SELL =
            new Category("Investment Sell", new ARGBColor(0xFFFFC107), CategoryType.INCOME, CategoryOrigin.DEFAULT, true);

    /**
     * Built-in friend loan category.
     */
    public static final Category FRIEND_LOAN =
            new Category("Friend Loan", new ARGBColor(0xFF795548), CategoryType.FRIEND_LOAN, CategoryOrigin.DEFAULT, true);

    private final CategoryOrigin origin;
    private boolean active;

    /**
     * Creates a category with explicit metadata.
     *
     * @param name
     *            the category name
     * @param color
     *            the category color
     * @param type
     *            the category type
     * @param origin
     *            the origin of the category
     * @param active
     *            whether the category is active
     * @throws NullPointerException
     *             if any mandatory argument is {@code null}
     * @throws IllegalArgumentException
     *             if a default category is created as inactive
     */
    public Category(
            final String name,
            final ARGBColor color,
            final CategoryType type,
            final CategoryOrigin origin,
            final boolean active) {
        super(name, color, type);
        this.origin = Objects.requireNonNull(origin);
        if (origin == CategoryOrigin.DEFAULT && !active) {
            throw new IllegalArgumentException("Default categories must always be active.");
        }
        this.active = active;
    }

    /**
     * Creates a new active custom category.
     *
     * @param name
     *            the category name
     * @param color
     *            the category color
     * @param type
     *            the category type
     */
    public Category(final String name, final ARGBColor color, final CategoryType type) {
        this(name, color, type, CategoryOrigin.CUSTOM, true);
    }

    /**
     * Returns the built-in categories available in every application instance.
     *
     * @return an immutable list of default categories
     */
    public static List<Category> getDefaultCategories() {
        return List.of(
                FOOD,
                RENT,
                SAVINGS,
                TRANSPORT,
                TRANSFER,
                INVESTMENT_BUY,
                INVESTMENT_SELL,
                FRIEND_LOAN
        );
    }

    /**
     * Returns the origin of the category.
     *
     * @return the category origin
     */
    public CategoryOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns whether the category is active.
     *
     * @return {@code true} if active, otherwise {@code false}
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns whether this is a built-in category.
     *
     * @return {@code true} if this category is default, otherwise {@code false}
     */
    public boolean isDefault() {
        return origin == CategoryOrigin.DEFAULT;
    }

    /**
     * Returns whether this is a user-defined category.
     *
     * @return {@code true} if this category is custom, otherwise {@code false}
     */
    public boolean isCustom() {
        return origin == CategoryOrigin.CUSTOM;
    }

    /**
     * Renames a custom category.
     *
     * @param newName
     *            the new category name
     * @throws IllegalStateException
     *             if this category is a default category
     * @throws NullPointerException
     *             if {@code newName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code newName} is blank
     */
    public void rename(final String newName) {
        if (isDefault()) {
            throw new IllegalStateException("Default categories cannot be renamed.");
        }
        setName(newName);
    }

    /**
     * Changes the color of a custom category.
     *
     * @param newColor
     *            the new color
     * @throws IllegalStateException
     *             if this category is a default category
     * @throws NullPointerException
     *             if {@code newColor} is {@code null}
     */
    public void recolor(final ARGBColor newColor) {
        if (isDefault()) {
            throw new IllegalStateException("Default categories cannot be recolored.");
        }
        setColorHex(newColor);
    }

    /**
     * Archives a custom category.
     *
     * @throws IllegalStateException
     *             if this category is a default category
     */
    public void archive() {
        if (isDefault()) {
            throw new IllegalStateException("Default categories cannot be archived.");
        }
        this.active = false;
    }

    /**
     * Reactivates a custom category.
     *
     * @throws IllegalStateException
     *             if this category is a default category
     */
    public void reactivate() {
        if (isDefault()) {
            throw new IllegalStateException("Default categories are always active.");
        }
        this.active = true;
    }

    @Override
    public String toString() {
        return "Category{name='%s', color='%s', type='%s', origin='%s', active='%s'}"
                .formatted(getName(), getColorHex().toHexString(), getType(), origin, active);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Category other)) {
            return false;
        }
        return super.equals(other)
                && active == other.active
                && origin == other.origin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), origin, active);
    }
}