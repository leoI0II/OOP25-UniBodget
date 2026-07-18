package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import it.unibo.unibodget.model.utils.ARGBColor;

class CategoryManagerTest {

    @BeforeEach
    void hardResetCategoryManager() throws Exception {
        // Reset completo via reflection
        Field loadedField = CategoryManager.class.getDeclaredField("LOADED");
        loadedField.setAccessible(true);
        loadedField.set(null, new ArrayList<>()); // svuota completamente

        Field initField = CategoryManager.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, true); // impedisce init()

        // Popola SOLO le default categories
        List<Category> defaults = Category.getDefaultCategories();
        loadedField.set(null, new ArrayList<>(defaults));
    }

    @Test
    void shouldInitializeWithDefaultCategories() {
        assertFalse(CategoryManager.getAll().isEmpty());
        assertEquals(Category.getDefaultCategories().size(), CategoryManager.getAll().size());
    }

    @Test
    void shouldAddCategory() {
        Category c = new Category("NewCat", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertTrue(CategoryManager.getAll().stream()
                .anyMatch(cat -> cat.getName().equals("NewCat")));
    }

    @Test
    void shouldNotAddDuplicateCategory() {
        Category c = new Category("Duplicate", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertThrows(IllegalArgumentException.class, () ->
                CategoryManager.add(new Category("Duplicate", new ARGBColor("#00FF00"), CategoryType.EXPENSE))
        );
    }

    @Test
    void shouldRemoveCategory() {
        Category c = new Category("Removable", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertTrue(CategoryManager.remove(c));
        assertFalse(CategoryManager.getAll().contains(c));
    }

    @Test
    void shouldGetByType() {
        var expenses = CategoryManager.getByType(CategoryType.EXPENSE);

        assertFalse(expenses.isEmpty());
        assertTrue(expenses.stream().allMatch(cat -> cat.getType() == CategoryType.EXPENSE));
    }
}
