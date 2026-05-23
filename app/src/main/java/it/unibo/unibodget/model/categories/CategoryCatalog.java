package it.unibo.unibodget.model.categories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared catalog of categories available across all wallets.
 *
 * It contains built-in default categories and user-defined custom categories.
 * Custom categories can be archived/reactivated instead of being hard-deleted.
 */
public final class CategoryCatalog {

    private final List<Category> customCategories;

    public CategoryCatalog() {
        this.customCategories = new ArrayList<>();
    }

    public CategoryCatalog(final List<Category> customCategories) {
        this.customCategories = new ArrayList<>(Objects.requireNonNull(customCategories));
    }

    public List<Category> getDefaultCategories() {
        return Category.getDefaultCategories();
    }

    public List<Category> getCustomCategories() {
        return List.copyOf(customCategories);
    }

    public List<Category> getAllCategories() {
        final List<Category> all = new ArrayList<>(Category.getDefaultCategories());
        all.addAll(customCategories);
        return List.copyOf(all);
    }

    public List<Category> getActiveCategories() {
        return getAllCategories().stream()
                .filter(Category::isActive)
                .toList();
    }

    public Optional<Category> findByName(final String categoryName) {
        Objects.requireNonNull(categoryName);
        return getAllCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst();
    }

    public boolean existsByName(final String categoryName) {
        return findByName(categoryName).isPresent();
    }

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

    public void archiveCustomCategory(final String categoryName) {
        getCustomCategoryByName(categoryName).archive();
    }

    public void reactivateCustomCategory(final String categoryName) {
        getCustomCategoryByName(categoryName).reactivate();
    }

    private Category getCustomCategoryByName(final String categoryName) {
        return customCategories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(Objects.requireNonNull(categoryName)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Custom category not found: " + categoryName));
    }
}