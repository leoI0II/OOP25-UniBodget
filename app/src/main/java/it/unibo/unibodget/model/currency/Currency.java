package it.unibo.unibodget.model.currency;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a currency loaded from external JSON configuration.
 * <p>
 * A {@code Currency} instance defines:
 * <ul>
 *     <li>the {@link CurrencyType} category</li>
 *     <li>a graphical symbol</li>
 *     <li>a short identifier</li>
 *     <li>a full descriptive name</li>
 *     <li>a standardized ISO-like currency code</li>
 * </ul>
 * <p>
 * Currency objects may be created dynamically or loaded from the JSON file
 * {@code /json/currency/Currencies.json}. Loaded currencies are cached using
 * lazy initialization.
 */
public final class Currency implements CurrencyUnit {

    private CurrencyType type;
    private String symbol;
    private String shortName;
    private String fullName;
    private String code;

    private static final Map<String, Currency> loaded = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Empty constructor required for JSON deserialization via Jackson.
     * <p>
     * Fields are populated automatically when mapping JSON nodes.
     */
    public Currency() {}

    /**
     * Creates a new dynamic currency instance.
     *
     * @param type      the currency type; must not be {@code null}
     * @param symbol    the graphical symbol; must not be {@code null}
     * @param shortName the short identifier; must not be {@code null}
     * @param fullName  the full descriptive name
     * @param code      the standardized currency code; must not be {@code null}
     */
    public Currency(CurrencyType type, String symbol, String shortName, String fullName, String code) {
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency)) return false;
        Currency other = (Currency) obj;
        return Objects.equals(this.code, other.code);
    }

    /**
     * Retrieves a currency by its code (case-insensitive).
     * <p>
     * If currencies have not yet been loaded, the JSON configuration file is parsed.
     *
     * @param code the currency code to search for; must not be {@code null}
     * @return the matching {@code Currency}, or {@code null} if no match is found
     */
    public static Currency get(String code) {
        loadFromJson();
        return loaded.get(code.toUpperCase());
    }

    /**
     * Returns all loaded currencies.
     * <p>
     * If currencies have not yet been loaded, the JSON configuration file is parsed.
     *
     * @return a collection containing all available {@code Currency} instances
     */
    public static Collection<Currency> all() {
        loadFromJson();
        return loaded.values();
    }

    /**
     * Loads currency definitions from the JSON configuration file located at
     * {@code /json/currency/Currencies.json}.
     * <p>
     * This method uses lazy initialization: currencies are loaded only once.
     * The JSON file must contain an array under the key {@code "currencies"}.
     * Each element is mapped to a {@code Currency} instance using Jackson.
     * <p>
     * If the file is missing, malformed, or unreadable, an error is logged and
     * loading is aborted without throwing exceptions.
     */
    private static void loadFromJson() {
        if (initialized) return;

        try {
            InputStream is = Currency.class.getResourceAsStream("/json/currency/Currencies.json");
            if (is == null) {
                System.err.println("File not found: Currencies.json");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);
            JsonNode currenciesNode = root.get("currencies");

            if (currenciesNode == null || !currenciesNode.isArray()) {
                System.err.println("Key 'currencies' not found or not an array");
                return;
            }

            for (JsonNode node : currenciesNode) {
                Currency c = mapper.treeToValue(node, Currency.class);
                loaded.put(c.getCode().toUpperCase(), c);
                System.out.println("\nCurrencies loaded: " + loaded.size()
                        + " - " + c.getCode() + " - " + c.getFullName());
            }

            initialized = true;

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
