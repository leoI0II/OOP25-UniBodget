package it.unibo.unibodget.model.categories;

import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Concrete category used by the application.
 *
 * <p>
 * A category extends the shared base category with application-level metadata:
 * <ul>
 *   <li>origin ({@link CategoryOrigin#DEFAULT} or {@link CategoryOrigin#CUSTOM})</li>
 *   <li>active flag for archive/reactivate behaviour</li>
 * </ul>
 */
public final class Category extends BasicCategory {

    public static final Category FOOD =
            new Category("Food", new ARGBColor(0xFFFF9800), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    public static final Category RENT =
            new Category("Rent", new ARGBColor(0xFF9C27B0), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    public static final Category SAVINGS =
            new Category("Savings", new ARGBColor(0xFF4CAF50), CategoryType.INCOME, CategoryOrigin.DEFAULT, true);

    public static final Category TRANSPORT =
            new Category("Transport", new ARGBColor(0xFF009688), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    public static final Category TRANSFER =
            new Category("Transfer", new ARGBColor(0xFF2196F3), CategoryType.TRANSFER, CategoryOrigin.DEFAULT, true);

    public static final Category INVESTMENT_BUY =
            new Category("Investment Buy", new ARGBColor(0xFFFFC107), CategoryType.EXPENSE, CategoryOrigin.DEFAULT, true);

    public static final Category INVESTMENT_SELL =
            new Category("Investment Sell", new ARGBColor(0xFFFFC107), CategoryType.INCOME, CategoryOrigin.DEFAULT, true);

    public static final Category FRIEND_LOAN =
            new Category("Friend Loan", new ARGBColor(0xFF795548), CategoryType.FRIEND_LOAN, CategoryOrigin.DEFAULT, true);

    public static final Category BANK_LOAN =
            new Category("Bank Loan", new ARGBColor(0xFF607D8B), CategoryType.BANK_LOAN, CategoryOrigin.DEFAULT, true);

    private final CategoryOrigin origin;
    private boolean active;

    /**
     * Creates a new {@code Category} with full control over all fields.
     *
     * @param name    the category name; must not be {@code null}
     * @param color   the category color; must not be {@code null}
     * @param type    the category type; must not be {@code null}
     * @param origin  whether this is a default or custom category;
     *                must not be {@code null}
     * @param active  {@code true} if the category is active
     * @throws NullPointerException     if {@code origin} is {@code null}
     * @throws IllegalArgumentException if a {@link CategoryOrigin#DEFAULT} category
     *                                  is created with {@code active} set to {@code false}
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
     * Convenience constructor for creating an active custom category.
     *
     * @param name   the category name; must not be {@code null}
     * @param color  the category color; must not be {@code null}
     * @param type   the category type; must not be {@code null}
     */
    public Category(final String name, final ARGBColor color, final CategoryType type) {
        this(name, color, type, CategoryOrigin.CUSTOM, true);
    }

    /**
     * Returns the built-in categories available in every application instance.
     *
     * @return an unmodifiable list of all default {@code Category} instances
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
                FRIEND_LOAN,
                BANK_LOAN
        );
    }

    /**
     * Returns the origin of this category.
     *
     * @return {@link CategoryOrigin#DEFAULT} for built-in categories,
     *         {@link CategoryOrigin#CUSTOM} for user-defined ones
     */
    public CategoryOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns whether this category is currently active.
     *
     * @return {@code true} if the category is active, {@code false} if archived
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns whether this category is a built-in default.
     *
     * @return {@code true} if the origin is {@link CategoryOrigin#DEFAULT}
     */
    public boolean isDefault() {
        return origin == CategoryOrigin.DEFAULT;
    }

    /**
     * Returns whether this category was created by the user.
     *
     * @return {@code true} if the origin is {@link CategoryOrigin#CUSTOM}
     */
    public boolean isCustom() {
        return origin == CategoryOrigin.CUSTOM;
    }

    /**
     * Archives this category, making it inactive.
     *
     * @throws IllegalStateException if this is a default category, which cannot be archived
     */
    public void archive() {
        if (isDefault()) {
            throw new IllegalStateException("Default categories cannot be archived.");
        }
        this.active = false;
    }

    /**
     * Reactivates this category after it has been archived.
     *
     * @throws IllegalStateException if this is a default category, which is always active
     */
    public void reactivate() {
        if (isDefault()) {
            throw new IllegalStateException("Default categories are always active.");
        }
        this.active = true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Category{name='%s', color='%s', type='%s', origin='%s', active='%s'}"
                .formatted(getName(), getColorHex().toHexString(), getType(), origin, active);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category other)) {
            return false;
        }
        return super.equals(other)
                && active == other.active
                && origin == other.origin;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), origin, active);
    }
}
