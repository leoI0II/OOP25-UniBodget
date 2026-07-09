package it.unibo.unibodget.model.categories;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Concrete category used by the application.
 *
 * A category extends the shared base category with application-level metadata:
 * - origin (default or custom)
 * - active flag for archive/reactivate behavior
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonCreator
    public Category(
            @JsonProperty("name") final String name,
            @JsonProperty("colorHex") final ARGBColor color,
            @JsonProperty("type") final CategoryType type,
            @JsonProperty("origin") final CategoryOrigin origin,
            @JsonProperty("active") final boolean active) {
        super(name, color, type);
        this.origin = Objects.requireNonNull(origin);
        if (origin == CategoryOrigin.DEFAULT && !active) {
            throw new IllegalArgumentException("Default categories must always be active.");
        }
        this.active = active;
    }

    /**
     * Convenience constructor for creating active custom categories.
     *
     * @param name the category name
     * @param color the category color
     * @param type the category type
     */
    public Category(final String name, final ARGBColor color, final CategoryType type) {
        this(name, color, type, CategoryOrigin.CUSTOM, true);
    }

    /**
     * Returns the built-in categories available in every application instance.
     *
     * @return the default categories
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
     * Returns the origin of the category.
     *
     * @return the {@link CategoryOrigin} of this category
     */
    public CategoryOrigin getOrigin() {
        return origin;
    }

    /**
     * Returns whether the category is active.
     *
     * @return {@code true} if the category is active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status of the category.
     *
     * @param active the new active status
     */
    public boolean isDefault() {
        return origin == CategoryOrigin.DEFAULT;
    }

    /**
     * Returns whether the category is a custom category.
     *
     * @return {@code true} if the category is custom, {@code false} otherwise
     */
    public boolean isCustom() {
        return origin == CategoryOrigin.CUSTOM;
    }

    /**
     * Archives the category, making it inactive.
     *
     * @throws IllegalStateException if the category is a default category
     */
    public void archive() {
        if (isDefault()) {
            throw new IllegalStateException("Default categories cannot be archived.");
        }
        this.active = false;
    }

    /**
     * Reactivates the category, making it active again.
     *
     * @throws IllegalStateException if the category is a default category
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), origin, active);
    }

}
