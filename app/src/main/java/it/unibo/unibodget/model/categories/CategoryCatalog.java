package it.unibo.unibodget.model.categories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared catalog of categories available across all wallets.
 *
 * <p>
 * It contains built-in default categories and user-defined custom categories.
 * Custom categories can be archived/reactivated instead of being hard-deleted.
 */
public final class CategoryCatalog {

    private final List<Category> customCategories;

    /**
     * Creates a new category catalog with an empty list of custom categories.
     */
    public CategoryCatalog() {
        this.customCategories = new ArrayList<>();
        CategoryManager.init();
        CategoryManager.getAll().stream()
                .filter(Category::isCustom)
                .forEach(customCategories::add);
    }

    /**
     * Creates a new category catalog with the given list of custom categories.
     *
     * @param customCategories the list of custom categories to initialize the catalog with
     */
    public CategoryCatalog(final List<Category> customCategories) {
        this.customCategories = new ArrayList<>(Objects.requireNonNull(customCategories));
    }

    /**
     * Returns the list of default categories.
     *
     * @return the list of default categories
     */
    public List<Category> getDefaultCategories() {
        return Category.getDefaultCategories();
    }

    /**
     * Returns the list of custom categories.
     *
     * @return the list of custom categories
     */
    public List<Category> getCustomCategories() {
        return List.copyOf(customCategories);
    }

    /**
     * Returns the list of all categories.
     *
     * @return the list of all categories
     */
    public List<Category> getAllCategories() {
        final List<Category> all = new ArrayList<>(Category.getDefaultCategories());
        all.addAll(customCategories);
        return List.copyOf(all);
    }

    /**
     * Returns the list of active categories.
     *
     * @return the list of active categories
     */
    public List<Category> getActiveCategories() {
        return getAllCategories().stream()
                .filter(Category::isActive)
                .toList();
    }

    /**
     * Finds a category by its name (case-insensitive).
     *
     * @param categoryName the name of the category to find
     * @return an {@link Optional} containing the found category, or empty if not found
     */
    public Optional<Category> findByName(final String categoryName) {
        Objects.requireNonNull(categoryName);
        return getAllCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst();
    }

    /**
     * Checks if a category with the given name exists in the catalog.
     *
     * @param categoryName the name of the category to check
     * @return {@code true} if a category with the given name exists, {@code false} otherwise
     */
    public boolean existsByName(final String categoryName) {
        return findByName(categoryName).isPresent();
    }

    /**
     * Adds a new custom category to the catalog.
     *
     * @param category the custom category to add
     * @throws IllegalArgumentException if the category is not custom or 
     *                                  if a category with the same name already exists
     */
    public void addCustomCategory(final Category category) {
        Objects.requireNonNull(category);
        if (!category.isCustom()) {
            throw new IllegalArgumentException("Only custom categories can be added to the custom catalog.");
        }
        if (existsByName(category.getName())) {
            throw new IllegalArgumentException("A category with the same name already exists.");
        }
        // Persist the new custom category using CategoryManager
        System.out.println("Adding custom category: " + category);
        customCategories.add(category);
        CategoryManager.add(category);
    }

    /**
     * Removes a custom category from the catalog.
     *
     * @param categoryName the name of the custom category to remove
     * @throws IllegalArgumentException if the category is not found or is not custom
     */
    public void archiveCustomCategory(final String categoryName) {
        //getCustomCategoryByName(categoryName).archive();
        final Category category = getCustomCategoryByName(categoryName);
        category.archive();
        CategoryManager.saveAll();
    }

    /**
     * Reactivates a previously archived custom category in the catalog.
     *
     * @param categoryName the name of the custom category to reactivate
     * @throws IllegalArgumentException if the category is not found or is not custom
     */
    public void reactivateCustomCategory(final String categoryName) {
        //getCustomCategoryByName(categoryName).reactivate();
        final Category category = getCustomCategoryByName(categoryName);
        category.reactivate();
        CategoryManager.saveAll();
    }

    /**
     * Retrieves a custom category by its name.
     *
     * @param categoryName the name of the custom category to retrieve
     * @return the custom category with the specified name
     * @throws IllegalArgumentException if the custom category is not found
     */
    private Category getCustomCategoryByName(final String categoryName) {
        return customCategories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(Objects.requireNonNull(categoryName)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found: " + categoryName));
    }

}
