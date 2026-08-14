package it.unibo.unibodget.model.settings;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable snapshot of a {@link Settings} configuration at a specific moment.
 *
 * <p>
 * A snapshot captures only:</p>
 * <ul>
 *     <li>the theme</li>
 *     <li>the base currency</li>
 *     <li>window preferences</li>
 *     <li>a timestamp</li>
 * </ul>
 *
 * <p>
 * It deliberately does <strong>not</strong> include a nested history,
 * to avoid recursive structures (a snapshot containing a snapshot containing a snapshot...).</p>
 */
public final class SettingsSnapshot {

    private final Theme theme;
    private final String baseCurrency;
    private final WindowPreferences windowPrefs;
    private final String savedAt;

    /**
     * Creates a snapshot from JSON.
     *
     * @param theme        the theme at the moment of saving
     * @param baseCurrency the base currency code (e.g. "EUR")
     * @param windowPrefs  the window layout preferences
     * @param savedAt      timestamp of when the snapshot was created
     */
    @JsonCreator
    public SettingsSnapshot(
            @JsonProperty("theme") final Theme theme,
            @JsonProperty("baseCurrency") final String baseCurrency,
            @JsonProperty("windowPrefs") final WindowPreferences windowPrefs,
            @JsonProperty("savedAt") final String savedAt) {

        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.windowPrefs = Objects.requireNonNull(windowPrefs);
        this.savedAt = Objects.requireNonNull(savedAt);
    }

    /**
     * Creates a new snapshot from the given {@link Settings}.
     *
     * <p>
     * The timestamp is generated automatically using
     * {@link LocalDateTime#now()}.
     * </p>
     *
     * @param s the settings to snapshot
     * @return a new immutable snapshot
     */
    public static SettingsSnapshot of(final Settings s) {
        return new SettingsSnapshot(
                s.getTheme(),
                s.getBaseCurrency(),
                s.getWindowPrefs(),
                LocalDateTime.now().toString()
        );
    }

    /** 
     * Returns the saved theme. 
     * 
     * @return the theme at the time of snapshot creation
     */
    public Theme getTheme() { 
        return theme; 
    }

    /** 
     * Returns the saved base currency code. 
     * 
     * @return the base currency code (e.g., "EUR") at the time of snapshot creation
     */
    public String getBaseCurrency() { 
        return baseCurrency; 
    }

    /** 
     * Returns the saved window preferences. 
     * 
     * @return the window preferences at the time of snapshot creation
     */
    public WindowPreferences getWindowPrefs() { 
        return windowPrefs; 
    }

    /** 
     * Returns the timestamp of when the snapshot was created. 
     * 
     * @return the ISO-8601 formatted timestamp of snapshot creation
     */
    public String getSavedAt() { 
        return savedAt; 
    }

}
