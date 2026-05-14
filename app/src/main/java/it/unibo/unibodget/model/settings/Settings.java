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

    /**
     * Creates a new Settings object with default values.
     */
    public Settings() {
        this.theme = Theme.DEFAULT;
        this.baseCurrency = FiatCurrency.EUR;
        this.preferenceHistory = new ArrayList<>();
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
            final List<String> preferenceHistory
    ) {
        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.preferenceHistory = new ArrayList<>(Objects.requireNonNull(preferenceHistory));
    }

    /**
     * Returns the active theme.
     *
     * @return the current theme
     */
    public Theme getTheme() {
        return theme;
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
        return baseCurrency;
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
        return List.copyOf(preferenceHistory);
    }

    private void addToHistory(final String entry) {
        preferenceHistory.add(entry);
    }

    @Override
    public String toString() {
        return "Settings{"
                + "theme=" + theme
                + ", baseCurrency=" + baseCurrency
                + ", preferenceHistory=" + preferenceHistory
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
        return Objects.equals(theme, other.theme)
                && Objects.equals(baseCurrency, other.baseCurrency)
                && Objects.equals(preferenceHistory, other.preferenceHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, baseCurrency, preferenceHistory);
    }
}