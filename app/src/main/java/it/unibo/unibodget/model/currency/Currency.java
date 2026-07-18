package it.unibo.unibodget.model.currency;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.persistency.ModelFileManager;

/**
 * Represents a currency loaded from external JSON configuration.
 * 
 * <p>
 * A {@code Currency} instance defines:
 * <ul>
 *     <li>the {@link CurrencyType} category</li>
 *     <li>a graphical symbol</li>
 *     <li>a short identifier</li>
 *     <li>a full descriptive name</li>
 *     <li>a standardized ISO-like currency code</li>
 * </ul>
 * 
 * <p>
 * Currency objects may be created dynamically or loaded from the JSON file
 * {@code /json/currency/Currencies.json}. Loaded currencies are cached using
 * lazy initialization.
 */
public final class Currency implements CurrencyUnit {
    private static final Map<String, Currency> LOADED = new HashMap<>();
    private static boolean initialized = false;

    private static final Path PATH = Path.of("data/json/currency/Currencies.json");
    private static final String RESOURCE = "/json/currency/Currencies.json"; 

    private CurrencyType type;
    private String symbol;
    private String shortName;
    private String fullName;
    private String code;

    /**
     * Empty constructor required for JSON deserialization via Jackson.
     * 
     * <p>
     * Fields are populated automatically when mapping JSON nodes.
     */
    public Currency() {
        // Prevent instantion
    }

    /**
     * Creates a new dynamic currency instance.
     *
     * @param type      the currency type; must not be {@code null}
     * @param symbol    the graphical symbol; must not be {@code null}
     * @param shortName the short identifier; must not be {@code null}
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code; must not be {@code null}
     */
    public Currency(final CurrencyType type, final String symbol, final String shortName, 
                    final String fullName, final String code) {
        this.type = Objects.requireNonNull(type);
        this.symbol = Objects.requireNonNull(symbol);
        this.shortName = Objects.requireNonNull(shortName);
        this.fullName = fullName;
        this.code = Objects.requireNonNull(code);
    }

    /**
     * Returns the currency type.
     *
     * @return the {@link CurrencyType} of this currency
     */
    @Override
    public CurrencyType getType() {
        return this.type;
    }

    /**
     * Returns the graphical symbol of the currency.
     *
     * @return the symbol string
     */
    @Override
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Returns the short identifier of the currency.
     *
     * @return the short name
     */
    @Override
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Returns the full descriptive name of the currency.
     *
     * @return the full name
     */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Returns the standardized currency code.
     *
     * @return the currency code
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * Returns a human-readable representation of the currency.
     *
     * @return a formatted string containing short name, symbol, and full name
     */
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", this.shortName, this.symbol, this.fullName);
    }

    /**
     * Computes the hash code using the currency code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.code);
    }

    /**
     * Compares this currency with another object for equality.
     * <p>
     * Two currencies are considered equal if they share the same code.
     *
     * @param obj the object to compare
     * @return {@code true} if the other object is a {@code Currency} with the same code
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Currency)) {
            return false;
        }
        final Currency other = (Currency) obj;
        return Objects.equals(this.code, other.code);
    }
    
    /**
     * Initializes the currency manager by loading all currency definitions
     * from the JSON configuration file.
     *
     * <p>The method parses the "currencies" array, stores each entry in an
     * internal map keyed by its ISO code (upper‑case), and marks the manager
     * as initialized. If loading fails, a RuntimeException is thrown.
     */
    public static void init() {
        try {
            final ModelFileManager<Currency> mgr =
                new ModelFileManager<>(PATH, RESOURCE, Currency.class);
            mgr.open();
            List<Currency> list = mgr.loadList("currencies");
            mgr.close();
            if (list == null || list.isEmpty()) {
                System.out.println("Currency JSON empty → using mock currencies");
                list = generateMockCurrencies();
            }
            LOADED.clear();
            list.forEach(c -> LOADED.put(c.getCode().toUpperCase(), c));
            list.replaceAll(c -> LOADED.getOrDefault(c.getCode().toUpperCase(), c));
            initialized = true;
        } catch (final Exception e) {
            System.out.println("Currency JSON load failed → using mock currencies");
            LOADED.clear();
            generateMockCurrencies().forEach(
                c -> LOADED.put(c.getCode().toUpperCase(), c)
            );
            initialized = true;
        }
    }

    /**
     * Generates a list of mock currencies for testing purposes.
     *
     * @return a list of mock Currency instances
     */
    private static List<Currency> generateMockCurrencies() {
        return List.of(
            new Currency(CurrencyType.FIAT, "€", "EUR", "Euro", "EUR"),
            new Currency(CurrencyType.FIAT, "$", "USD", "US Dollar", "USD"),
            new Currency(CurrencyType.FIAT, "£", "GBP", "British Pound", "GBP"),
            new Currency(CurrencyType.FIAT, "¥", "JPY", "Japanese Yen", "JPY"),
            new Currency(CurrencyType.FIAT, "$", "CAD", "Canadian Dollar", "CAD")
        );
    }

    /**
     * Returns all currencies loaded from the JSON file.
     *
     * <p>
     * If the manager is not yet initialized, the JSON file is parsed
     * automatically.
     *
     * @return a collection of all available Currency instances
     */
    public static Collection<Currency> all() {
        if (!initialized) {
            init();
        }
        return LOADED.values();
    }

    /**
     * Retrieves a currency by its ISO code (case‑insensitive).
     *
     * <p>
     * If the manager is not yet initialized, the JSON file is parsed
     * automatically.
     *
     * @param code the ISO currency code; must not be null
     * @return the matching Currency, or null if no match exists
     */
    public static Currency get(final String code) {
        if (!initialized) {
            init();
        }
        return LOADED.get(code.toUpperCase());
    }

}
