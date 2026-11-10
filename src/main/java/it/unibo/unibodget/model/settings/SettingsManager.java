package it.unibo.unibodget.model.settings;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.unibodget.persistency.AppDataPathResolver;
import it.unibo.unibodget.persistency.ModelFileManager;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages loading and saving of {@link Settings} from persistent storage.
 *
 * <p>The manager keeps a single static instance of the current settings, automatically loading it
 * on first use. All changes are written to the resolved settings JSON path using
 * {@link ModelFileManager}.
 *
 * <p>It also provides convenience methods for appending snapshots to the history and updating
 * window preferences.
 */
public final class SettingsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsManager.class);

    private static final Path PATH = AppDataPathResolver.resolveJson("settings/Settings.json");
    private static final String RESOURCE = "/json/settings/Settings.json";

    private static Settings current;
    private static boolean initialized;

    /** Creates a SettingsManager and initializes the settings if needed. */
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
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "SettingsManager provides access to active singleton settings")
    public Settings getCurrent() {
        return current;
    }

    private static synchronized void setCurrent(final Settings s) {
        current = s;
    }

    /**
     * Saves the given settings to disk and updates the in‑memory instance.
     *
     * @param s the settings to persist
     */
    public void saveCurrent(final Settings s) {
        try (ModelFileManager<Settings> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Settings.class)) {

            mgr.open();
            mgr.saveObject(s);
            setCurrent(s);

        } catch (final IOException e) {
            LOGGER.error("Settings save failed", e);
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
     * <p>This method is called only once, on first construction of {@link SettingsManager}.
     */
    private static synchronized void init() {
        if (initialized) {
            return;
        }
        try (ModelFileManager<Settings> mgr =
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
