package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.utils.ARGBColor;

class SettingsTest {

    @Test
    void shouldSaveAndReloadSettings() throws Exception {
        final Settings settings = new Settings();
        settings.setTheme(
            Theme.DEFAULT
        );
        final SettingsManager manager = new SettingsManager();
        manager.saveCurrent(settings);
        final Settings loaded = manager.getCurrent();
        assertEquals(settings, loaded);
    }

    @Test
    public void testSettingsPersistence() {
        final SettingsManager mgr = new SettingsManager();

        final Settings s = mgr.getCurrent();
        s.setTheme(
            new Theme(
                "Test",
                new ARGBColor("#FFFFFF"),
                new ARGBColor("#DDDDDD"),
                new ARGBColor("#000000"),
                "Arial",
                16,
                true
            )
        );
        s.setBaseCurrency(FiatCurrency.USD.getShortName());

        mgr.saveCurrent(s);
        mgr.appendToHistory(s.copy());

        final SettingsManager mgr2 = new SettingsManager();
        assertEquals("Test", mgr2.getCurrent().getTheme().getName());
    }

    @Test
    void shouldPersistBaseCurrency() {
        final SettingsManager mgr = new SettingsManager();
        final Settings settings = mgr.getCurrent();
        settings.setBaseCurrency(FiatCurrency.USD.getShortName());
        mgr.saveCurrent(settings);
        final SettingsManager mgr2 = new SettingsManager();
        assertEquals(
            FiatCurrency.USD.getShortName(),
            mgr2.getCurrent().getBaseCurrency()
        );
    }

    @Test
    void shouldAddEntryToHistoryWhenThemeChanges() {
        final Settings settings = new Settings();

        final int initialSize = settings.getPreferenceHistory().size();

        settings.setTheme(
                new Theme(
                        "Dark",
                        new ARGBColor("#000000"),
                        new ARGBColor("#222222"),
                        new ARGBColor("#FFFFFF")
                )
        );
        assertEquals(
                initialSize + 1,
                settings.getPreferenceHistory().size()
        );
    }

    @Test
    void shouldCreateIndependentCopy() {
        final Settings original = new Settings();
        final Settings copy = original.copy();

        copy.setTheme(
                new Theme(
                        "Modified",
                        new ARGBColor("#FFFFFF"),
                        new ARGBColor("#DDDDDD"),
                        new ARGBColor("#000000")
                )
        );

        assertEquals(
                Theme.DEFAULT.getName(),
                original.getTheme().getName()
        );
    }

    @Test
    void shouldStoreWindowPreferences() {
        final Settings settings = new Settings();
        settings.setWindowPrefs(
                new it.unibo.unibodget.model.settings.WindowPreferences(
                        1920,
                        1080,
                        true
                )
        );

        assertEquals(
                1920,
                settings.getWindowPrefs().getWidth()
        );
        assertEquals(
                1080,
                settings.getWindowPrefs().getHeight()
        );
        assertEquals(
                true,
                settings.getWindowPrefs().isMaximized()
        );
    }

    @Test
    void shouldPersistHistory() {
        final SettingsManager mgr = new SettingsManager();
        final Settings snapshot = new Settings();
        mgr.appendToHistory(snapshot);
        assertFalse(
                snapshot.getPreferenceHistory().isEmpty()
        );
    }

}
