package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SettingsManagerTest {

    @Test
    void shouldLoadDefaultSettingsIfFileMissing() {
        SettingsManager mgr = new SettingsManager();
        Settings s = mgr.getCurrent();

        assertNotNull(s);
        assertNotNull(s.getTheme());
        assertNotNull(s.getWindowPrefs());
    }

    @Test
    void shouldSaveAndReloadSettings() {
        SettingsManager mgr = new SettingsManager();
        Settings s = new Settings();

        s.setBaseCurrency("USD");
        mgr.saveCurrent(s);

        SettingsManager mgr2 = new SettingsManager();
        assertEquals("USD", mgr2.getCurrent().getBaseCurrency());
    }

    @Test
    void shouldAppendSnapshotToHistory() {
        SettingsManager mgr = new SettingsManager();
        Settings s = new Settings();

        int before = s.getPreferenceHistory().size();
        mgr.appendToHistory(s);

        assertEquals(before + 1, s.getPreferenceHistory().size());
    }
    
}
