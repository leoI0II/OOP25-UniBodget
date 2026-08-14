package it.unibo.unibodget.model.settings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.unibo.unibodget.model.currency.Currency;
import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;

/**
 * Represents the global user preferences for the application.
 *
 * <p>
 * This includes: </p>
 * <ul>
 *     <li>the current UI theme</li>
 *     <li>the base currency used across all views</li>
 *     <li>a history of saved configurations</li>
 *     <li>window size and layout preferences</li>
 * </ul>
 *
 * <p>
 * The class is fully serializable via Jackson and supports
 * snapshot‑based persistence for undo/restore operations.
 * </p>
 */
public final class Settings {

    private Theme theme;
    private String baseCurrency;

    @JsonProperty("preferenceHistory")
    private final List<SettingsSnapshot> preferenceHistory;

    private WindowPreferences windowPrefs;

    @JsonIgnore
    private LocalDate lastModified;

    /**
     * Creates a Settings instance with default values.
     * 
     * <ul>
     *     <li>Theme: {@link Theme#DEFAULT}</li>
     *     <li>Base currency: EUR</li>
     *     <li>Empty preference history</li>
     *     <li>Default window preferences</li>
     * </ul>
     */
    public Settings() {
        this.theme = Theme.DEFAULT;
        this.baseCurrency = FiatCurrency.EUR.getShortName();
        this.preferenceHistory = new ArrayList<>();
        this.windowPrefs = new WindowPreferences();
    }

    /**
     * Creates a Settings instance from JSON.
     *
     * @param theme             the selected theme
     * @param baseCurrency      the base currency code (e.g. "EUR")
     * @param preferenceHistory the list of saved snapshots
     * @param windowPrefs       window layout preferences
     */
    @JsonCreator
    public Settings(
        @JsonProperty("theme") final Theme theme,
        @JsonProperty("baseCurrency") final String baseCurrency,
        @JsonProperty("preferenceHistory") final List<SettingsSnapshot> preferenceHistory,
        @JsonProperty("windowPrefs") final WindowPreferences windowPrefs
    ) {
        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.preferenceHistory = new ArrayList<>(Objects.requireNonNull(preferenceHistory));
        this.windowPrefs = Objects.requireNonNull(windowPrefs);
    }

    /** 
     * Returns the current theme. 
     * 
     * @return theme
     */
    public Theme getTheme() { 
        return theme; 
    }

    /**
     * Sets the theme and records a snapshot if the theme actually changed.
     *
     * <p>
     * This ensures that the history only grows when meaningful
     * changes occur.
     * </p>
     * 
     * @param theme the new theme to apply
     */
    public void setTheme(final Theme theme) { 
        if (!theme.equals(this.theme)) {
            addSnapshotToHistory();   // Save previous configuration
        }
        this.theme = theme; 
    }

    /** 
     * Returns the base currency code (e.g. "EUR"). 
     * 
     * @return base currency code
     */
    public String getBaseCurrency() { 
        return baseCurrency; 
    }

    /** 
     * Sets the base currency code. 
     * 
     * @param currency the new base currency code (not null)
     */
    public void setBaseCurrency(final String currency) { 
        this.baseCurrency = currency; 
    }

    /**
     * Returns an immutable view of the preference history.
     * 
     * @return list of snapshots
     */
    public List<SettingsSnapshot> getPreferenceHistory() {
        return List.copyOf(this.preferenceHistory);
    }

    /**
     * Replaces the entire preference history.
     * 
     * <p>
     * This method is primarily intended for deserialization and testing.
     * 
     * @param list the new list of snapshots (may be null or empty)
     */
    public void setPreferenceHistory(final List<SettingsSnapshot> list) {
        this.preferenceHistory.clear();
        if (list != null) {
            this.preferenceHistory.addAll(list);
        }
    }

    /**
     * Get LocalDate last modified.
     * 
     * @return LocalDate last modified
     */
    public LocalDate getLastModified() {
        return lastModified;
    }

    /**
     * Set LocalDate last modified.
     * 
     * @param date LocalDate last modified
     */
    public void setLastModified(final LocalDate date) {
        this.lastModified = date;
    }

    /**
     * Appends a new snapshot representing the current settings state.
     */
    public void addSnapshotToHistory() {
        this.preferenceHistory.add(SettingsSnapshot.of(this));
    }

    /**
     * Creates a new Settings instance from a snapshot.
     * The history is intentionally reset.
     * 
     * @param snap the snapshot to restore from
     * @return a new Settings instance reflecting the snapshot
     */
    public static Settings fromSnapshot(final SettingsSnapshot snap) {
        return new Settings(
            snap.getTheme(), 
            snap.getBaseCurrency(), 
            new ArrayList<>(), 
            snap.getWindowPrefs());
    }

    /**
     * Returns a deep copy of this Settings instance.
     * 
     * <p>
     * The copy has its own independent history and window preferences.
     * 
     * @return a new Settings instance with the same values
     */
    public Settings copy() {
        return new Settings(
            theme, 
            baseCurrency, 
            new ArrayList<>(preferenceHistory), 
            windowPrefs);
    }

    /** 
     * Returns the window layout preferences.
     * 
     * @return window preferences
     */
    public WindowPreferences getWindowPrefs() { 
        return windowPrefs; 
    }

    /** 
     * Sets the window layout preferences. 
     * 
     * @param prefs the new window preferences (not null)
     */
    public void setWindowPrefs(final WindowPreferences prefs) { 
        this.windowPrefs = prefs; 
    }

    /**
     * Resolves the base currency code into a {@link CurrencyUnit}.
     *
     * <p>
     * This method is ignored during JSON serialization and used
     * only at runtime.
     * </p>
     * 
     * @return the CurrencyUnit corresponding to the base currency code
     */
    @JsonIgnore
    public CurrencyUnit getBaseCurrencyUnit() {
        return Currency.get(baseCurrency);
    }

}
