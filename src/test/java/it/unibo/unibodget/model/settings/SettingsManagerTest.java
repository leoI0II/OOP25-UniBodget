package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import it.unibo.unibodget.persistency.AppDataPathResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettingsManagerTest {

    private static final Path DATA_FILE = AppDataPathResolver.resolveJson("settings/Settings.json");
    private byte[] backup;
    private Settings previousSettings;

    @BeforeEach
    void backupSettingsFile() throws IOException {
        backup = Files.exists(DATA_FILE) ? Files.readAllBytes(DATA_FILE) : null;
        previousSettings = new SettingsManager().getCurrent().copy();
    }

    @AfterEach
    void restoreSettingsFile() throws IOException {
        if (previousSettings != null) {
            new SettingsManager().saveCurrent(previousSettings);
        }
        if (backup == null) {
            Files.deleteIfExists(DATA_FILE);
        } else {
            Files.write(DATA_FILE, backup);
        }
    }

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
