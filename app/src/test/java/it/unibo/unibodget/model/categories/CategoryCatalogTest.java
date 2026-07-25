package it.unibo.unibodget.model.categories;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import it.unibo.unibodget.model.utils.ARGBColor;

class CategoryCatalogTest {

    @Test
    void shouldLoadDefaultAndCustomCategories() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertFalse(catalog.getDefaultCategories().isEmpty());
        assertEquals(1, catalog.getCustomCategories().size());
    }

    @Test
    void shouldReturnAllCategories() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertEquals(
                catalog.getDefaultCategories().size() + 1,
                catalog.getAllCategories().size()
        );
    }

    @Test
    void shouldFindCategoryByName() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertTrue(catalog.findByName("Custom1").isPresent());
        assertFalse(catalog.findByName("Unknown").isPresent());
    }

    @Test
    void shouldCheckExistenceByName() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertTrue(catalog.existsByName("Custom1"));
        assertFalse(catalog.existsByName("Unknown"));
    }

    @Test
    void shouldAddCustomCategory() {
        final CategoryCatalog catalog = new CategoryCatalog(List.of());
        final String name = "NewCat-" + UUID.randomUUID();
        final Category c = new Category(
            name,
            new ARGBColor("#00FF00"),
            CategoryType.EXPENSE
        );

        catalog.addCustomCategory(c);

        assertTrue(catalog.existsByName(name));
    }

    @Test
    void shouldNotAddDuplicateCategory() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        assertThrows(IllegalArgumentException.class, () ->
                catalog.addCustomCategory(new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE))
        );
    }

    @Test
    void shouldArchiveCustomCategory() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        catalog.archiveCustomCategory("Custom1");

        assertFalse(custom.isActive());
    }

    @Test
    void shouldReactivateCustomCategory() {
        final Category custom = 
            new Category("Custom1", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        final CategoryCatalog catalog = new CategoryCatalog(List.of(custom));

        catalog.archiveCustomCategory("Custom1");
        catalog.reactivateCustomCategory("Custom1");

        assertTrue(custom.isActive());
    }

    @Test
    void shouldThrowIfCustomCategoryNotFound() {
        final CategoryCatalog catalog = new CategoryCatalog(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                catalog.archiveCustomCategory("Unknown")
        );
    }

}
