package it.unibo.unibodget.controller.settings;

import it.unibo.unibodget.model.settings.*;

import java.util.List;

/**
 * Controller responsible for managing {@link Settings} and coordinating
 * updates between the model layer and UI-level managers such as
 * {@link ThemeManager} and {@link CurrencyContext}.
 *
 * <p>The controller exposes high-level operations for:</p>
 * <ul>
 *     <li>changing the theme</li>
 *     <li>changing the base currency</li>
 *     <li>updating window preferences</li>
 *     <li>saving and restoring configurations</li>
 * </ul>
 *
 * <p>All changes are persisted through {@link SettingsManager}.</p>
 */
public final class SettingsController {

    private final SettingsManager manager = new SettingsManager();
    private Settings settings = manager.getCurrent();

    /**
     * Initializes the controller by applying the current theme and base currency
     * to the global managers.
     */
    public SettingsController() {
        ThemeManager.setTheme(settings.getTheme());
        CurrencyContext.setBase(settings.getBaseCurrency());
    }

    /** 
     * Return active settings
     * @return the active settings instance 
     */
    public Settings getSettings() { 
        return settings; 
    }

    /** 
     * Return all saved configuration
     * @return all saved configuration snapshots 
     */
    public List<SettingsSnapshot> getAllSavedConfigurations() { 
        return settings.getPreferenceHistory(); 
    }

    /**
     * Changes the theme, updates the global {@link ThemeManager},
     * and persists the new settings.
     *
     * @param newTheme the theme to apply
     */
    public void changeTheme(Theme newTheme) {
        // Update model
        settings.setTheme(newTheme);
        // Update global theme manager
        ThemeManager.setTheme(newTheme);
        // Persist changes
        manager.saveCurrent(settings);
    }

    /**
     * Changes the base currency, updates {@link CurrencyContext},
     * and persists the new settings.
     *
     * @param newBase the new base currency code
     */
    public void changeBaseCurrency(String newBase) {
        // Update model
        settings.setBaseCurrency(newBase);
        // Update global currency context
        CurrencyContext.setBase(newBase);
        // Persist changes
        manager.saveCurrent(settings);
    }

    /**
     * Updates window preferences (size, maximized state) and persists them.
     *
     * @param prefs the new window preferences
     */
    public void updateWindowPrefs(WindowPreferences prefs) {
        settings.setWindowPrefs(prefs);
        manager.saveCurrent(settings);
    }

    /**
     * Saves the current configuration by adding a snapshot to history
     * and persisting the updated settings.
     */
    public void saveConfiguration() {
        // Add snapshot of current state
        settings.addSnapshotToHistory();
        // Persist changes
        manager.saveCurrent(settings);
    }

    /**
     * Applies a previously saved configuration snapshot.
     *
     * <p>This replaces the current settings with the snapshot values,
     * updates global managers, and persists the new state.</p>
     *
     * @param snap the snapshot to restore
     */
    public void applyConfiguration(SettingsSnapshot snap) {
        // Replace settings with snapshot
        this.settings = Settings.fromSnapshot(snap);
        // Update global managers
        ThemeManager.setTheme(settings.getTheme());
        CurrencyContext.setBase(settings.getBaseCurrency());
        // Persist restored configuration
        manager.saveCurrent(settings);
    }
    
}
