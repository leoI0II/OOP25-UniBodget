package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.utils.ARGBColor;

class CategoryTest {

    @Test
    void defaultCategoriesShouldBeAlwaysActive() {
        assertTrue(Category.FOOD.isActive());
        assertTrue(Category.FOOD.isDefault());
        assertFalse(Category.FOOD.isCustom());
    }

    @Test
    void customCategoryShouldBeActiveByDefault() {
        Category c = new Category("MyCat", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        assertTrue(c.isActive());
        assertTrue(c.isCustom());
        assertFalse(c.isDefault());
    }

    @Test
    void shouldArchiveCustomCategory() {
        Category c = new Category("MyCat", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        c.archive();
        assertFalse(c.isActive());
    }

    @Test
    void shouldReactivateCustomCategory() {
        Category c = new Category("MyCat", new ARGBColor("#00FF00"), CategoryType.EXPENSE);
        c.archive();
        c.reactivate();
        assertTrue(c.isActive());
    }

    @Test
    void shouldNotArchiveDefaultCategory() {
        assertThrows(IllegalStateException.class, () -> Category.FOOD.archive());
    }

    @Test
    void shouldNotReactivateDefaultCategory() {
        assertThrows(IllegalStateException.class, () -> Category.FOOD.reactivate());
    }

    @Test
    void shouldReturnDefaultCategories() {
        assertFalse(Category.getDefaultCategories().isEmpty());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Category c1 = new Category("X", new ARGBColor("#123456"), CategoryType.EXPENSE);
        Category c2 = new Category("X", new ARGBColor("#123456"), CategoryType.EXPENSE);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

}
