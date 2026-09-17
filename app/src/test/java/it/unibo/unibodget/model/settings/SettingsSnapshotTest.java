package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SettingsSnapshotTest {

    @Test
    void shouldCreateSnapshotFromSettings() {
        final Settings s = new Settings();
        s.setBaseCurrency("USD");

        final SettingsSnapshot snap = SettingsSnapshot.of(s);

        assertEquals("USD", snap.getBaseCurrency());
        assertNotNull(snap.getSavedAt());
    }

    @Test
    void shouldStoreAllFields() {
        final WindowPreferences prefs = new WindowPreferences(100, 200, true);
        final Theme theme = Theme.DEFAULT;

        final SettingsSnapshot snap = new SettingsSnapshot(
                theme,
                "EUR",
                prefs,
                "2024-01-01T10:00"
        );

        assertEquals(theme, snap.getTheme());
        assertEquals("EUR", snap.getBaseCurrency());
        assertEquals(100, snap.getWindowPrefs().getWidth());
        assertEquals("2024-01-01T10:00", snap.getSavedAt());
    }

}
