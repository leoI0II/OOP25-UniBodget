package it.unibo.unibodget.model.settings;

import it.unibo.unibodget.persistency.ModelFileManager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Manages loading and saving of {@link Settings} from persistent storage.
 *
 * <p>The manager keeps a single static instance of the current settings,
 * automatically loading it on first use. All changes are written to
 * <code>data/json/settings/Settings.json</code> using {@link ModelFileManager}.</p>
 *
 * <p>It also provides convenience methods for appending snapshots to the
 * history and updating window preferences.</p>
 */
public final class SettingsManager {

    private static final Path PATH = Path.of("data/json/settings/Settings.json");
    private static final String RESOURCE = "/json/settings/Settings.json";

    private static Settings current;
    private static boolean initialized;

    /**
     * Creates a SettingsManager and initializes the settings if needed.
     */
    public SettingsManager() {
        if (!initialized) {
            init();
        }
    }

    /**
     * Returns the currently loaded settings.
     *
     * @return the active {@link Settings} instance
     */
    public Settings getCurrent() {
        return current;
    }

    /**
     * Saves the given settings to disk and updates the in‑memory instance.
     *
     * @param s the settings to persist
     */
    public void saveCurrent(final Settings s) {
        try (final ModelFileManager<Settings> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Settings.class)) {

            mgr.open();
            mgr.saveObject(s);
            current = s; // Update in‑memory reference

        } catch (final IOException e) {
            System.out.println("Settings save failed: " + e);
        }
    }

    /**
     * Adds a snapshot of the given settings to its history and saves it.
     *
     * @param s the settings to update and persist
     */
    public void appendToHistory(final Settings s) {
        s.addSnapshotToHistory();
        saveCurrent(s);
    }

    /**
     * Loads settings from disk or creates defaults if none exist.
     *
     * <p>This method is called only once, on first construction of
     * {@link SettingsManager}.</p>
     */
    private static void init() {
        initialized = false;
        try (final ModelFileManager<Settings> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Settings.class)) {

            mgr.open();
            current = mgr.loadObject();

            // If no file exists, create default settings
            if (current == null) {
                current = new Settings();
            }

            initialized = true;

        } catch (final IOException e) {
            // Fallback to default settings on any error
            current = new Settings();
            initialized = true;
        }
    }

}
