package it.unibo.unibodget.model.categories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import it.unibo.unibodget.model.utils.ARGBColor;

class CategoryManagerTest {

    private static final String GREEN = "#00FF00";
    private static final String NEW_CAT = "NewCat";
    private static final String DUPLICATE = "Duplicate";

    @BeforeEach
    void hardResetCategoryManager() throws Exception {
        // Reset completo via reflection
        final Field loadedField = CategoryManager.class.getDeclaredField("loaded");
        loadedField.setAccessible(true);
        loadedField.set(null, new ArrayList<>()); // svuota completamente

        final Field initField = CategoryManager.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.set(null, true); // impedisce init()

        // Popola SOLO le default categories
        final List<Category> defaults = Category.getDefaultCategories();
        loadedField.set(null, new ArrayList<>(defaults));
    }

    @Test
    void shouldInitializeWithDefaultCategories() {
        assertFalse(CategoryManager.getAll().isEmpty());
        assertEquals(Category.getDefaultCategories().size(), CategoryManager.getAll().size());
    }

    @Test
    void shouldAddCategory() {
        final Category c = 
            new Category(NEW_CAT, new ARGBColor(GREEN), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertTrue(CategoryManager.getAll().stream()
                .anyMatch(cat -> cat.getName().equals(NEW_CAT)));
    }

    @Test
    void shouldNotAddDuplicateCategory() {
        final Category c = 
            new Category(DUPLICATE, new ARGBColor(GREEN), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertThrows(IllegalArgumentException.class, () ->
                CategoryManager.add(new Category(DUPLICATE, new ARGBColor(GREEN), CategoryType.EXPENSE))
        );
    }

    @Test
    void shouldRemoveCategory() {
        final Category c = 
            new Category("Removable", new ARGBColor(GREEN), CategoryType.EXPENSE);
        CategoryManager.add(c);

        assertTrue(CategoryManager.remove(c));
        assertFalse(CategoryManager.getAll().contains(c));
    }

    @Test
    void shouldGetByType() {
        final var expenses = CategoryManager.getByType(CategoryType.EXPENSE);

        assertFalse(expenses.isEmpty());
        assertTrue(expenses.stream().allMatch(cat -> cat.getType() == CategoryType.EXPENSE));
    }
}
