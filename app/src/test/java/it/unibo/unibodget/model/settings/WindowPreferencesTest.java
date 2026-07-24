package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WindowPreferencesTest {

    @Test
    void shouldStoreValues() {
        final WindowPreferences prefs = new WindowPreferences(800, 600, true);

        assertEquals(800, prefs.getWidth());
        assertEquals(600, prefs.getHeight());
        assertTrue(prefs.isMaximized());
    }

    @Test
    void shouldUpdateValues() {
        final WindowPreferences prefs = new WindowPreferences();

        prefs.setWidth(1000);
        prefs.setHeight(700);
        prefs.setMaximized(true);

        assertEquals(1000, prefs.getWidth());
        assertEquals(700, prefs.getHeight());
        assertTrue(prefs.isMaximized());
    }

}
