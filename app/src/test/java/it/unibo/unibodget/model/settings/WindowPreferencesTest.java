package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WindowPreferencesTest {

    private static final int WIDTH_800 = 800;
    private static final int HEIGHT_600 = 600;
    private static final int WIDTH_1000 = 1000;
    private static final int HEIGHT_700 = 700;

    @Test
    void shouldStoreValues() {
        final WindowPreferences prefs = new WindowPreferences(WIDTH_800, HEIGHT_600, true);

        assertEquals(WIDTH_800, prefs.getWidth());
        assertEquals(HEIGHT_600, prefs.getHeight());
        assertTrue(prefs.isMaximized());
    }

    @Test
    void shouldUpdateValues() {
        final WindowPreferences prefs = new WindowPreferences();

        prefs.setWidth(WIDTH_1000);
        prefs.setHeight(HEIGHT_700);
        prefs.setMaximized(true);

        assertEquals(WIDTH_1000, prefs.getWidth());
        assertEquals(HEIGHT_700, prefs.getHeight());
        assertTrue(prefs.isMaximized());
    }

    @Test
    void shouldSupportEqualityAndHashCode() {
        final WindowPreferences p1 = new WindowPreferences(WIDTH_800, HEIGHT_600, false);
        final WindowPreferences p2 = new WindowPreferences(WIDTH_800, HEIGHT_600, false);
        final WindowPreferences p3 = new WindowPreferences(WIDTH_1000, HEIGHT_700, true);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "other");
    }
}
