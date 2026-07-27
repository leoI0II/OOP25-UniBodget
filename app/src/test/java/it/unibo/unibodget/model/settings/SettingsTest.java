package it.unibo.unibodget.model.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import it.unibo.unibodget.model.currency.FiatCurrency;
import it.unibo.unibodget.model.utils.ARGBColor;

class SettingsTest {

    private static final String WHITE = "#FFFFFF";
    private static final String GREY = "#DDDDDD";
    private static final String BLACK = "#000000";
    private static final String TEST = "Test";
    private static final int EXP_1080 = 1080;
    private static final int EXP_1920 = 1920;

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
                TEST,
                new ARGBColor(WHITE),
                new ARGBColor(GREY),
                new ARGBColor(BLACK),
                "Arial",
                16,
                true
            )
        );
        s.setBaseCurrency(FiatCurrency.USD.getShortName());

        mgr.saveCurrent(s);
        mgr.appendToHistory(s.copy());

        final SettingsManager mgr2 = new SettingsManager();
        assertEquals(TEST, mgr2.getCurrent().getTheme().getName());
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
                        new ARGBColor(BLACK),
                        new ARGBColor("#222222"),
                        new ARGBColor(WHITE)
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
                        new ARGBColor(WHITE),
                        new ARGBColor(GREY),
                        new ARGBColor(BLACK)
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
                        EXP_1920,
                        EXP_1080,
                        true
                )
        );

        assertEquals(
                EXP_1920,
                settings.getWindowPrefs().getWidth()
        );
        assertEquals(
                EXP_1080,
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
