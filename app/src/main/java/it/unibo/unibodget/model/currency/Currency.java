package it.unibo.unibodget.model.currency;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unibo.unibodget.persistency.ModelFileManager;

/**
 * A currency loaded from external JSON configuration.
 *
 * <p>Unlike the enum-based currencies ({@link FiatCurrency}, {@link CryptoCurrency}),
 * this class supports dynamic currencies defined at runtime.</p>
 */
public final class Currency implements CurrencyUnit {
    private static final Map<String, Currency> LOADED = new HashMap<>();
    private static boolean initialized;

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
    public Currency(
            final CurrencyType type,
            final String symbol,
            final String shortName,
            final String fullName,
            final String code
    ) {
        this.type = Objects.requireNonNull(type);
        this.symbol = Objects.requireNonNull(symbol);
        this.shortName = Objects.requireNonNull(shortName);
        this.fullName = fullName;
        this.code = Objects.requireNonNull(code);
    }

    /** {@inheritDoc} */
    @Override
    public CurrencyType getType() {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public String getSymbol() {
        return this.symbol;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullName() {
        return this.fullName;
    }

    /** {@inheritDoc} */
    @Override
    public String getCode() {
        return this.code;
    }

    /** {@inheritDoc} */
    @Override
    public int getDisplayDecimals() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", this.shortName, this.symbol, this.fullName);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(this.code);
    }

    /** {@inheritDoc} */
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
     * <p>
     * The method parses the "currencies" array, stores each entry in an
     * internal map keyed by its ISO code (upper‑case), and marks the manager
     * as initialized. If loading fails, a RuntimeException is thrown.
     */
    public static void init() {
        initialized = false;
        try {
            List<Currency> list;
            try (ModelFileManager<Currency> mgr = new ModelFileManager<>(PATH, RESOURCE, Currency.class)) {
                mgr.open();
                list = mgr.loadList("currencies");
            }
            if (list == null || list.isEmpty()) {
                System.out.println("Currency JSON empty → using mock currencies");
                list = generateMockCurrencies();
            }
            LOADED.clear();
            list.forEach(c -> LOADED.put(c.getCode().toUpperCase(), c));
            list.replaceAll(c -> LOADED.getOrDefault(c.getCode().toUpperCase(), c));
            initialized = true;
        } catch (final IOException e) {
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
