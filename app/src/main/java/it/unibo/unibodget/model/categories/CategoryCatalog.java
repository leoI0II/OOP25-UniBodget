package it.unibo.unibodget.model.categories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import it.unibo.unibodget.model.utils.ARGBColor;

/**
 * Shared catalog of categories available across the application.
 *
 * <p>
 * The catalog contains:
 * </p>
 * <ul>
 * <li>built-in default categories, always present and always active,</li>
 * <li>user-defined custom categories, which can be added, renamed, recolored,
 * archived, and reactivated.</li>
 * </ul>
 *
 * <p>
 * Custom categories are not hard-deleted by management operations. Instead,
 * they can be archived and later reactivated.
 * </p>
 */
public final class CategoryCatalog {

    private final List<Category> customCategories;

    /**
     * Creates an empty catalog containing only built-in default categories.
     */
    public CategoryCatalog() {
        this.customCategories = new ArrayList<>();
    }

    /**
     * Creates a catalog initialized with the given custom categories.
     *
     * @param customCategories
     *            the initial custom categories
     * @throws NullPointerException
     *             if {@code customCategories} is {@code null}
     */
    public CategoryCatalog(final List<Category> customCategories) {
        Objects.requireNonNull(customCategories);
        this.customCategories = new ArrayList<>(customCategories);
    }

    /**
     * Returns the built-in categories.
     *
     * @return an immutable list of default categories
     */
    public List<Category> getDefaultCategories() {
        return Category.getDefaultCategories();
    }

    /**
     * Returns the custom categories currently stored in the catalog.
     *
     * @return an immutable list of custom categories
     */
    public List<Category> getCustomCategories() {
        return List.copyOf(customCategories);
    }

    /**
     * Returns all categories known to the catalog.
     *
     * @return an immutable list containing both default and custom categories
     */
    public List<Category> getAllCategories() {
        final List<Category> all = new ArrayList<>(Category.getDefaultCategories());
        all.addAll(customCategories);
        return List.copyOf(all);
    }

    /**
     * Returns only active categories.
     *
     * @return an immutable list of active categories
     */
    public List<Category> getActiveCategories() {
        return getAllCategories().stream()
                .filter(Category::isActive)
                .toList();
    }

    /**
     * Returns archived custom categories.
     *
     * @return an immutable list of archived custom categories
     */
    public List<Category> getArchivedCustomCategories() {
        return customCategories.stream()
                .filter(category -> !category.isActive())
                .toList();
    }

    /**
     * Finds a category by name, ignoring case.
     *
     * @param categoryName
     *            the name to search
     * @return the matching category, if present
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank
     */
    public Optional<Category> findByName(final String categoryName) {
        final String normalized = normalizeName(categoryName);
        return getAllCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(normalized))
                .findFirst();
    }

    /**
     * Returns whether a category with the given name exists.
     *
     * @param categoryName
     *            the category name to search
     * @return {@code true} if a category with that name exists, otherwise
     *         {@code false}
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank
     */
    public boolean existsByName(final String categoryName) {
        return findByName(categoryName).isPresent();
    }

    /**
     * Adds a custom category to the catalog.
     *
     * @param category
     *            the category to add
     * @throws NullPointerException
     *             if {@code category} is {@code null}
     * @throws IllegalArgumentException
     *             if the category is not custom or if another category with the
     *             same name already exists
     */
    public void addCustomCategory(final Category category) {
        Objects.requireNonNull(category);
        if (!category.isCustom()) {
            throw new IllegalArgumentException("Only custom categories can be added to the custom catalog.");
        }
        if (existsByName(category.getName())) {
            throw new IllegalArgumentException("A category with the same name already exists.");
        }
        customCategories.add(category);
    }

    /**
     * Renames a custom category.
     *
     * @param currentName
     *            the current category name
     * @param newName
     *            the new category name
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @throws IllegalArgumentException
     *             if either name is blank, if the target custom category does
     *             not exist, or if another category with the new name already
     *             exists
     */
    public void renameCustomCategory(final String currentName, final String newName) {
        final String normalizedCurrent = normalizeName(currentName);
        final String normalizedNew = normalizeName(newName);

        if (!normalizedCurrent.equalsIgnoreCase(normalizedNew) && existsByName(normalizedNew)) {
            throw new IllegalArgumentException("A category with the same name already exists.");
        }

        getCustomCategoryByName(normalizedCurrent).rename(normalizedNew);
    }

    /**
     * Recolors a custom category.
     *
     * @param categoryName
     *            the category name
     * @param newColor
     *            the new color
     * @throws NullPointerException
     *             if any argument is {@code null}
     * @throws IllegalArgumentException
     *             if the category name is blank or the custom category does not
     *             exist
     */
    public void recolorCustomCategory(final String categoryName, final ARGBColor newColor) {
        getCustomCategoryByName(categoryName).recolor(Objects.requireNonNull(newColor));
    }

    /**
     * Archives a custom category.
     *
     * @param categoryName
     *            the category name
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank or the custom category does
     *             not exist
     */
    public void archiveCustomCategory(final String categoryName) {
        getCustomCategoryByName(categoryName).archive();
    }

    /**
     * Reactivates a custom category.
     *
     * @param categoryName
     *            the category name
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank or the custom category does
     *             not exist
     */
    public void reactivateCustomCategory(final String categoryName) {
        getCustomCategoryByName(categoryName).reactivate();
    }

    /**
     * Returns a custom category by name.
     *
     * @param categoryName
     *            the category name
     * @return the matching custom category
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank or the custom category does
     *             not exist
     */
    private Category getCustomCategoryByName(final String categoryName) {
        final String normalized = normalizeName(categoryName);
        return customCategories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found: " + categoryName));
    }

    /**
     * Validates and normalizes a category name.
     *
     * @param categoryName
     *            the raw category name
     * @return the trimmed validated name
     * @throws NullPointerException
     *             if {@code categoryName} is {@code null}
     * @throws IllegalArgumentException
     *             if {@code categoryName} is blank
     */
    private String normalizeName(final String categoryName) {
        Objects.requireNonNull(categoryName);
        final String normalized = categoryName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be blank.");
        }
        return normalized;
    }
}