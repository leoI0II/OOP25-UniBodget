package it.unibo.unibodget.model.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;

/**
 * Represents global user preferences for the application.
 *
 * <p>This includes visual preferences shared across all views and
 * the system default currency used to display aggregated totals
 * across multiple wallets.</p>
 */
public final class Settings {

    private Theme theme;
    private CurrencyUnit baseCurrency;
    private final List<String> preferenceHistory;
    private WindowPreferences windowPrefs = new WindowPreferences();

    /**
     * Creates a new Settings object with default values.
     */
    public Settings() {
        this.theme = Theme.DEFAULT;
        this.baseCurrency = FiatCurrency.EUR;
        this.preferenceHistory = new ArrayList<>();
        this.windowPrefs = new WindowPreferences();
    }

    /**
     * Creates a new Settings object with custom values.
     *
     * @param theme the selected theme
     * @param baseCurrency the system default currency
     * @param preferenceHistory the history of user preference changes
     */
    public Settings(
            final Theme theme,
            final CurrencyUnit baseCurrency,
            final List<String> preferenceHistory,
            final WindowPreferences windowPrefs
    ) {
        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.preferenceHistory = new ArrayList<>(Objects.requireNonNull(preferenceHistory));
        this.windowPrefs = Objects.requireNonNull(windowPrefs);
    }

    /**
     * Returns the active theme.
     *
     * @return the current theme
     */
    public Theme getTheme() {
        return this.theme;
    }

    /**
     * Updates the active theme.
     *
     * @param theme the new theme
     */
    public void setTheme(final Theme theme) {
        this.theme = Objects.requireNonNull(theme);
        addToHistory("Theme changed to: " + theme.getName());
    }

    /**
     * Returns the system default currency used for aggregated totals.
     *
     * @return the base currency
     */
    public CurrencyUnit getBaseCurrency() {
        return this.baseCurrency;
    }

    /**
     * Updates the system default currency.
     *
     * @param currency the new base currency
     */
    public void setBaseCurrency(final CurrencyUnit currency) {
        this.baseCurrency = Objects.requireNonNull(currency);
        addToHistory("Base currency changed to: " + currency);
    }

    /**
     * Returns an immutable copy of the preference history.
     *
     * @return the preference history
     */
    public List<String> getPreferenceHistory() {
        return List.copyOf(this.preferenceHistory);
    }

    /**
     * Adds a new entry to the preference history.
     *
     * @param entry the description of the change performed
     */
    private void addToHistory(final String entry) {
        this.preferenceHistory.add(entry);
    }

    /**
     * Returns the window preferences.
     *
     * @return the window preferences
     */
    public WindowPreferences getWindowPrefs() { 
        return this.windowPrefs; 
    }

    /**
     * Sets the window preferences.
     *
     * @param prefs the new window preferences
     */
    public void setWindowPrefs(WindowPreferences prefs) { 
        this.windowPrefs = prefs; 
    }

    @Override
    public String toString() {
        return "Settings{"
                + "theme=" + this.theme
                + ", baseCurrency=" + this.baseCurrency
                + ", preferenceHistory=" + this.preferenceHistory
                + ", windowPrefs=" + this.windowPrefs
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Settings)) {
            return false;
        }
        final Settings other = (Settings) o;
        return Objects.equals(this.theme, other.theme)
                && Objects.equals(this.baseCurrency, other.baseCurrency)
                && Objects.equals(this.preferenceHistory, other.preferenceHistory)
                && Objects.equals(this.windowPrefs, other.windowPrefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.theme, this.baseCurrency, this.preferenceHistory, this.windowPrefs);
    }
}