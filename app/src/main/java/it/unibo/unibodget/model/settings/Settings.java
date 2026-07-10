package it.unibo.unibodget.model.settings;

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
 * <p>This includes:</p>
 * <ul>
 *     <li>the current UI theme</li>
 *     <li>the base currency used across all views</li>
 *     <li>a history of saved configurations</li>
 *     <li>window size and layout preferences</li>
 * </ul>
 *
 * <p>The class is fully serializable via Jackson and supports
 * snapshot‑based persistence for undo/restore operations.</p>
 */
public final class Settings {

    private Theme theme;
    private String baseCurrency;

    @JsonProperty("preferenceHistory")
    private List<SettingsSnapshot> preferenceHistory;

    private WindowPreferences windowPrefs;

    /**
     * Creates a Settings instance with default values:
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
        @JsonProperty("theme") Theme theme,
        @JsonProperty("baseCurrency") String baseCurrency,
        @JsonProperty("preferenceHistory") List<SettingsSnapshot> preferenceHistory,
        @JsonProperty("windowPrefs") WindowPreferences windowPrefs
    ) {
        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.preferenceHistory = new ArrayList<>(Objects.requireNonNull(preferenceHistory));
        this.windowPrefs = Objects.requireNonNull(windowPrefs);
    }

    /** Returns the current theme. */
    public Theme getTheme() { return theme; }

    /**
     * Sets the theme and records a snapshot if the theme actually changed.
     *
     * <p>This ensures that the history only grows when meaningful
     * changes occur.</p>
     */
    public void setTheme(Theme theme) { 
        if (!theme.equals(this.theme)) {
            addSnapshotToHistory();   // Save previous configuration
        }
        this.theme = theme; 
    }

    /** 
     * Returns the base currency code (e.g. "EUR"). 
     */
    public String getBaseCurrency() { return baseCurrency; }

    /** 
     * Sets the base currency code. 
     */
    public void setBaseCurrency(String currency) { this.baseCurrency = currency; }

    /**
     * Returns an immutable view of the preference history.
     */
    public List<SettingsSnapshot> getPreferenceHistory() {
        return List.copyOf(this.preferenceHistory);
    }

    /**
     * Replaces the entire preference history.
     */
    public void setPreferenceHistory(List<SettingsSnapshot> list) {
        this.preferenceHistory.clear();
        if (list != null) this.preferenceHistory.addAll(list);
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
     */
    public static Settings fromSnapshot(SettingsSnapshot snap) {
        return new Settings(
            snap.getTheme(), 
            snap.getBaseCurrency(), 
            new ArrayList<>(), 
            snap.getWindowPrefs());
    }

    /**
     * Returns a deep copy of this Settings instance.
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
     */
    public WindowPreferences getWindowPrefs() { 
        return windowPrefs; 
    }

    /** Sets the window layout preferences. */
    public void setWindowPrefs(WindowPreferences prefs) { 
        this.windowPrefs = prefs; 
    }

    /**
     * Resolves the base currency code into a {@link CurrencyUnit}.
     *
     * <p>This method is ignored during JSON serialization and used
     * only at runtime.</p>
     */
    @JsonIgnore
    public CurrencyUnit getBaseCurrencyUnit() {
        return Currency.get(baseCurrency);
    }
}
