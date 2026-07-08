package it.unibo.unibodget.model.categories;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unibo.unibodget.persistency.ModelFileManager;

public final class CategoryManager {

    private static final Path PATH = Path.of("data/json/categories/Categories.json");
    private static final String RESOURCE = "/json/categories/Categories.json";

    private static List<Category> loaded = new ArrayList<>();
    private static boolean initialized = false;

    private CategoryManager() {
    }

    /*
     * Initializes the category manager by loading categories from the JSON file.
     */
    public static void init() {
        try {
            final ModelFileManager<Category> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Category.class);
            mgr.open();
            loaded = new ArrayList<>(mgr.loadList("categories"));
            mgr.close();
            if (loaded.isEmpty()) {
                System.out.println("No categories found in file, loading default categories.");
                loaded.addAll(Category.getDefaultCategories());
                saveAll();
            }
            initialized = true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to load categories from file, loading default categories.");
            loaded.clear();
            loaded.addAll(Category.getDefaultCategories());
            initialized = true;
        }
    }

    /**
     * Returns all loaded categories.
     *
     * @return a list of all categories
     */
    public static List<Category> getAll() {
        if (!initialized) {
            init();
        }
        return new ArrayList<>(loaded);
    }

    /**
     * Adds a category to the loaded list.
     *
     * @param category the category to add
     */
    public static void add(Category category) {
        if (!initialized) {
            init();
        }
        boolean alreadyExists = loaded.stream()
                .anyMatch(c ->
                    c.getName().equalsIgnoreCase(category.getName())
                );
        if (alreadyExists) {
            throw new IllegalArgumentException(
                "Category already exists: " + category.getName()
            );
        }
        loaded.add(category);
        saveAll();
    }

    /**
     * Removes a category from the loaded list.
     *
     * @param category the category to remove
     * @return {@code true} if the category was removed, {@code false} otherwise
     */
    public static boolean remove(Category category) {
        if (!initialized) {
            init();
        }
        boolean removed = loaded.remove(category);
        if (removed) {
            saveAll();
        }
        return removed;
    }

    /**
     * Saves all categories to the JSON file.
     */
    public static void saveAll() {
        try {
            final ModelFileManager<Category> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Category.class);

            mgr.open();
            mgr.saveList("categories", loaded);
            mgr.close();

        } catch (Exception e) {
            throw new RuntimeException("Unable to save categories", e);
        }
    }

    /**
     * Reloads all categories from the JSON file.
     */
    public static void reload() {
        initialized = false;
        init();
    }

    /**
     * Returns all categories of a specific type.
     *
     * @param type the category type
     * @return a list of categories of the specified type
     */
    public static List<Category> getByType(CategoryType type) {
        if (!initialized) {
            init();
        }

        return loaded.stream()
                .filter(c -> c.getType() == type)
                .toList();
    }

    /**
     * Saves a category to the JSON file.
     *
     * @param category the category to save
     */
    public static void save(Category category) {
        if (!initialized) {
            init();
        }
        saveAll();
    }

}
