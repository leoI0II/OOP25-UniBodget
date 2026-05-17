package it.unibo.unibodget.model.currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface to represent a generic currency unit.
 * 
 * Defines the minimal contract that every currency type
 * (fiat, crypto, stock, or dynamically loaded currencies) must satisfy.
 * 
 * Implementations provide:
 * - type: the category of the currency (e.g., "fiat", "crypto", "stock")
 * - symbol: the graphical symbol of the currency (e.g., "$", "€", "₿")
 * - shortName: a short identifier (e.g., "USD", "EUR", "BTC")
 * - fullName: the full description of the currency (e.g., "US Dollar", "Euro", "Bitcoin")
 * - code: a unique code for the currency (e.g., "USD", "EUR", "BTC")
 * 
 * Additional fields or methods may be implemented by specific currency types
 * (e.g., crypto API identifiers), but they are not required by this interface.
 */
public interface CurrencyUnit {

    /**
     * Returns the graphical symbol of the currency.
     * @return the type of currency
     */
    CurrencyType getType();

    /**
     * Returns the graphical symbol of the currency.
     * @return the symbol
     */
    String getSymbol();

    /**
     * Returns the short identifier of the currency.
     * @return the short name
     */
    String getShortName();

    /**
     * Returns the full description of the currency.
     * @return the full name
     */
    String getFullName();

    /**
     * Returns the unique code of the currency.
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
    public static CurrencyUnit getByCode(String code) {

        // Fiat currencies
        for (var c : FiatCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }
        
        // Crypto currencies
        for (var c : CryptoCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }

        // Stock market currencies
        for (var c : StockMarketCurrency.values()) {
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }

        // No match found
        return null;
    }

    /**
     * Returns a list containing all available currency units in the system.
     * 
     * This method aggregates all enum-based currency types implemented in the
     * application
     *
     * @return a list of all {@link CurrencyUnit} instances defined in the system
     */
    public static List<CurrencyUnit> allCurrencies() {
        List<CurrencyUnit> list = new ArrayList<>();
        Collections.addAll(list, FiatCurrency.values());
        Collections.addAll(list, CryptoCurrency.values());
        Collections.addAll(list, StockMarketCurrency.values());
        return list;
    }

    public int getDisplayDecimals();

}
