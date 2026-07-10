package it.unibo.unibodget.model.settings;

import it.unibo.unibodget.model.currency.FiatCurrency;

/**
 * Global currency context used by all aggregators and dashboards.
 * When the user changes the base currency in Settings, this class
 * updates the global reference so all future calculations use it.
 */
public final class CurrencyContext {

    private static String base = FiatCurrency.EUR.getShortName();

    private CurrencyContext() {}

    /**
     * Returns the current base currency used for all calculations.
     *
     * @return the current base currency
     */
    public static String getBase() {
        return base;
    }

    /**
     * Sets a new base currency for all calculations.
     *
     * @param newBase the new base currency to set
     */
    public static void setBase(String newBase) {
        base = newBase;
    }
}
