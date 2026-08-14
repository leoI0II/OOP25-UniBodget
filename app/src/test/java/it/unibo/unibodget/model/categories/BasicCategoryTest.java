package it.unibo.unibodget.model.categories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.utils.ARGBColor;

class BasicCategoryTest {

    private static final String FOOD = "Food";
    private static final String RED = "#FF0000";
    private static final String BROWN = "#FF862D2D";
    private static final String LIGHT_BROWN = "#986868";

    @Test
    void shouldStoreFieldsCorrectly() {
        final TestCategory c = 
            new TestCategory(FOOD, new ARGBColor(BROWN), CategoryType.EXPENSE);

        assertEquals(FOOD, c.getName());
        assertEquals(BROWN, c.getColorHex().toHexString());
        assertEquals(CategoryType.EXPENSE, c.getType());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        final TestCategory c1 =
            new TestCategory(FOOD, new ARGBColor(RED), CategoryType.EXPENSE);
        final TestCategory c2 = 
            new TestCategory(FOOD, new ARGBColor(RED), CategoryType.EXPENSE);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void shouldPrintReadableToString() {
        final TestCategory c = 
            new TestCategory(FOOD, new ARGBColor(LIGHT_BROWN), CategoryType.EXPENSE);

        assertTrue(c.toString().contains(FOOD));
        assertFalse(c.toString().contains(RED));
    }

    static class TestCategory extends AbstractCategory {
        TestCategory(final String name, final ARGBColor color, final CategoryType type) {
            super(name, color, type);
        }
    }

}
