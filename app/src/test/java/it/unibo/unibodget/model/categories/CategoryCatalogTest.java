package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import it.unibo.unibodget.model.utils.ARGBColor;

class CategoryCatalogTest {

    @Test
    void shouldLoadDefaultAndCustomCategories() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertFalse(catalog.getDefaultCategories().isEmpty());
        assertEquals(1, catalog.getCustomCategories().size());
    }

    @Test
    void shouldReturnAllCategories() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertEquals(
                catalog.getDefaultCategories().size() + 1,
                catalog.getAllCategories().size()
        );
    }

    @Test
    void shouldFindCategoryByName() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertTrue(catalog.findByName("Custom1").isPresent());
        assertFalse(catalog.findByName("Unknown").isPresent());
    }

    @Test
    void shouldCheckExistenceByName() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertTrue(catalog.existsByName("Custom1"));
        assertFalse(catalog.existsByName("Unknown"));
    }

    @Test
    void shouldAddCustomCategory() {
        CategoryCatalog catalog = new CategoryCatalog(List.of());
        Category c = new Category("NewCat", new ARGBColor("#00FF00"), CategoryType.EXPENSE);

        catalog.addCustomCategory(c);

        assertTrue(catalog.existsByName("NewCat"));
    }

    @Test
    void shouldNotAddDuplicateCategory() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertThrows(IllegalArgumentException.class, () ->
                catalog.addCustomCategory(new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE))
        );
    }

    @Test
    void shouldArchiveCustomCategory() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        catalog.archiveCustomCategory("Custom1");

        assertFalse(custom.isActive());
    }

    @Test
    void shouldReactivateCustomCategory() {
        Category custom = new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        catalog.archiveCustomCategory("Custom1");
        catalog.reactivateCustomCategory("Custom1");

        assertTrue(custom.isActive());
    }

    @Test
    void shouldThrowIfCustomCategoryNotFound() {
        CategoryCatalog catalog = new CategoryCatalog(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                catalog.archiveCustomCategory("Unknown")
        );
    }

}
