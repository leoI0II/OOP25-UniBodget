package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.utils.ARGBColor;

class BasicCategoryTest {

    static class TestCategory extends BasicCategory {
        TestCategory(final String name, final ARGBColor color, final CategoryType type) {
            super(name, color, type);
        }
    }

    @Test
    void shouldStoreFieldsCorrectly() {
        final TestCategory c = 
            new TestCategory("Food", new ARGBColor("#FF862D2D"), CategoryType.EXPENSE);

        assertEquals("Food", c.getName());
        assertEquals("#FF862D2D", c.getColorHex().toHexString());
        assertEquals(CategoryType.EXPENSE, c.getType());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        final TestCategory c1 = 
            new TestCategory("Food", new ARGBColor("#FF0000"), CategoryType.EXPENSE);
        final TestCategory c2 = 
            new TestCategory("Food", new ARGBColor("#FF0000"), CategoryType.EXPENSE);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        final TestCategory c = 
            new TestCategory("Food", new ARGBColor("#986868"), CategoryType.EXPENSE);
            
        assertTrue(c.toString().contains("Food"));
        assertFalse(c.toString().contains("#FF0000"));
    }

}
