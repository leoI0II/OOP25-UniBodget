package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SettingsManagerTest {

    @Test
    void shouldLoadDefaultSettingsIfFileMissing() {
        final SettingsManager mgr = new SettingsManager();
        final Settings s = mgr.getCurrent();

        assertNotNull(s);
        assertNotNull(s.getTheme());
        assertNotNull(s.getWindowPrefs());
    }

    @Test
    void shouldSaveAndReloadSettings() {
        final SettingsManager mgr = new SettingsManager();
        final Settings s = new Settings();

        s.setBaseCurrency("USD");
        mgr.saveCurrent(s);

        final SettingsManager mgr2 = new SettingsManager();
        assertEquals("USD", mgr2.getCurrent().getBaseCurrency());
    }

    @Test
    void shouldAppendSnapshotToHistory() {
        final SettingsManager mgr = new SettingsManager();
        final Settings s = new Settings();

        final int before = s.getPreferenceHistory().size();
        mgr.appendToHistory(s);

        assertEquals(before + 1, s.getPreferenceHistory().size());
    }

}
