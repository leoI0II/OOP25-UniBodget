package it.unibo.unibodget.model.currency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Interface to represent a generic currency unit.
 * 
 * <p>
 * Defines the minimal contract that every currency type
 * (fiat, crypto, stock, or dynamically loaded currencies) must satisfy.
 * 
 * <p>
 * Implementations provide:
 * - type: the category of the currency (e.g., "fiat", "crypto", "stock")
 * - symbol: the graphical symbol of the currency (e.g., "$", "€", "₿")
 * - shortName: a short identifier (e.g., "USD", "EUR", "BTC")
 * - fullName: the full description of the currency (e.g., "US Dollar", "Euro", "Bitcoin")
 * - code: a unique code for the currency (e.g., "USD", "EUR", "BTC")
 * 
 * <p>
 * Additional fields or methods may be implemented by specific currency types
 * (e.g., crypto API identifiers), but they are not required by this interface.
 */
public interface CurrencyUnit {

    /**
     * Returns the graphical symbol of the currency.
     * 
     * @return the type of currency
     */
    CurrencyType getType();

    /**
     * Returns the graphical symbol of the currency.
     * 
     * @return the symbol
     */
    String getSymbol();

    /**
     * Returns the short identifier of the currency.
     * 
     * @return the short name
     */
    String getShortName();

    /**
     * Returns the full description of the currency.
     * 
     * @return the full name
     */
    String getFullName();

    /**
     * Returns the unique code of the currency.
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
        // Fiat
        for (var c : FiatCurrency.values()){
            if (c.getCode().equalsIgnoreCase(code)){
                return c;
            }
        }

        // Crypto
        for (var c : CryptoCurrency.values()){
            if (c.getCode().equalsIgnoreCase(code)){
                return c;
            }
        }

        // Stock
        for (var c : StockMarketCurrency.values()){
            if (c.getCode().equalsIgnoreCase(code)){
                return c;
            }
        }

        // Custom from JSON
        return Currency.get(code);
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
        final List<CurrencyUnit> list = new ArrayList<>();
        Collections.addAll(list, FiatCurrency.values());
        Collections.addAll(list, CryptoCurrency.values());
        Collections.addAll(list, StockMarketCurrency.values());
        list.addAll(Currency.all());
        return list;
    }

    /**
     * Returns a list containing the basic currency units available for conversion.
     * This excludes stock market currencies.
     *
     * @return a list of basic {@link CurrencyUnit} instances
     */
    public static List<CurrencyUnit> basicCurrencies() {
        final List<CurrencyUnit> list = new ArrayList<>(allCurrencies());
        final ObservableList<CurrencyUnit> filteredCurrencies = FXCollections.observableArrayList(
            list.stream()
            .filter(c -> c.getType() != CurrencyType.STOCK)
            .filter(c -> c.getType() != CurrencyType.CUSTOM)
            .filter(c -> c.getType() != CurrencyType.CRYPTO)
            .collect(Collectors.toList())
        );
        System.out.println("Filtered Currencies: " + filteredCurrencies);
        return filteredCurrencies;
    }

}
