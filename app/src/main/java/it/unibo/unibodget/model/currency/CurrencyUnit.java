package it.unibo.unibodget.model.currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common contract for all currency types in the system.
 *
 * <p>Every currency — whether fiat ({@link FiatCurrency}), crypto ({@link CryptoCurrency}),
 * stock ({@link StockMarketCurrency}), or dynamically loaded — must implement this interface
 * to expose its type, display symbol, identifiers, and formatting preferences.
 */
public interface CurrencyUnit {

    /**
     * Returns the category this currency belongs to (fiat, crypto, or stock).
     *
     * @return the {@link CurrencyType} of this currency
     */
    CurrencyType getType();

    /**
     * Returns the graphical symbol used to represent this currency in the UI.
     * For example: {@code "$"} for USD, {@code "€"} for EUR, {@code "₿"} for BTC.
     *
     * @return the display symbol
     */
    String getSymbol();

    /**
     * Returns the short ticker or ISO identifier for this currency.
     * For example: {@code "USD"}, {@code "EUR"}, {@code "BTC"}.
     *
     * @return the short name / ticker
     */
    String getShortName();

    /**
     * Returns the full human-readable name of this currency.
     * For example: {@code "US Dollar"}, {@code "Euro"}, {@code "Bitcoin"}.
     *
     * @return the full name
     */
    String getFullName();

    /**
     * Returns the unique standardized code identifying this currency.
     * Typically equal to the short name, but kept separate to allow
     * implementations to diverge if needed.
     *
     * @return the currency code
     */
    String getCode();

    /**
     * Retrieves a {@link CurrencyUnit} instance from its ISO code.
     * This method searches only in the known enum-based currencies.
     *
     * @param code the ISO currency code (e.g., "USD", "EUR")
     * @return the corresponding {@link CurrencyUnit}, or {@code null} if not found
     */
    static CurrencyUnit getByCode(final String code) {

        // Fiat currencies
        for (final var c : FiatCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }

        // Crypto currencies
        for (final var c : CryptoCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }

        // Stock market currencies
        for (final var c : StockMarketCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }
        // No match found
        return null;
    }

    /**
     * Returns a mutable list containing all available currency units in the system.
     *
     * <p>This method aggregates all enum-based currency types ({@link FiatCurrency},
     * {@link CryptoCurrency}, {@link StockMarketCurrency}) defined in the application.
     *
     * @return a new mutable list of all {@link CurrencyUnit} instances defined in the system
     */
    static List<CurrencyUnit> allCurrencies() {
        final List<CurrencyUnit> list = new ArrayList<>();
        Collections.addAll(list, FiatCurrency.values());
        Collections.addAll(list, CryptoCurrency.values());
        Collections.addAll(list, StockMarketCurrency.values());
        return list;
    }

    /**
     * Returns the number of decimal places to use when displaying amounts in this currency.
     * For example: {@code 2} for EUR/USD, {@code 8} for BTC, {@code 4} for most altcoins.
     *
     * @return the number of decimal places for display purposes
     */
    int getDisplayDecimals();
}
