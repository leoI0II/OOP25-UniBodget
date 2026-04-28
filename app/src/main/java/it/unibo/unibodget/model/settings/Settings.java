package it.unibo.unibodget.model.settings;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

import it.unibo.unibodget.model.currency.CurrencyUnit;
import it.unibo.unibodget.model.currency.FiatCurrency;

/**
 * Represents all user-configurable settings of the application.
 * This includes theme, base currency, budget limits and the
 * history of preference changes.
 */
public class Settings {

    private Theme theme;
    private CurrencyUnit baseCurrency;
    private BudgetLimit budgetLimit;
    private final List<String> preferenceHistory;

    /**
     * Creates a new Settings object with default values.
     * Default theme is {@link Theme#DEFAULT}, default currency is EUR,
     * and the global budget limit is initialized to zero in the base currency.
     */
    public Settings() {
        this.theme = Theme.DEFAULT;
        this.baseCurrency = FiatCurrency.EUR;
        this.budgetLimit = new BudgetLimit(0.0, baseCurrency);
        this.preferenceHistory = new ArrayList<>();
    }

    /**
     * Creates a new Settings object using the provided values.
     * This constructor is useful when loading settings from persistent storage
     * or when creating custom configurations for testing.
     *
     * @param theme the selected Theme (not null)
     * @param baseCurrency the base CurrencyUnit (not null)
     * @param budgetLimit the global BudgetLimit (may be null)
     * @param preferenceHistory the list of preference changes (not null)
     */
    public Settings(
            Theme theme,
            CurrencyUnit baseCurrency,
            BudgetLimit budgetLimit,
            List<String> preferenceHistory
    ) {
        this.theme = Objects.requireNonNull(theme);
        this.baseCurrency = Objects.requireNonNull(baseCurrency);
        this.budgetLimit = budgetLimit;
        this.preferenceHistory = new ArrayList<>(Objects.requireNonNull(preferenceHistory));
    }

    /**
     * Returns the currently selected theme.
     *
     * @return the active Theme
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Updates the current theme and records the change in the history.
     *
     * @param theme the new Theme to apply
     */
    public void setTheme(final Theme theme) {
        this.theme = theme;
        addToHistory("Theme changed to: " + theme);
    }

    /**
     * Returns the base currency used across the application.
     *
     * @return the base CurrencyUnit
     */
    public CurrencyUnit getBaseCurrency() {
        return baseCurrency;
    }

    /**
     * Updates the base currency and records the change in the history.
     *
     * @param currency the new base CurrencyUnit
     */
    public void setBaseCurrency(final CurrencyUnit currency) {
        this.baseCurrency = currency;
        addToHistory("Base currency changed to: " + currency);
    }

    /**
     * Returns the global budget limit set by the user.
     *
     * @return the BudgetLimit object
     */
    public BudgetLimit getBudgetLimit() {
        return budgetLimit;
    }

    /**
     * Updates the global budget limit and records the change in the history.
     *
     * @param limit the new BudgetLimit to apply
     */
    public void setBudgetLimit(final BudgetLimit limit) {
        this.budgetLimit = limit;
        addToHistory("Global budget limit updated");
    }

    /**
     * Returns the global budget limit wrapped in a list.
     * This method exists only for compatibility with previous structures.
     *
     * @return a list containing the single BudgetLimit, or an empty list if null
     */
    public List<BudgetLimit> getAllLimits() {
        return budgetLimit == null
        ? List.of()
        : List.of(budgetLimit);
    }

    /**
     * Returns an immutable list of all preference changes made by the user.
     *
     * @return a list of preference history entries
     */
    public List<String> getPreferenceHistory() {
        return List.copyOf(preferenceHistory);
    }

    /**
     * Adds a new entry to the preference history.
     *
     * @param entry the description of the change performed
     */
    private void addToHistory(final String entry) {
        preferenceHistory.add(entry);
    }

    @Override
    public String toString() {
        return "Settings{" +
                "theme=" + theme +
                ", baseCurrency=" + baseCurrency +
                ", budgetLimit=" + budgetLimit +
                ", preferenceHistory=" + preferenceHistory +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Settings)) return false;
        Settings s = (Settings) o;
        return Objects.equals(theme, s.theme)
                && Objects.equals(baseCurrency, s.baseCurrency)
                && Objects.equals(budgetLimit, s.budgetLimit)
                && Objects.equals(preferenceHistory, s.preferenceHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theme, baseCurrency, budgetLimit, preferenceHistory);
    }
}
