package it.unibo.unibodget.model.currency;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.persistency.util.FilesUtils;
import it.unibo.unibodget.persistency.parser.impl.JsonDataParser;

/**
 * Represents a currency loaded from external JSON configuration.
 * 
 * A Currency instance defines:
 * - type: the category of the currency
 * - symbol: graphical representation
 * - shortName: short identifier
 * - fullName: full descriptive name
 * - code: standardized currency code
 */
public final class Currency implements CurrencyUnit {

    private CurrencyType type;
    private String symbol;
    private String shortName;
    private String fullName;
    private String code;

    private static final Map<String, Currency> loaded = new HashMap<>();
    private static boolean initialized = false;

    /** Empty constructor required for the generic JSON parser. */
    public Currency() {}

    /**
     * Creates a new dynamic currency.
     *
     * @param type      the type of currency
     * @param symbol    the graphical symbol of the currency
     * @param shortName the short identifier
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code
     */
    public Currency(CurrencyType type, String symbol, String shortName, String fullName, String code) {
        this.type = Objects.requireNonNull(type);
        this.symbol = Objects.requireNonNull(symbol);
        this.shortName = Objects.requireNonNull(shortName);
        this.fullName = Objects.requireNonNull(fullName);
        this.code = Objects.requireNonNull(code);
    }

    /**
     * Returns the currency type.
     *
     * @return the type of this currency
     */
    @Override
    public CurrencyType getType() {
        return this.type;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the symbol of this currency
     */
    @Override
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the currency short name.
     *
     * @return the short name of this currency
     */
    @Override
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Returns the currency full name.
     *
     * @return the full name of this currency
     */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Returns the currency code.
     *
     * @return the code of this currency
     */
    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s", this.shortName, this.symbol, this.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.code);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency)) return false;
        Currency other = (Currency) obj;
        return Objects.equals(this.code, other.code);
    }

    /**
     * Returns the currency associated with the given code.
     * The lookup is case-insensitive.
     *
     * @param code the currency code to search for
     * @return the matching currency, or null if not found
     */
    public static Currency get(String code) {
        loadFromJson();
        return loaded.get(code.toUpperCase());
    }

    /**
     * Returns all loaded currencies. If currencies have not been loaded yet,
     * the JSON configuration file is parsed before returning the collection.
     *
     * @return a collection of all available currencies
     */
    public static Collection<Currency> all() {
        loadFromJson();
        return loaded.values();
    }

    /**
     * Loads currency definitions from the JSON configuration file.
     * The file is searched inside the application resources directory.
     * Parsed currencies are stored in a static cache and loaded only once.
     */
    private static void loadFromJson() {
        if (initialized) return;
        try {
            // Locates the JSON file inside the application resources directory
            Path file = FilesUtils.findFileByName("Currencies.json");
            if (file == null) {
                System.err.println("File not found: Currencies.json");
                return;
            } else {
                System.err.println("File found: " + file.toAbsolutePath());
            }
            // Creates a parser capable of converting JSON objects into Currency instances
            JsonDataParser<Currency> parser = new JsonDataParser<>(Currency.class);
            List<Currency> list = parser.loadListFromFile(file);
            // Stores each parsed currency in the internal cache using its code as the key
            for (Currency c : list) {
                loaded.put(c.getCode().toUpperCase(), c);
            }
            initialized = true;
        } catch (Exception e) {
            // Handles unexpected runtime errors such as IO or reflection failures
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

}
